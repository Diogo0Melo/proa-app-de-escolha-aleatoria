package com.ogos.apprandomizador.modes.ui.yesno

import androidx.lifecycle.ViewModel
import com.ogos.apprandomizador.modes.domain.YesNoAnswer
import com.ogos.apprandomizador.modes.domain.YesNoOracle
import com.ogos.apprandomizador.data.repository.IOracleRepository
import com.ogos.apprandomizador.modes.domain.StoredOutcome
import com.ogos.apprandomizador.viewmodel.EntropySource
import com.ogos.apprandomizador.viewmodel.MonotonicClock
import com.ogos.apprandomizador.viewmodel.AndroidMonotonicClock
import com.ogos.apprandomizador.viewmodel.deriveOracleStream
import com.ogos.apprandomizador.viewmodel.nextIntInRange
import kotlinx.coroutines.CancellationException
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface YesNoPhase {
    data object Idle : YesNoPhase
    data object Revealing : YesNoPhase
    data object Recording : YesNoPhase
    data object Revealed : YesNoPhase
}

data class YesNoUiState(
    val phase: YesNoPhase = YesNoPhase.Idle,
    val answer: YesNoAnswer? = null,
    val requestId: Long = 0L,
    val durationMillis: Long = 0L,
    val revealStartElapsedRealtime: Long = 0L
)

class YesNoViewModel(
    private val entropySource: EntropySource,
    private val clock: MonotonicClock = AndroidMonotonicClock,
    private val oracleRepository: IOracleRepository? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(YesNoUiState())
    val uiState: StateFlow<YesNoUiState> = _uiState.asStateFlow()

    private var nextRequestId = 0L

    fun onAsk() {
        if (_uiState.value.phase != YesNoPhase.Idle) return

        val requestId = ++nextRequestId
        val stream = deriveOracleStream(entropySource.nextSeed(), requestId)
        val answer = YesNoOracle.ask(stream)
        val duration = stream.nextIntInRange(MIN_REVEAL_MILLIS, MAX_REVEAL_MILLIS)

        _uiState.value = YesNoUiState(
            phase = YesNoPhase.Revealing,
            answer = answer,
            requestId = requestId,
            durationMillis = duration.toLong(),
            revealStartElapsedRealtime = clock.nowMillis()
        )
    }

    fun onRevealFinished(requestId: Long) {
        val current = _uiState.value
        if (current.phase != YesNoPhase.Revealing || current.requestId != requestId) return
        _uiState.value = current.copy(phase = YesNoPhase.Recording)
        viewModelScope.launch {
            // A Revealed state must always carry the answer selected by the domain.
            val answer = current.answer ?: run {
                val latest = _uiState.value
                if (latest.phase == YesNoPhase.Recording && latest.requestId == requestId) {
                    _uiState.value = latest.copy(phase = YesNoPhase.Idle, answer = null)
                }
                return@launch
            }
            try {
                oracleRepository?.record("yes_no", StoredOutcome.YesNo(answer.name))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                java.util.logging.Logger.getLogger("YesNoViewModel").log(java.util.logging.Level.WARNING, "Não foi possível registrar o uso do oráculo", error)
            } finally {
                val latest = _uiState.value
                if (latest.phase == YesNoPhase.Recording && latest.requestId == requestId) {
                    _uiState.value = latest.copy(phase = YesNoPhase.Revealed)
                }
            }
        }
    }

    fun onReset() {
        // Uma revelação em andamento é concluída; reset não descarta um resultado já sorteado.
        if (_uiState.value.phase == YesNoPhase.Revealing || _uiState.value.phase == YesNoPhase.Recording) return
        _uiState.value = YesNoUiState()
    }

    private companion object {
        const val MIN_REVEAL_MILLIS = 700
        const val MAX_REVEAL_MILLIS = 1_200
    }
}
