package com.ogos.apprandomizador.modes.ui.tarot

import com.ogos.apprandomizador.modes.data.FullTarotDeck
import com.ogos.apprandomizador.modes.domain.Spread
import com.ogos.apprandomizador.modes.ui.ai.MainDispatcherRule
import com.ogos.apprandomizador.viewmodel.EntropySource
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class TarotRevealSequenceTest {
    @get:Rule val mainDispatcher = MainDispatcherRule()

    private fun viewModelWithThreeCards() = TarotViewModel(
        entropySource = EntropySource { ByteArray(32) { (it * 3).toByte() } },
        deckProvider = FullTarotDeck
    ).also { vm ->
        vm.onSpreadChange(Spread.PAST_PRESENT_FUTURE)
        vm.onDraw()
    }

    @Test fun `cartas sao fixadas uma a uma e a leitura comeca depois da ultima`() = runTest(mainDispatcher.dispatcher) {
        val vm = viewModelWithThreeCards()
        val id = vm.uiState.value.requestId

        vm.onCardLanded(id, 0)
        assertEquals(1, vm.uiState.value.landedCount)
        assertEquals(TarotPhase.Revealing, vm.uiState.value.phase)

        vm.onCardLanded(id, 1)
        assertEquals(2, vm.uiState.value.landedCount)
        assertEquals(TarotPhase.Revealing, vm.uiState.value.phase)

        vm.onCardLanded(id, 2)
        assertEquals(3, vm.uiState.value.landedCount)
        assertEquals(TarotPhase.Interpreting, vm.uiState.value.phase)

        advanceUntilIdle()
        assertEquals(TarotPhase.Revealed, vm.uiState.value.phase)
        assertNotNull(vm.uiState.value.reading)
    }

    @Test fun `landing fora de ordem, repetido ou de rodada antiga e ignorado`() = runTest(mainDispatcher.dispatcher) {
        val vm = viewModelWithThreeCards()
        val id = vm.uiState.value.requestId

        vm.onCardLanded(id, 1)
        assertEquals(0, vm.uiState.value.landedCount)

        vm.onCardLanded(id, 0)
        vm.onCardLanded(id, 0)
        assertEquals(1, vm.uiState.value.landedCount)

        vm.onCardLanded(id + 1, 1)
        assertEquals(1, vm.uiState.value.landedCount)
    }

    @Test fun `encerrar a sequencia de uma vez fixa todas as cartas e inicia a leitura`() = runTest(mainDispatcher.dispatcher) {
        val vm = viewModelWithThreeCards()
        vm.onRevealFinished(vm.uiState.value.requestId)
        assertEquals(3, vm.uiState.value.landedCount)
        assertEquals(TarotPhase.Interpreting, vm.uiState.value.phase)

        advanceUntilIdle()
        assertEquals(TarotPhase.Revealed, vm.uiState.value.phase)
    }

    @Test fun `nova tiragem comeca sem cartas fixadas`() = runTest(mainDispatcher.dispatcher) {
        val vm = viewModelWithThreeCards()
        vm.onRevealFinished(vm.uiState.value.requestId)
        advanceUntilIdle()
        vm.onReset()
        vm.onDraw()
        assertEquals(0, vm.uiState.value.landedCount)
        assertEquals(TarotPhase.Revealing, vm.uiState.value.phase)
    }
}
