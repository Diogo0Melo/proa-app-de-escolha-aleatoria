package com.ogos.apprandomizador.modes.ui.yesno

import com.ogos.apprandomizador.data.database.OracleModeStats
import com.ogos.apprandomizador.data.repository.IOracleRepository
import com.ogos.apprandomizador.modes.domain.StoredOutcome
import com.ogos.apprandomizador.modes.ui.ai.MainDispatcherRule
import com.ogos.apprandomizador.viewmodel.EntropySource
import com.ogos.apprandomizador.viewmodel.MonotonicClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class YesNoPersistenceTest {
    @get:Rule val mainDispatcher = MainDispatcherRule()

    @Test fun `records the decided answer when reveal finishes`() = runTest(mainDispatcher.dispatcher) {
        val repository = FakeOracleRepository()
        val vm = YesNoViewModel(
            entropySource = EntropySource { ByteArray(32) { it.toByte() } },
            clock = MonotonicClock { 500L },
            oracleRepository = repository
        )
        vm.onAsk()
        val requestId = vm.uiState.value.requestId
        vm.onRevealFinished(requestId)
        advanceUntilIdle()
        assertEquals("yes_no", repository.modeId)
        assertTrue((repository.outcome as StoredOutcome.YesNo).answer in setOf("YES", "NO"))
        assertEquals(YesNoPhase.Revealed, vm.uiState.value.phase)
    }

    private class FakeOracleRepository : IOracleRepository {
        var modeId: String? = null
        var outcome: StoredOutcome? = null
        override val stats: Flow<List<OracleModeStats>> = emptyFlow()
        override suspend fun record(modeId: String, outcome: StoredOutcome) {
            this.modeId = modeId
            this.outcome = outcome
        }
    }
}
