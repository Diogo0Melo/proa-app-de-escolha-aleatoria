package com.ogos.apprandomizador.modes.ui.oracle

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ogos.apprandomizador.data.repository.IOracleRepository
import com.ogos.apprandomizador.data.repository.IRouletteSource
import com.ogos.apprandomizador.model.ItemList
import com.ogos.apprandomizador.model.RaffleItem
import com.ogos.apprandomizador.modes.domain.AiSettingsRepository
import com.ogos.apprandomizador.modes.domain.DefaultOracleReflections
import com.ogos.apprandomizador.modes.domain.OracleAiResult
import com.ogos.apprandomizador.modes.domain.OracleAiService
import com.ogos.apprandomizador.modes.domain.OracleDrawer
import com.ogos.apprandomizador.modes.domain.OracleReading
import com.ogos.apprandomizador.modes.domain.OracleSource
import com.ogos.apprandomizador.modes.domain.ReadingDepth
import com.ogos.apprandomizador.modes.domain.StoredOutcome
import com.ogos.apprandomizador.modes.domain.asOracleSource
import com.ogos.apprandomizador.modes.domain.isAiReady
import com.ogos.apprandomizador.modes.ui.ai.AiAction
import com.ogos.apprandomizador.modes.ui.ai.aiActionFor
import com.ogos.apprandomizador.viewmodel.EntropySource
import com.ogos.apprandomizador.viewmodel.deriveOracleStream
import com.ogos.apprandomizador.viewmodel.nextIntInRange
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class OraclePhase { Idle, Revealing, Recording, Revealed }

/** Aviso sobre a fonte. A interface escolhe o texto; a ViewModel não carrega strings. */
enum class OracleSourceNotice { RouletteRemoved }

@Immutable
data class OracleUiState(
    val question: String = "",
    val phase: OraclePhase = OraclePhase.Idle,
    val source: OracleSource = OracleSource.CLASSIC,
    val linkedRouletteId: Long? = null,
    val sourceNotice: OracleSourceNotice? = null,
    val availableSources: List<ItemList> = emptyList(),
    val answer: RaffleItem? = null,
    val requestId: Long = 0L,
    val durationMillis: Long = 0L,
    val aiEnabled: Boolean = false,
    val aiReading: OracleReading? = null,
    val aiModel: String? = null,
    val aiLoading: Boolean = false,
    val aiAttempt: Int = 0,
    val aiError: String? = null,
    val aiCancelled: Boolean = false
) {
    /** Resposta visível. Só existe depois da revelação, nunca durante a animação. */
    val revealedAnswer: RaffleItem? get() = answer.takeIf { phase == OraclePhase.Revealed }

    val canAsk: Boolean get() = phase == OraclePhase.Idle && source.items.isNotEmpty()

    /**
     * A leitura curta é automática e ainda permite aprofundar; só a estendida encerra as ações de IA.
     */
    val aiAction: AiAction
        get() = aiActionFor(
            enabled = aiEnabled,
            revealed = phase == OraclePhase.Revealed,
            hasReading = aiReading?.depth == ReadingDepth.Extended,
            loading = aiLoading,
            error = aiError,
            cancelled = aiCancelled
        )

    /** Frase de reflexão offline, só enquanto a resposta está revelada. */
    val reflection: String? get() = revealedAnswer?.let(DefaultOracleReflections::forAnswer)
}

/**
 * Oráculo: o sorteio decide a resposta; a IA, quando ativa, só a elabora.
 * Fases: Idle → Revealing (animação) → Recording (grava o uso) → Revealed.
 * Com IA ativa, uma leitura curta sai sozinha; a estendida é opcional e pedida pelo usuário.
 */
