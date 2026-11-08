package com.ogos.apprandomizador.modes.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ogos.apprandomizador.modes.domain.AiConfig
import com.ogos.apprandomizador.modes.domain.AiSettingsRepository
import com.ogos.apprandomizador.modes.domain.ApiErrorMapper
import com.ogos.apprandomizador.modes.domain.BaseUrlNormalizer
import com.ogos.apprandomizador.modes.domain.LlmClient
import com.ogos.apprandomizador.modes.domain.LlmHttpException
import com.ogos.apprandomizador.modes.domain.Secret
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ConnectionState {
    data object Idle : ConnectionState
    data object Testing : ConnectionState
    data class Success(val modelCount: Int, val elapsedMillis: Long) : ConnectionState
    data class Error(val message: String) : ConnectionState
}

data class AiSettingsUiState(
    val baseUrl: String = "https://api.openai.com/v1",
    val model: String = "",
    val apiKeyInput: Secret? = null,
    val hasSavedKey: Boolean = false,
    val consentedAuthority: String? = null,
    val enabled: Boolean = true,
    val models: List<String> = emptyList(),
    val showAllModels: Boolean = false,
    val connection: ConnectionState = ConnectionState.Idle,
    val message: String? = null,
    val isSaving: Boolean = false
) {
    val hasConsent: Boolean
        get() = consentedAuthority != null && runCatching { BaseUrlNormalizer.authority(baseUrl) == consentedAuthority }.getOrDefault(false)
}

class AiSettingsViewModel(
    private val repository: AiSettingsRepository,
    private val client: LlmClient
) : ViewModel() {
    private val _state = MutableStateFlow(AiSettingsUiState())
    private var settingsLoaded = false
    private var connectionJob: kotlinx.coroutines.Job? = null
    val state: StateFlow<AiSettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.settings.collect { settings ->
                val config = settings.config
                val firstLoad = !settingsLoaded
                settingsLoaded = true
                _state.value = _state.value.copy(
                    baseUrl = if (firstLoad) config?.baseUrl ?: _state.value.baseUrl else _state.value.baseUrl,
                    model = if (firstLoad) config?.model ?: _state.value.model else _state.value.model,
                    enabled = if (firstLoad) config?.enabled ?: true else _state.value.enabled,
                    hasSavedKey = settings.hasApiKey,
                    consentedAuthority = settings.consentedAuthority
                )
            }
        }
    }

    fun updateBaseUrl(value: String) {
        connectionJob?.cancel()
        _state.value = _state.value.copy(baseUrl = value, models = emptyList(), connection = ConnectionState.Idle, message = null)
    }
    fun updateModel(value: String) { connectionJob?.cancel(); _state.value = _state.value.copy(model = value, connection = ConnectionState.Idle) }
    fun updateApiKey(value: String) { connectionJob?.cancel(); _state.value = _state.value.copy(apiKeyInput = value.takeIf(String::isNotEmpty)?.let(::Secret), connection = ConnectionState.Idle) }
    fun toggleEnabled(value: Boolean) { _state.value = _state.value.copy(enabled = value) }
    fun toggleShowAll(value: Boolean) { _state.value = _state.value.copy(showAllModels = value) }

    fun acceptConsentAndTest() {
        viewModelScope.launch {
            try {
                val authority = BaseUrlNormalizer.authority(_state.value.baseUrl)
                repository.saveConsent(authority)
                _state.value = _state.value.copy(consentedAuthority = authority)
                testConnection()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.value = _state.value.copy(message = ApiErrorMapper.message(error))
            }
        }
    }

    fun testConnection() {
        if (_state.value.connection == ConnectionState.Testing || connectionJob?.isActive == true) return
        val input = _state.value
        if (!input.hasConsent) {
            _state.value = input.copy(message = "Aceite o envio de dados para testar a conexão.")
            return
        }
        connectionJob = viewModelScope.launch {
            _state.value = input.copy(connection = ConnectionState.Testing, message = null)
            val started = (System.nanoTime() / 1_000_000L)
            try {
                val base = BaseUrlNormalizer.normalize(input.baseUrl)
                val key = input.apiKeyInput?.value?.takeIf(String::isNotBlank) ?: repository.apiKey()
                    ?: error("Informe a chave da API.")
                val models = try {
                    client.listModels(base, key)
                } catch (status: LlmHttpException) {
                    if (status.statusCode !in setOf(404, 405)) throw status
                    require(input.model.isNotBlank()) { "Informe o model ID para testar este provedor." }
                    client.ping(base, key, input.model.trim())
                    emptyList()
                }
                _state.value = _state.value.copy(
                    models = models,
                    connection = ConnectionState.Success(models.size, (System.nanoTime() / 1_000_000L) - started),
                    message = if (models.isEmpty()) "Conectado; o provedor não disponibilizou a lista de modelos." else null
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.value = _state.value.copy(connection = ConnectionState.Error(ApiErrorMapper.message(error)), message = null)
            }
        }
    }

    fun save() {
        connectionJob?.cancel()
        val current = _state.value.copy(connection = ConnectionState.Idle)
        viewModelScope.launch {
            _state.value = current.copy(isSaving = true, message = null)
            try {
                repository.save(AiConfig(current.baseUrl, current.model, current.enabled), current.apiKeyInput?.value)
                _state.value = _state.value.copy(isSaving = false, apiKeyInput = null, message = "Configurações salvas com segurança.")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.value = _state.value.copy(isSaving = false, message = ApiErrorMapper.message(error))
            }
        }
    }

    fun removeCredentials() {
        connectionJob?.cancel()
        viewModelScope.launch {
            try {
                repository.removeCredentials()
                settingsLoaded = true
                _state.value = AiSettingsUiState()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.value = _state.value.copy(message = ApiErrorMapper.message(error))
            }
        }
    }

    fun visibleModels(): List<String> = _state.value.models.filter { model ->
        _state.value.showAllModels || listOf("embedding", "whisper", "tts", "image").none { model.contains(it, true) }
    }
}
