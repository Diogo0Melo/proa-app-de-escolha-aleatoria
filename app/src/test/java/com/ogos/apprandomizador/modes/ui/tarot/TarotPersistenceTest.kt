package com.ogos.apprandomizador.modes.ui.tarot

import com.ogos.apprandomizador.data.database.OracleModeStats
import com.ogos.apprandomizador.data.repository.IOracleRepository
import com.ogos.apprandomizador.modes.data.FullTarotDeck
import com.ogos.apprandomizador.modes.domain.StoredOutcome
import com.ogos.apprandomizador.modes.domain.AiConfig
import com.ogos.apprandomizador.modes.domain.AiReadingService
import com.ogos.apprandomizador.modes.domain.AiSettings
import com.ogos.apprandomizador.modes.domain.AiSettingsRepository
import com.ogos.apprandomizador.modes.domain.LlmClient
import com.ogos.apprandomizador.modes.domain.Reading
import kotlinx.coroutines.flow.MutableStateFlow
import com.ogos.apprandomizador.viewmodel.EntropySource
import com.ogos.apprandomizador.modes.ui.ai.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class TarotPersistenceTest {
    @get:Rule val mainDispatcher = MainDispatcherRule()

    @Test fun `records only drawn card ids and publishes offline reading`() = runTest(mainDispatcher.dispatcher) {
        val repository = FakeOracleRepository()
        val vm = TarotViewModel(
            entropySource = EntropySource { ByteArray(32) { it.toByte() } },
            deckProvider = FullTarotDeck,
            oracleRepository = repository
        )
        vm.onDraw()
        val requestId = vm.uiState.value.requestId
        vm.onRevealFinished(requestId)
        advanceUntilIdle()
        assertEquals("tarot", repository.modeId)
        val outcome = repository.outcome as StoredOutcome.Tarot
        assertEquals(1, outcome.cards.size)
        assertTrue(outcome.cards.single().cardId.isNotBlank())
        assertEquals(TarotPhase.Revealed, vm.uiState.value.phase)
        assertNotNull(vm.uiState.value.reading)
    }

    @Test fun `database failure does not hide offline interpretation`() = runTest(mainDispatcher.dispatcher) {
        val repository = FakeOracleRepository(fail = true)
        val vm = TarotViewModel(
            entropySource = EntropySource { ByteArray(32) { (it + 1).toByte() } },
            deckProvider = FullTarotDeck,
            oracleRepository = repository
        )
        vm.onDraw()
        vm.onRevealFinished(vm.uiState.value.requestId)
        advanceUntilIdle()
        assertEquals(TarotPhase.Revealed, vm.uiState.value.phase)
        assertNotNull(vm.uiState.value.reading)
    }


    @Test fun `AI failure leaves offline reading visible`() = runTest(mainDispatcher.dispatcher) {
        val settings = FakeAiSettingsRepository()
        val aiService = AiReadingService(object : LlmClient {
            override suspend fun listModels(baseUrl: String, apiKey: String) = emptyList<String>()
            override suspend fun ping(baseUrl: String, apiKey: String, model: String) = Unit
            override suspend fun complete(baseUrl: String, apiKey: String, model: String, prompt: String): String = error("provider unavailable")
        }, settings)
        val vm = TarotViewModel(
            entropySource = EntropySource { ByteArray(32) { (it + 2).toByte() } },
            deckProvider = FullTarotDeck,
            aiReadingService = aiService,
            aiSettingsRepository = settings
        )
        advanceUntilIdle()
        vm.onDraw()
        vm.onRevealFinished(vm.uiState.value.requestId)
        advanceUntilIdle()
        val offlineReading = vm.uiState.value.reading
        assertNotNull(offlineReading)
        vm.onDeepenWithAi()
        advanceUntilIdle()
        assertEquals(TarotPhase.Revealed, vm.uiState.value.phase)
        assertEquals(offlineReading, vm.uiState.value.reading)
        assertNotNull(vm.uiState.value.aiError)
    }

    @Test fun `disabling AI clears its error and keeps the offline reading`() = runTest(mainDispatcher.dispatcher) {
        val settings = FakeAiSettingsRepository()
        val aiService = AiReadingService(object : LlmClient {
            override suspend fun listModels(baseUrl: String, apiKey: String) = emptyList<String>()
            override suspend fun ping(baseUrl: String, apiKey: String, model: String) = Unit
            override suspend fun complete(baseUrl: String, apiKey: String, model: String, prompt: String): String = error("provider unavailable")
        }, settings)
        val vm = TarotViewModel(
            entropySource = EntropySource { ByteArray(32) { (it + 3).toByte() } },
            deckProvider = FullTarotDeck,
            aiReadingService = aiService,
            aiSettingsRepository = settings
        )
        advanceUntilIdle()
        vm.onDraw()
        vm.onRevealFinished(vm.uiState.value.requestId)
        advanceUntilIdle()
        vm.onDeepenWithAi()
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.aiError)

        settings.settings.value = AiSettings(AiConfig("https://api.openai.com/v1", "model", enabled = false), true, "api.openai.com")
        advanceUntilIdle()

        assertFalse(vm.uiState.value.aiEnabled)
        assertNull(vm.uiState.value.aiError)
        assertNull(vm.uiState.value.aiReading)
        assertNotNull(vm.uiState.value.reading)
    }

    private class FakeAiSettingsRepository : AiSettingsRepository {
        override val settings = MutableStateFlow(AiSettings(AiConfig("https://api.openai.com/v1", "model"), true, "api.openai.com"))
        override suspend fun save(config: AiConfig, apiKey: String?) = Unit
        override suspend fun removeCredentials() = Unit
        override suspend fun apiKey(): String? = "fake-key"
        override suspend fun saveConsent(authority: String) = Unit
    }

    private class FakeOracleRepository(private val fail: Boolean = false) : IOracleRepository {
        var modeId: String? = null
        var outcome: StoredOutcome? = null
        override val stats: Flow<List<OracleModeStats>> = emptyFlow()
        override suspend fun record(modeId: String, outcome: StoredOutcome) {
            if (fail) error("database unavailable")
            this.modeId = modeId
            this.outcome = outcome
        }
    }
}
