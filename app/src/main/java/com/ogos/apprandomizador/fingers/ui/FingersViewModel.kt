package com.ogos.apprandomizador.fingers.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ogos.apprandomizador.data.repository.IOracleRepository
import com.ogos.apprandomizador.fingers.domain.FingersDirection
import com.ogos.apprandomizador.fingers.domain.FingersPalette
import com.ogos.apprandomizador.fingers.domain.FingersPick
import com.ogos.apprandomizador.fingers.domain.FingersPicker
import com.ogos.apprandomizador.modes.domain.StoredOutcome
import com.ogos.apprandomizador.viewmodel.EntropySource
import com.ogos.apprandomizador.viewmodel.deriveFingerStream
import java.util.logging.Level
import java.util.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val COUNTDOWN_START = 3
private const val COUNTDOWN_STEP_MILLIS = 1_000L

enum class FingersPhase { Idle, Counting, Revealed }

@Immutable
data class FingerState(
    val id: Long,
    val position: Offset,
    val color: Long
)

@Immutable
data class FingersUiState(
    val fingers: List<FingerState> = emptyList(),
    val phase: FingersPhase = FingersPhase.Idle,
    val countdown: Int = COUNTDOWN_START,
    val winnerId: Long? = null,
    val winnerDirection: FingersDirection? = null
) {
    val winner: FingerState? get() = winnerId?.let { id -> fingers.firstOrNull { it.id == id } }
}

/**
 * Estado da tela Dedos. A UI só envia eventos de ponteiro; as regras moram aqui.
 *
 * Fases: Idle (menos de 2 dedos) → Counting (3→2→1) → Revealed (resultado).
 * - Entrar ou sair de um dedo reinicia a contagem. Mover um dedo não reinicia.
 * - Em Revealed o estado fica congelado: só [onReset] volta a Idle.
 * - O sorteio usa um fluxo de entropia próprio por pedido ([deriveFingerStream]).
 */
class FingersViewModel(
    private val entropySource: EntropySource,
    private val oracleRepository: IOracleRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(FingersUiState())
    val uiState: StateFlow<FingersUiState> = _uiState.asStateFlow()

    private var nextRequestId = 0L
    private var nextEntryIndex = 0
    private var countdownJob: Job? = null

    /** Um dedo entrou. Ignorado durante a revelação. */
    fun onFingerDown(id: Long, position: Offset) {
        val current = _uiState.value
        if (current.phase == FingersPhase.Revealed) return
        if (current.fingers.any { it.id == id }) return
        val color = FingersPalette.colorAt(nextEntryIndex++)
        _uiState.value = current.copy(fingers = current.fingers + FingerState(id, position, color))
        restartCountdown()
    }

    /** Um dedo se moveu. Atualiza a posição e não reinicia a contagem. */
    fun onFingerMove(id: Long, position: Offset) {
        val current = _uiState.value
        if (current.phase == FingersPhase.Revealed) return
        if (current.fingers.none { it.id == id }) return
        _uiState.value = current.copy(
            fingers = current.fingers.map { if (it.id == id) it.copy(position = position) else it }
        )
    }

    /** Um dedo saiu. Ignorado durante a revelação. */
    fun onFingerUp(id: Long) {
        val current = _uiState.value
        if (current.phase == FingersPhase.Revealed) return
        if (current.fingers.none { it.id == id }) return
        _uiState.value = current.copy(fingers = current.fingers.filterNot { it.id == id })
        restartCountdown()
    }

    /** Volta ao estado inicial. Chamado ao fechar o resultado e ao sair da tela. */
    fun onReset() {
        countdownJob?.cancel()
        countdownJob = null
        nextEntryIndex = 0
        _uiState.value = FingersUiState()
    }

    private fun restartCountdown() {
        countdownJob?.cancel()
        val current = _uiState.value
        if (current.fingers.size < FingersPicker.MIN_FINGERS) {
            _uiState.value = current.copy(
                phase = FingersPhase.Idle,
                countdown = COUNTDOWN_START,
                winnerId = null,
                winnerDirection = null
            )
            return
        }
        _uiState.value = current.copy(
            phase = FingersPhase.Counting,
            countdown = COUNTDOWN_START,
            winnerId = null,
            winnerDirection = null
        )
        val requestId = ++nextRequestId
        countdownJob = viewModelScope.launch { countDownThenPick(requestId) }
    }

    private suspend fun countDownThenPick(requestId: Long) {
        for (value in COUNTDOWN_START downTo 1) {
            _uiState.value = _uiState.value.copy(countdown = value)
            delay(COUNTDOWN_STEP_MILLIS)
        }
        // A contagem só chega aqui sem cancelamento, então a lista é a que está na tela.
        val pickable = _uiState.value.fingers
        if (pickable.size < FingersPicker.MIN_FINGERS) return

        val pick = FingersPicker.pick(
            fingerCount = pickable.size,
            stream = deriveFingerStream(entropySource.nextSeed(), requestId)
        )
        val winner = pickable[pick.winnerIndex]
        _uiState.value = _uiState.value.copy(
            phase = FingersPhase.Revealed,
            winnerId = winner.id,
            winnerDirection = pick.direction
        )
        record(pick, pickable.size)
    }

    /** Grava o uso em segundo plano. Falha não esconde o resultado, como no oráculo. */
    private fun record(pick: FingersPick, fingerCount: Int) {
        val repository = oracleRepository ?: return
        viewModelScope.launch {
            try {
                repository.record(
                    MODE_ID,
                    StoredOutcome.Fingers(
                        fingerIndex = pick.winnerIndex,
                        direction = pick.direction.name,
                        fingerCount = fingerCount
                    )
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Logger.getLogger("FingersViewModel")
                    .log(Level.WARNING, "Não foi possível registrar o sorteio dos dedos", error)
            }
        }
    }

    companion object {
        const val MODE_ID = "fingers"
    }
}
