package com.ogos.apprandomizador.features.fingers.ui

import androidx.compose.ui.geometry.Offset
import com.ogos.apprandomizador.core.database.OracleModeStats
import com.ogos.apprandomizador.core.entropy.EntropySource
import com.ogos.apprandomizador.core.outcome.IOracleRepository
import com.ogos.apprandomizador.core.outcome.StoredOutcome
import com.ogos.apprandomizador.core.testing.MainDispatcherRule
import com.ogos.apprandomizador.features.fingers.domain.FingersPalette
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FingersViewModelTest {
    @get:Rule val mainDispatcher = MainDispatcherRule()

    private class FakeEntropy : EntropySource {
        private var calls = 0
        override fun nextSeed(): ByteArray =
            ByteArray(32) { (it + calls).toByte() }.also { calls++ }
    }

    private class FakeRepo : IOracleRepository {
        val recorded = mutableListOf<Pair<String, StoredOutcome>>()
        override val stats: Flow<List<OracleModeStats>> = emptyFlow()
        override suspend fun record(modeId: String, outcome: StoredOutcome) {
            recorded += modeId to outcome
        }
    }

    private class FailingRepo : IOracleRepository {
        override val stats: Flow<List<OracleModeStats>> = emptyFlow()
        override suspend fun record(modeId: String, outcome: StoredOutcome) {
            error("disco cheio")
        }
    }

    private fun FingersViewModel.down(id: Long) = onFingerDown(id, Offset(id * 100f, 300f))

    @Test fun `um dedo fica em Idle sem contagem`() = runTest(mainDispatcher.dispatcher) {
        val vm = FingersViewModel(FakeEntropy())
        vm.down(1L)
        assertEquals(FingersPhase.Idle, vm.uiState.value.phase)
        assertEquals(1, vm.uiState.value.fingers.size)
    }

    @Test fun `dois dedos iniciam contagem e revelam apos 3 segundos`() = runTest(mainDispatcher.dispatcher) {
        val repo = FakeRepo()
        val vm = FingersViewModel(FakeEntropy(), repo)
        vm.down(1L)
        vm.down(2L)
        assertEquals(FingersPhase.Counting, vm.uiState.value.phase)

        advanceTimeBy(3_100)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(FingersPhase.Revealed, state.phase)
        assertNotNull(state.winner)
        assertNotNull(state.winnerDirection)
        assertEquals(1, repo.recorded.size)
        val (modeId, outcome) = repo.recorded.single()
        assertEquals("fingers", modeId)
        assertTrue(outcome is StoredOutcome.Fingers)
        assertEquals(2, (outcome as StoredOutcome.Fingers).fingerCount)
    }

    @Test fun `contagem desce de 3 ate 1`() = runTest(mainDispatcher.dispatcher) {
        val vm = FingersViewModel(FakeEntropy())
        vm.down(1L)
        vm.down(2L)
        assertEquals(3, vm.uiState.value.countdown)

        advanceTimeBy(1_500)
        assertEquals(2, vm.uiState.value.countdown)

        advanceTimeBy(1_000)
        assertEquals(1, vm.uiState.value.countdown)
    }

    @Test fun `mover um dedo nao reinicia a contagem`() = runTest(mainDispatcher.dispatcher) {
        val vm = FingersViewModel(FakeEntropy())
        vm.down(1L)
        vm.down(2L)
        advanceTimeBy(2_000)

        vm.onFingerMove(1L, Offset(60f, 60f))
        advanceTimeBy(1_200)
        advanceUntilIdle()

        assertEquals(FingersPhase.Revealed, vm.uiState.value.phase)
    }

    @Test fun `adicionar dedo reinicia a contagem`() = runTest(mainDispatcher.dispatcher) {
        val vm = FingersViewModel(FakeEntropy())
        vm.down(1L)
        vm.down(2L)
        advanceTimeBy(2_000)

        // O ciclo novo começa em t=2000 e revela em t=5000, não em t=3000.
        vm.down(3L)
        advanceTimeBy(1_200)
        assertEquals(FingersPhase.Counting, vm.uiState.value.phase)

        advanceTimeBy(2_000)
        advanceUntilIdle()
        assertEquals(FingersPhase.Revealed, vm.uiState.value.phase)
    }

    @Test fun `soltar dedo ate abaixo de 2 volta para Idle e nao revela`() = runTest(mainDispatcher.dispatcher) {
        val repo = FakeRepo()
        val vm = FingersViewModel(FakeEntropy(), repo)
        vm.down(1L)
        vm.down(2L)
        advanceTimeBy(1_000)

        vm.onFingerUp(1L)
        assertEquals(FingersPhase.Idle, vm.uiState.value.phase)

        advanceTimeBy(5_000)
        advanceUntilIdle()
        assertEquals(FingersPhase.Idle, vm.uiState.value.phase)
        assertTrue(repo.recorded.isEmpty())
    }

    @Test fun `dedos simultaneos tem cores todas distintas`() = runTest(mainDispatcher.dispatcher) {
        val vm = FingersViewModel(FakeEntropy())
        (1L..5L).forEach { vm.down(it) }
        val colors = vm.uiState.value.fingers.map { it.color }
        assertEquals(5, colors.toSet().size)
    }

    @Test fun `dedo novo depois de um que saiu nao recebe a mesma cor`() = runTest(mainDispatcher.dispatcher) {
        val vm = FingersViewModel(FakeEntropy())
        vm.down(1L)
        vm.down(2L)
        val removedColor = vm.uiState.value.fingers.first().color

        vm.onFingerUp(1L)
        vm.down(3L)

        val newestColor = vm.uiState.value.fingers.last().color
        assertNotEquals(removedColor, newestColor)
    }

    @Test fun `reset devolve a paleta ao inicio`() = runTest(mainDispatcher.dispatcher) {
        val vm = FingersViewModel(FakeEntropy())
        vm.down(1L)
        vm.down(2L)
        advanceTimeBy(3_100)
        advanceUntilIdle()

        vm.onReset()
        vm.down(9L)
        assertEquals(FingersPalette.colorAt(0), vm.uiState.value.fingers.single().color)
    }

    @Test fun `reset limpa tudo e volta a Idle`() = runTest(mainDispatcher.dispatcher) {
        val vm = FingersViewModel(FakeEntropy())
        vm.down(1L)
        vm.down(2L)
        advanceTimeBy(3_100)
        advanceUntilIdle()
        assertEquals(FingersPhase.Revealed, vm.uiState.value.phase)

        vm.onReset()

        assertEquals(FingersPhase.Idle, vm.uiState.value.phase)
        assertTrue(vm.uiState.value.fingers.isEmpty())
        assertNull(vm.uiState.value.winnerId)
    }

    @Test fun `reset no meio da contagem cancela o sorteio`() = runTest(mainDispatcher.dispatcher) {
        val repo = FakeRepo()
        val vm = FingersViewModel(FakeEntropy(), repo)
        vm.down(1L)
        vm.down(2L)
        advanceTimeBy(1_000)

        vm.onReset()
        advanceTimeBy(5_000)
        advanceUntilIdle()

        assertEquals(FingersPhase.Idle, vm.uiState.value.phase)
        assertTrue(repo.recorded.isEmpty())
    }

    @Test fun `toques e movimentos apos revelar sao ignorados`() = runTest(mainDispatcher.dispatcher) {
        val vm = FingersViewModel(FakeEntropy())
        vm.down(1L)
        vm.down(2L)
        advanceTimeBy(3_100)
        advanceUntilIdle()
        val snapshot = vm.uiState.value

        vm.down(3L)
        vm.onFingerMove(1L, Offset(0f, 0f))
        vm.onFingerUp(1L)

        assertEquals(snapshot, vm.uiState.value)
    }

    @Test fun `falha ao gravar nao esconde o resultado`() = runTest(mainDispatcher.dispatcher) {
        val vm = FingersViewModel(FakeEntropy(), FailingRepo())
        vm.down(1L)
        vm.down(2L)
        advanceTimeBy(3_100)
        advanceUntilIdle()

        assertEquals(FingersPhase.Revealed, vm.uiState.value.phase)
        assertNotNull(vm.uiState.value.winner)
    }
}