class OracleViewModel(
    private val entropySource: EntropySource,
    private val rouletteSource: IRouletteSource? = null,
    private val oracleRepository: IOracleRepository? = null,
    private val aiSettingsRepository: AiSettingsRepository? = null,
    private val aiService: OracleAiService? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(OracleUiState())
    val uiState: StateFlow<OracleUiState> = _uiState.asStateFlow()

    private var nextRequestId = 0L
    private var linkJob: Job? = null
    private var aiJob: Job? = null

    init {
        rouletteSource?.let { source ->
            viewModelScope.launch {
                source.allItems.collect { lists ->
                    _uiState.value = _uiState.value.copy(availableSources = lists)
                }
            }
        }
        aiSettingsRepository?.let { repository ->
            viewModelScope.launch {
                repository.settings.collect { settings ->
                    val enabled = settings.isAiReady()
                    _uiState.value = _uiState.value.copy(aiEnabled = enabled)
                    if (!enabled) clearAiState()
                }
            }
        }
    }

    fun onQuestionChange(value: String) {
        if (_uiState.value.phase != OraclePhase.Idle) return
        _uiState.value = _uiState.value.copy(question = value.take(MAX_QUESTION))
    }

    /** Texto ditado é acrescentado à pergunta. Quem confirma a pergunta é o usuário. */
    fun onSpokenQuestion(spoken: String) {
        val current = _uiState.value
        if (current.phase != OraclePhase.Idle) return
        val merged = listOf(current.question, spoken).filter(String::isNotBlank).joinToString(" ")
        _uiState.value = current.copy(question = merged.take(MAX_QUESTION))
    }

    /**
     * Vincula uma roleta do usuário como fonte. Só em Idle: trocar a fonte durante a leitura
     * faria um aprofundamento posterior usar um tema diferente do da pergunta original.
     */
    fun onLinkRoulette(id: Long) {
        val source = rouletteSource ?: return
        if (_uiState.value.phase != OraclePhase.Idle) return
        linkJob?.cancel()
        linkJob = viewModelScope.launch {
            source.getItemFlow(id).collect { list ->
                val current = _uiState.value
                _uiState.value = if (list == null) {
                    current.copy(
                        source = OracleSource.CLASSIC,
                        linkedRouletteId = null,
                        sourceNotice = OracleSourceNotice.RouletteRemoved
                    )
                } else {
                    current.copy(source = list.asOracleSource(), linkedRouletteId = id, sourceNotice = null)
                }
            }
        }
    }

    fun onUseClassic() {
        if (_uiState.value.phase != OraclePhase.Idle) return
        linkJob?.cancel()
        _uiState.value = _uiState.value.copy(
            source = OracleSource.CLASSIC,
            linkedRouletteId = null,
            sourceNotice = null
        )
    }

    fun onAsk() {
        val current = _uiState.value
        if (current.phase != OraclePhase.Idle || current.source.items.isEmpty()) return
        aiJob?.cancel()
        val requestId = ++nextRequestId
        val stream = deriveOracleStream(entropySource.nextSeed(), requestId)
        val answer = OracleDrawer.draw(current.source.items, stream)
        val duration = stream.nextIntInRange(MIN_REVEAL_MILLIS, MAX_REVEAL_MILLIS)
        _uiState.value = current.copy(
            phase = OraclePhase.Revealing,
            answer = answer,
            requestId = requestId,
            durationMillis = duration.toLong(),
            aiReading = null,
            aiModel = null,
            aiLoading = false,
            aiAttempt = 0,
            aiError = null,
            aiCancelled = false
        )
    }

    /** A animação terminou: grava o uso e revela. Com IA ativa, a leitura curta começa sozinha. */
    fun onRevealFinished(requestId: Long) {
        val current = _uiState.value
        if (current.phase != OraclePhase.Revealing || current.requestId != requestId) return
        val answer = current.answer ?: return
        _uiState.value = current.copy(phase = OraclePhase.Recording)
        viewModelScope.launch {
            try {
                oracleRepository?.record(
                    MODE_ID,
                    StoredOutcome.Oracle(answer = answer.name, sourceTopic = current.source.topic)
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                java.util.logging.Logger.getLogger("OracleViewModel")
                    .log(java.util.logging.Level.WARNING, "Não foi possível registrar o uso do oráculo", error)
            }
            val latest = _uiState.value
            if (latest.phase == OraclePhase.Recording && latest.requestId == requestId) {
                _uiState.value = latest.copy(phase = OraclePhase.Revealed)
                if (latest.aiEnabled) onDeepenWithAi()
            }
        }
    }

    /**
     * Pede a IA. Sem leitura ainda, pede a curta. Com a curta pronta, o mesmo botão pede a
     * estendida. Depois da estendida com sucesso, nenhuma ação de IA volta para esta tiragem.
     */
    fun onDeepenWithAi() {
        val current = _uiState.value
        if (current.aiAction != AiAction.Deepen && current.aiAction != AiAction.Retry) return
        val service = aiService ?: return
        val answer = current.answer ?: return
        val depth = if (current.aiReading == null) ReadingDepth.Short else ReadingDepth.Extended
        aiJob?.cancel()
        val requestId = current.requestId
        aiJob = viewModelScope.launch {
            // A leitura curta, se existir, continua visível durante a estendida.
            _uiState.value = _uiState.value.copy(aiLoading = true, aiAttempt = 1, aiError = null, aiCancelled = false)
            val onRetry: (Int) -> Unit = { attempt ->
                val latest = _uiState.value
                if (latest.aiLoading && latest.requestId == requestId) {
                    _uiState.value = latest.copy(aiAttempt = attempt)
                }
            }
            when (val result = service.deepen(current.question, answer, current.source.topic, depth, onRetry)) {
                is OracleAiResult.Success -> {
                    val latest = _uiState.value
                    if (latest.phase == OraclePhase.Revealed && latest.requestId == requestId) {
                        _uiState.value = latest.copy(
                            aiLoading = false,
                            aiAttempt = 0,
                            aiReading = result.reading,
                            aiModel = result.modelId,
                            aiError = null
                        )
                    }
                }
                is OracleAiResult.Failure -> {
                    val latest = _uiState.value
                    if (latest.phase == OraclePhase.Revealed && latest.requestId == requestId) {
                        _uiState.value = latest.copy(aiLoading = false, aiAttempt = 0, aiError = result.reason)
                    }
                }
            }
        }
    }

    /** Cancelar não é erro nem sucesso: o aprofundamento pode ser refeito. */
    fun onCancelAi() {
        if (!_uiState.value.aiLoading) return
        aiJob?.cancel()
        _uiState.value = _uiState.value.copy(aiLoading = false, aiAttempt = 0, aiCancelled = true)
    }

    /** Nova pergunta: bloqueada durante a revelação e durante a geração. A fonte vinculada persiste. */
    fun onReset() {
        val current = _uiState.value
        if (current.phase == OraclePhase.Revealing || current.phase == OraclePhase.Recording || current.aiLoading) return
        aiJob?.cancel()
        _uiState.value = current.copy(
            question = "",
            phase = OraclePhase.Idle,
            answer = null,
            aiReading = null,
            aiModel = null,
            aiLoading = false,
            aiAttempt = 0,
            aiError = null,
            aiCancelled = false
        )
    }

    private fun clearAiState() {
        aiJob?.cancel()
        _uiState.value = _uiState.value.copy(
            aiReading = null,
            aiModel = null,
            aiLoading = false,
            aiAttempt = 0,
            aiError = null,
            aiCancelled = false
        )
    }

    companion object {
        /** Mantém o identificador do modo: as estatísticas do hub já salvas continuam válidas. */
        const val MODE_ID = "yes_no"
        const val MAX_QUESTION = 140
        /** Revelação mais longa: dá tempo para a animação criar o momento. */
        const val MIN_REVEAL_MILLIS = 1_500
        const val MAX_REVEAL_MILLIS = 1_900
    }
}
