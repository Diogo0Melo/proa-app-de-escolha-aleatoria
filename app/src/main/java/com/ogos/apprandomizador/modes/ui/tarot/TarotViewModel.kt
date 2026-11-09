package com.ogos.apprandomizador.modes.ui.tarot

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ogos.apprandomizador.data.repository.IOracleRepository
import com.ogos.apprandomizador.modes.domain.AiReadingResult
import com.ogos.apprandomizador.modes.domain.AiSettingsRepository
import com.ogos.apprandomizador.modes.domain.StoredOutcome
import com.ogos.apprandomizador.modes.domain.StoredTarotCard
import com.ogos.apprandomizador.modes.domain.DeckProvider
import com.ogos.apprandomizador.modes.domain.DrawnCard
import com.ogos.apprandomizador.modes.domain.OfflineReadingInterpreter
import com.ogos.apprandomizador.modes.domain.Reading
import com.ogos.apprandomizador.modes.domain.ReadingInterpreter
import com.ogos.apprandomizador.modes.domain.ReadingSection
import com.ogos.apprandomizador.modes.domain.Spread
import com.ogos.apprandomizador.modes.domain.TarotDrawer
import com.ogos.apprandomizador.viewmodel.EntropySource
import com.ogos.apprandomizador.viewmodel.deriveOracleStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TarotPhase {
    data object Idle : TarotPhase
    data object Revealing : TarotPhase
    data object Interpreting : TarotPhase
    data object Revealed : TarotPhase
}

/** Ação de IA disponível na leitura. Derivada do estado: a tela não combina flags soltas. */
enum class AiAction { None, Deepen, Loading, Retry }

@Immutable
data class TarotUiState(
    val question: String = "",
    val spread: Spread = Spread.SINGLE,
    val allowReversed: Boolean = true,
    val phase: TarotPhase = TarotPhase.Idle,
    val requestId: Long = 0L,
    val drawn: List<DrawnCard> = emptyList(),
    /** Quantas cartas já foram reveladas e fixadas na tela, em ordem (0 até drawn.size). */
    val landedCount: Int = 0,
    val reading: Reading? = null,
    val aiEnabled: Boolean = false,
    val aiReading: Reading? = null,
    val aiModel: String? = null,
    val aiLoading: Boolean = false,
    /** Número da tentativa em curso (1 = primeira). Valores acima de 1 indicam nova tentativa. */
    val aiAttempt: Int = 0,
    val aiError: String? = null,
    /** Geração cancelada pelo usuário: não é erro nem sucesso, então pode ser refeita. */
    val aiCancelled: Boolean = false
) {
    /**
     * Uma leitura de IA com sucesso encerra os botões de IA para esta tiragem.
     * Só há nova chamada se houve erro ou cancelamento.
     */
    val aiAction: AiAction
        get() = when {
            !aiEnabled || phase != TarotPhase.Revealed -> AiAction.None
            aiReading != null -> AiAction.None
            aiLoading -> AiAction.Loading
            aiError != null || aiCancelled -> AiAction.Retry
            else -> AiAction.Deepen
        }
}

