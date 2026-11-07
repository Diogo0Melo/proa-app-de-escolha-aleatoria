package com.ogos.apprandomizador.modes.ui.tarot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ogos.apprandomizador.modes.domain.DeckProvider
import com.ogos.apprandomizador.modes.domain.DrawnCard
import com.ogos.apprandomizador.modes.domain.OfflineReadingInterpreter
import com.ogos.apprandomizador.modes.domain.Reading
import com.ogos.apprandomizador.modes.domain.ReadingInterpreter
import com.ogos.apprandomizador.modes.domain.ReadingSection
import com.ogos.apprandomizador.modes.domain.Spread
import com.ogos.apprandomizador.modes.domain.TarotDrawer
import com.ogos.apprandomizador.viewmodel.AndroidMonotonicClock
import com.ogos.apprandomizador.viewmodel.EntropySource
import com.ogos.apprandomizador.viewmodel.MonotonicClock
import com.ogos.apprandomizador.viewmodel.deriveOracleStream
import com.ogos.apprandomizador.viewmodel.uniformInt
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

data class TarotUiState(
    val question: String = "",
    val spread: Spread = Spread.SINGLE,
    val allowReversed: Boolean = true,
    val phase: TarotPhase = TarotPhase.Idle,
    val requestId: Long = 0L,
    val revealStartElapsedRealtime: Long = 0L,
    val durationMillis: Long = 900L,
    val drawn: List<DrawnCard> = emptyList(),
    val reading: Reading? = null
)

class TarotViewModel(
    private val entropySource: EntropySource,
    private val deckProvider: DeckProvider,
    private val readingInterpreter: ReadingInterpreter = OfflineReadingInterpreter(),
    private val clock: MonotonicClock = AndroidMonotonicClock
) : ViewModel() {
    private val _uiState = MutableStateFlow(TarotUiState())
    val uiState: StateFlow<TarotUiState> = _uiState.asStateFlow()
    private var nextRequestId = 0L

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
        val drawn =
            TarotDrawer.draw(deckProvider.getDeck(), current.spread, stream, current.allowReversed)
        val duration = 700L + stream.uniformInt(501)
        _uiState.value = current.copy(
            phase = TarotPhase.Revealing,
            requestId = requestId,
            revealStartElapsedRealtime = clock.nowMillis(),
            durationMillis = duration,
            drawn = drawn,
            reading = null
        )
    }

    fun onRevealFinished(requestId: Long) {
        val current = _uiState.value
        if (current.phase != TarotPhase.Revealing || current.requestId != requestId) return
        _uiState.value = current.copy(phase = TarotPhase.Interpreting)
        viewModelScope.launch {
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

    fun onReset() {
        if (_uiState.value.phase == TarotPhase.Revealing || _uiState.value.phase == TarotPhase.Interpreting) return
        _uiState.value = _uiState.value.copy(
            question = "",
            phase = TarotPhase.Idle,
            drawn = emptyList(),
            reading = null
        )
    }
}
