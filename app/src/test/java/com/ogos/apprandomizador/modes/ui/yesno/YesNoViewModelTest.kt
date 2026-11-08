package com.ogos.apprandomizador.modes.ui.yesno

import com.ogos.apprandomizador.modes.ui.ai.MainDispatcherRule
import com.ogos.apprandomizador.viewmodel.EntropySource
import com.ogos.apprandomizador.viewmodel.MonotonicClock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Rule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class YesNoViewModelTest {
    @get:Rule val mainDispatcher = MainDispatcherRule()
    private class FakeEntropySource : EntropySource {
        private var seed = 0
        override fun nextSeed(): ByteArray = ByteArray(32) { (it + seed++).toByte() }
    }

    @Test
    fun `ask reveals and only matching request can finish`() = runTest(mainDispatcher.dispatcher) {
        val viewModel = YesNoViewModel(FakeEntropySource(), MonotonicClock { 1_000L })
        assertEquals(YesNoPhase.Idle, viewModel.uiState.value.phase)

        viewModel.onAsk()
        val first = viewModel.uiState.value
        assertEquals(YesNoPhase.Revealing, first.phase)
        assertNotNull(first.answer)
        assertTrue(first.durationMillis in 700L..1_200L)
        assertEquals(1_000L, first.revealStartElapsedRealtime)

        viewModel.onAsk()
        assertEquals(first.requestId, viewModel.uiState.value.requestId)
        viewModel.onRevealFinished(first.requestId + 1)
        assertEquals(YesNoPhase.Revealing, viewModel.uiState.value.phase)

        viewModel.onRevealFinished(first.requestId)
        advanceUntilIdle()
        assertEquals(YesNoPhase.Revealed, viewModel.uiState.value.phase)
        assertEquals(first.answer, viewModel.uiState.value.answer)
    }

    @Test
    fun `reset returns to idle and next request gets a new id`() = runTest(mainDispatcher.dispatcher) {
        val viewModel = YesNoViewModel(FakeEntropySource(), MonotonicClock { 1_000L })
        viewModel.onAsk()
        val requestId = viewModel.uiState.value.requestId
        viewModel.onRevealFinished(requestId)
        advanceUntilIdle()
        viewModel.onReset()
        assertEquals(YesNoPhase.Idle, viewModel.uiState.value.phase)
        assertNull(viewModel.uiState.value.answer)
        viewModel.onAsk()
        assertNotEquals(requestId, viewModel.uiState.value.requestId)
    }

    @Test
    fun `reset cannot interrupt an active reveal`() = runTest(mainDispatcher.dispatcher) {
        val viewModel = YesNoViewModel(FakeEntropySource(), MonotonicClock { 1_000L })
        viewModel.onAsk()
        val requestId = viewModel.uiState.value.requestId
        viewModel.onReset()
        assertEquals(YesNoPhase.Revealing, viewModel.uiState.value.phase)
        assertEquals(requestId, viewModel.uiState.value.requestId)
    }

}