class TarotViewModel(
    private val entropySource: EntropySource,
    private val deckProvider: DeckProvider,
    private val readingInterpreter: ReadingInterpreter = OfflineReadingInterpreter(),
    private val oracleRepository: IOracleRepository? = null,
    private val aiReadingService: com.ogos.apprandomizador.modes.domain.AiReadingService? = null,
    aiSettingsRepository: AiSettingsRepository? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(TarotUiState())
    val uiState: StateFlow<TarotUiState> = _uiState.asStateFlow()
    private var nextRequestId = 0L
    private var aiJob: kotlinx.coroutines.Job? = null

    init {
        aiSettingsRepository?.let { repository ->
            viewModelScope.launch {
                repository.settings.collect { settings ->
                    val config = settings.config
                    val enabled = config != null && config.enabled && settings.hasApiKey && runCatching {
                        settings.consentedAuthority == com.ogos.apprandomizador.modes.domain.BaseUrlNormalizer.authority(config.baseUrl)
                    }.getOrDefault(false)
                    _uiState.value = _uiState.value.copy(aiEnabled = enabled)
                    if (!enabled) clearAiState()
                }
            }
        }
    }

    fun onQuestionChange(value: String) {
        if (_uiState.value.phase != TarotPhase.Idle) return
        _uiState.value = _uiState.value.copy(question = value.take(140))
    }

    fun onSpreadChange(spread: Spread) {
        if (_uiState.value.phase != TarotPhase.Idle) return
        _uiState.value = _uiState.value.copy(spread = spread)
    }

    fun onAllowReversedChange(allow: Boolean) {
        if (_uiState.value.phase != TarotPhase.Idle) return
        _uiState.value = _uiState.value.copy(allowReversed = allow)
    }

    fun onDraw() {
        if (_uiState.value.phase != TarotPhase.Idle) return
        val current = _uiState.value
        val requestId = ++nextRequestId
        val stream = deriveOracleStream(entropySource.nextSeed(), requestId)
        val drawn = TarotDrawer.draw(deckProvider.getDeck(), current.spread, stream, current.allowReversed)
        _uiState.value = current.copy(
            phase = TarotPhase.Revealing,
            requestId = requestId,
            drawn = drawn,
            landedCount = 0,
            reading = null
        )
    }

    /** Uma carta terminou a animação e foi fixada na tela. A leitura começa quando a última é fixada. */
    fun onCardLanded(requestId: Long, index: Int) {
        val current = _uiState.value
        if (current.phase != TarotPhase.Revealing || current.requestId != requestId) return
        if (index != current.landedCount) return
        val landed = current.copy(landedCount = current.landedCount + 1)
        _uiState.value = landed
        if (landed.landedCount == landed.drawn.size) beginInterpretation(landed)
    }

    /** Encerra a sequência de revelação de uma vez: usado com animações desligadas e em testes. */
    fun onRevealFinished(requestId: Long) {
        val current = _uiState.value
        if (current.phase != TarotPhase.Revealing || current.requestId != requestId) return
        beginInterpretation(current.copy(landedCount = current.drawn.size))
    }

    private fun beginInterpretation(current: TarotUiState) {
        val requestId = current.requestId
        _uiState.value = current.copy(phase = TarotPhase.Interpreting)
        viewModelScope.launch {
            try {
                oracleRepository?.record(
                    "tarot",
                    StoredOutcome.Tarot(
                        spread = current.spread.name,
                        cards = current.drawn.map { StoredTarotCard(it.card.id, it.reversed) }
                    )
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                java.util.logging.Logger.getLogger("TarotViewModel").log(java.util.logging.Level.WARNING, "Não foi possível registrar o uso do oráculo", error)
            }
            val reading = try {
                readingInterpreter.interpret(current.question, current.drawn)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                Reading(
                    summary = "A tiragem foi concluída. Não foi possível montar a interpretação automática.",
                    sections = current.drawn.map { card ->
                        ReadingSection(card.position, card.card.name, card.meaning.essence)
                    }
                )
            }
            val latest = _uiState.value
            if (latest.phase == TarotPhase.Interpreting && latest.requestId == requestId) {
                _uiState.value = latest.copy(phase = TarotPhase.Revealed, reading = reading)
            }
        }
    }

    fun onDeepenWithAi() {
        val current = _uiState.value
        if (current.aiAction != AiAction.Deepen && current.aiAction != AiAction.Retry) return
        val service = aiReadingService ?: return
        aiJob?.cancel()
        val requestId = current.requestId
        aiJob = viewModelScope.launch {
            _uiState.value = current.copy(aiLoading = true, aiAttempt = 1, aiError = null, aiReading = null, aiModel = null, aiCancelled = false)
            val onRetry: (Int) -> Unit = { attempt ->
                val latest = _uiState.value
                if (latest.aiLoading && latest.requestId == requestId) _uiState.value = latest.copy(aiAttempt = attempt)
            }
            when (val result = service.deepen(current.question, current.drawn, onRetry)) {
                is AiReadingResult.Success -> {
                    val latest = _uiState.value
                    if (latest.phase == TarotPhase.Revealed && latest.requestId == requestId) {
                        _uiState.value = latest.copy(aiLoading = false, aiAttempt = 0, aiReading = result.reading, aiModel = result.modelId, aiError = null)
                    }
                }
                is AiReadingResult.Failure -> {
                    val latest = _uiState.value
                    if (latest.phase == TarotPhase.Revealed && latest.requestId == requestId) {
                        _uiState.value = latest.copy(aiLoading = false, aiAttempt = 0, aiError = result.reason)
                    }
                }
            }
        }
    }

    /** Sem IA ativa, a leitura de IA sai da tela; a leitura offline continua disponível. */
    private fun clearAiState() {
        aiJob?.cancel()
        _uiState.value = _uiState.value.copy(aiReading = null, aiModel = null, aiLoading = false, aiAttempt = 0, aiError = null, aiCancelled = false)
    }

    fun onCancelAi() {
        if (!_uiState.value.aiLoading) return
        aiJob?.cancel()
        _uiState.value = _uiState.value.copy(aiLoading = false, aiAttempt = 0, aiCancelled = true)
    }

    fun onReset() {
        if (_uiState.value.phase == TarotPhase.Revealing || _uiState.value.phase == TarotPhase.Interpreting || _uiState.value.aiLoading) return
        aiJob?.cancel()
        _uiState.value = _uiState.value.copy(question = "", phase = TarotPhase.Idle, drawn = emptyList(), landedCount = 0, reading = null, aiReading = null, aiModel = null, aiLoading = false, aiAttempt = 0, aiError = null, aiCancelled = false)
    }
}
