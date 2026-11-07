package com.ogos.apprandomizador.modes.ui.yesno

import androidx.lifecycle.ViewModel
import com.ogos.apprandomizador.modes.domain.YesNoAnswer
import com.ogos.apprandomizador.modes.domain.YesNoOracle
import com.ogos.apprandomizador.viewmodel.EntropySource
import com.ogos.apprandomizador.viewmodel.deriveOracleStream
import com.ogos.apprandomizador.viewmodel.nextIntInRange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface YesNoPhase {
    data object Idle : YesNoPhase
    data object Revealing : YesNoPhase
    data object Revealed : YesNoPhase
}

data class YesNoUiState(
    val phase: YesNoPhase = YesNoPhase.Idle,
    val answer: YesNoAnswer? = null,
    val requestId: Long = 0L,
    val durationMillis: Long = 0L,
    val question: String? = null
)

class YesNoViewModel(
    private val entropySource: EntropySource
) : ViewModel() {
    private val _uiState = MutableStateFlow(YesNoUiState())
    val uiState: StateFlow<YesNoUiState> = _uiState.asStateFlow()

    private var nextRequestId = 0L

    fun onAsk(question: String?) {
        if (_uiState.value.phase == YesNoPhase.Revealing) return

        val requestId = ++nextRequestId
        val stream = deriveOracleStream(entropySource.nextSeed(), requestId)
        val answer = YesNoOracle.ask(stream)
        val duration = stream.nextIntInRange(MIN_REVEAL_MILLIS, MAX_REVEAL_MILLIS)

        _uiState.value = YesNoUiState(
            phase = YesNoPhase.Revealing,
            answer = answer,
            requestId = requestId,
            durationMillis = duration.toLong(),
            question = question?.trim()?.takeIf(String::isNotEmpty)
        )
    }

    fun onRevealFinished(requestId: Long) {
        val current = _uiState.value
        if (current.phase != YesNoPhase.Revealing || current.requestId != requestId) return
        _uiState.value = current.copy(phase = YesNoPhase.Revealed)
    }

    fun onReset() {
        _uiState.value = YesNoUiState()
    }

    private companion object {
        const val MIN_REVEAL_MILLIS = 700
        const val MAX_REVEAL_MILLIS = 1_200
    }
}
