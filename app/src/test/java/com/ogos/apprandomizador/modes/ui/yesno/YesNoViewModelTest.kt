package com.ogos.apprandomizador.modes.ui.yesno

import com.ogos.apprandomizador.viewmodel.EntropySource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YesNoViewModelTest {
    private class FakeEntropySource : EntropySource {
        private var seed = 0
        override fun nextSeed(): ByteArray = ByteArray(32) { (it + seed++).toByte() }
    }

    @Test
    fun `ask reveals and only matching request can finish`() {
        val viewModel = YesNoViewModel(FakeEntropySource())
        assertEquals(YesNoPhase.Idle, viewModel.uiState.value.phase)

        viewModel.onAsk("  Devo ir?  ")
        val first = viewModel.uiState.value
        assertEquals(YesNoPhase.Revealing, first.phase)
        assertEquals("Devo ir?", first.question)
        assertNotNull(first.answer)
        assertTrue(first.durationMillis in 700L..1_200L)

        viewModel.onAsk("segunda pergunta")
        assertEquals(first.requestId, viewModel.uiState.value.requestId)
        viewModel.onRevealFinished(first.requestId + 1)
        assertEquals(YesNoPhase.Revealing, viewModel.uiState.value.phase)

        viewModel.onRevealFinished(first.requestId)
        assertEquals(YesNoPhase.Revealed, viewModel.uiState.value.phase)
        assertEquals(first.answer, viewModel.uiState.value.answer)
    }

    @Test
    fun `reset returns to idle and next request gets a new id`() {
        val viewModel = YesNoViewModel(FakeEntropySource())
        viewModel.onAsk(null)
        val requestId = viewModel.uiState.value.requestId
        viewModel.onReset()
        assertEquals(YesNoPhase.Idle, viewModel.uiState.value.phase)
        assertNull(viewModel.uiState.value.answer)
        viewModel.onAsk(null)
        assertNotEquals(requestId, viewModel.uiState.value.requestId)
    }

    private fun assertNotNull(value: Any?) = org.junit.Assert.assertNotNull(value)
    private fun assertTrue(value: Boolean) = org.junit.Assert.assertTrue(value)
}
