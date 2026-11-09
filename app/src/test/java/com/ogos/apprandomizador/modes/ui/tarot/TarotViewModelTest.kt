package com.ogos.apprandomizador.modes.ui.tarot

import com.ogos.apprandomizador.modes.data.FullTarotDeck
import com.ogos.apprandomizador.modes.domain.Spread
import com.ogos.apprandomizador.viewmodel.EntropySource
import com.ogos.apprandomizador.viewmodel.MonotonicClock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TarotViewModelTest {
    private class FakeEntropySource : EntropySource {
        private var seed = 0
        override fun nextSeed(): ByteArray = ByteArray(32) { (it + seed++).toByte() }
    }

    @Test fun `draw enters revealing with selected spread and no duplicate cards`() {
        val vm = TarotViewModel(FakeEntropySource(), FullTarotDeck, clock = MonotonicClock { 5_000L })
        vm.onQuestionChange("Qual caminho explorar?")
        vm.onSpreadChange(Spread.PAST_PRESENT_FUTURE)
        vm.onDraw()
        val first = vm.uiState.value
        assertEquals(TarotPhase.Revealing, first.phase)
        assertEquals(3, first.drawn.size)
        assertEquals(3, first.drawn.map { it.card.id }.distinct().size)
        assertEquals("Qual caminho explorar?", first.question)
        assertEquals(5_000L, first.revealStartElapsedRealtime)
        vm.onDraw()
        assertEquals(first.requestId, vm.uiState.value.requestId)
    }

    @Test fun `reset cannot interrupt a reveal already decided by the oracle`() {
        val vm = TarotViewModel(FakeEntropySource(), FullTarotDeck, clock = MonotonicClock { 5_000L })
        vm.onDraw()
        val request = vm.uiState.value.requestId
        vm.onReset()
        assertEquals(TarotPhase.Revealing, vm.uiState.value.phase)
        assertEquals(request, vm.uiState.value.requestId)
        assertTrue(vm.uiState.value.drawn.isNotEmpty())
    }
}
