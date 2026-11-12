package com.ogos.apprandomizador.modes.ui.oracle

import com.ogos.apprandomizador.core.ai.domain.AiConfig
import com.ogos.apprandomizador.core.ai.domain.AiSettings
import com.ogos.apprandomizador.core.ai.domain.AiSettingsRepository
import com.ogos.apprandomizador.core.ai.domain.LlmClient
import com.ogos.apprandomizador.core.data.IRouletteSource
import com.ogos.apprandomizador.core.database.OracleModeStats
import com.ogos.apprandomizador.core.entropy.EntropySource
import com.ogos.apprandomizador.core.model.ItemList
import com.ogos.apprandomizador.core.model.RaffleItem
import com.ogos.apprandomizador.core.outcome.IOracleRepository
import com.ogos.apprandomizador.core.outcome.StoredOutcome
import com.ogos.apprandomizador.core.ui.ai.AiAction
import com.ogos.apprandomizador.modes.domain.OracleAiService
import com.ogos.apprandomizador.modes.domain.OracleSource
import com.ogos.apprandomizador.modes.ui.ai.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OracleViewModelTest {
    @get:Rule val mainDispatcher = MainDispatcherRule()

    private class FakeEntropySource : EntropySource {
        private var seed = 0
        override fun nextSeed(): ByteArray = ByteArray(32) { (it + seed++).toByte() }
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

    private class FakeRouletteSource : IRouletteSource {
        val lists = MutableStateFlow<List<ItemList>>(emptyList())
        override val allItems: Flow<List<ItemList>> = lists
        override fun getItemFlow(id: Long): Flow<ItemList?> = lists.map { all -> all.firstOrNull { it.id == id } }
    }

    private class FakeSettings : AiSettingsRepository {
        override val settings = MutableStateFlow(
            AiSettings(AiConfig("https://api.openai.com/v1", "model"), true, "api.openai.com")
        )
        override suspend fun save(config: AiConfig, apiKey: String?) = Unit
        override suspend fun removeCredentials() = Unit
        override suspend fun apiKey(): String? = "fake-key"
        override suspend fun saveConsent(authority: String) = Unit
    }

    private class CountingClient(private val reply: String) : LlmClient {
        var calls = 0
        override suspend fun listModels(baseUrl: String, apiKey: String): List<String> = emptyList()
        override suspend fun ping(baseUrl: String, apiKey: String, model: String) = Unit
        override suspend fun complete(baseUrl: String, apiKey: String, model: String, prompt: String): String {
            calls++
            return reply
        }
    }

    @Test fun `pergunta revela e so o pedido atual termina a revelacao`() = runTest(mainDispatcher.dispatcher) {
        val vm = OracleViewModel(FakeEntropySource())
        assertEquals(OraclePhase.Idle, vm.uiState.value.phase)
        vm.onAsk()
        val first = vm.uiState.value
        assertEquals(OraclePhase.Revealing, first.phase)
        assertNotNull(first.answer)
        assertTrue(first.durationMillis in 1_500L..1_900L)

        vm.onAsk()
        assertEquals(first.requestId, vm.uiState.value.requestId)
        vm.onRevealFinished(first.requestId + 1)
        assertEquals(OraclePhase.Revealing, vm.uiState.value.phase)

        vm.onRevealFinished(first.requestId)
        advanceUntilIdle()
        assertEquals(OraclePhase.Revealed, vm.uiState.value.phase)
        assertEquals(first.answer, vm.uiState.value.answer)
    }

    @Test fun `a resposta so fica visivel depois da revelacao`() = runTest(mainDispatcher.dispatcher) {
        val vm = OracleViewModel(FakeEntropySource())
        vm.onAsk()
        assertNotNull(vm.uiState.value.answer)
        assertNull(vm.uiState.value.revealedAnswer)
        assertNull(vm.uiState.value.reflection)

        vm.onRevealFinished(vm.uiState.value.requestId)
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.revealedAnswer)
        assertNotNull(vm.uiState.value.reflection)
    }

    @Test fun `registra o uso no modo yes_no com a resposta e sem tema no classico`() = runTest(mainDispatcher.dispatcher) {
        val repository = FakeOracleRepository()
        val vm = OracleViewModel(FakeEntropySource(), oracleRepository = repository)
        vm.onAsk()
        vm.onRevealFinished(vm.uiState.value.requestId)
        advanceUntilIdle()
        assertEquals("yes_no", repository.modeId)
        val outcome = repository.outcome as StoredOutcome.Oracle
        assertEquals(vm.uiState.value.answer?.name, outcome.answer)
        assertNull(outcome.sourceTopic)
    }

    @Test fun `falha ao gravar nao esconde a resposta`() = runTest(mainDispatcher.dispatcher) {
        val vm = OracleViewModel(FakeEntropySource(), oracleRepository = FakeOracleRepository(fail = true))
        vm.onAsk()
        vm.onRevealFinished(vm.uiState.value.requestId)
        advanceUntilIdle()
        assertEquals(OraclePhase.Revealed, vm.uiState.value.phase)
        assertNotNull(vm.uiState.value.answer)
    }

    @Test fun `nova pergunta so volta a Idle depois da revelacao e limpa a pergunta`() = runTest(mainDispatcher.dispatcher) {
        val vm = OracleViewModel(FakeEntropySource())
        vm.onQuestionChange("Devo mudar?")
        vm.onAsk()
        vm.onReset()
        assertEquals(OraclePhase.Revealing, vm.uiState.value.phase)

        vm.onRevealFinished(vm.uiState.value.requestId)
        advanceUntilIdle()
        vm.onReset()
        assertEquals(OraclePhase.Idle, vm.uiState.value.phase)
        assertEquals("", vm.uiState.value.question)
        assertNull(vm.uiState.value.answer)
    }

    @Test fun `ditado por voz acrescenta a pergunta`() = runTest(mainDispatcher.dispatcher) {
        val vm = OracleViewModel(FakeEntropySource())
        vm.onSpokenQuestion("primeira parte")
        vm.onSpokenQuestion("segunda")
        assertEquals("primeira parte segunda", vm.uiState.value.question)
    }

    @Test fun `vinculada a roleta, o sorteio usa somente os itens dela`() = runTest(mainDispatcher.dispatcher) {
        val roulettes = FakeRouletteSource()
        roulettes.lists.value = listOf(
            ItemList(id = 5L, topic = "Carreira", items = listOf(RaffleItem("Vai", 1L), RaffleItem("Espera", 2L)))
        )
        val vm = OracleViewModel(FakeEntropySource(), rouletteSource = roulettes)
        advanceUntilIdle()
        vm.onLinkRoulette(5L)
        advanceUntilIdle()
        assertEquals("Carreira", vm.uiState.value.source.topic)
        assertEquals(5L, vm.uiState.value.linkedRouletteId)

        vm.onAsk()
        assertTrue(vm.uiState.value.answer!!.name in setOf("Vai", "Espera"))
    }

    @Test fun `roleta vazia desabilita a pergunta e nao sorteia`() = runTest(mainDispatcher.dispatcher) {
        val roulettes = FakeRouletteSource()
        roulettes.lists.value = listOf(ItemList(id = 6L, topic = "Vazia", items = emptyList()))
        val vm = OracleViewModel(FakeEntropySource(), rouletteSource = roulettes)
        advanceUntilIdle()
        vm.onLinkRoulette(6L)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.canAsk)
        vm.onAsk()
        assertEquals(OraclePhase.Idle, vm.uiState.value.phase)
    }

    @Test fun `roleta apagada volta ao classico e avisa`() = runTest(mainDispatcher.dispatcher) {
        val roulettes = FakeRouletteSource()
        roulettes.lists.value = listOf(ItemList(id = 5L, topic = "Carreira", items = listOf(RaffleItem("Vai", 1L))))
        val vm = OracleViewModel(FakeEntropySource(), rouletteSource = roulettes)
        advanceUntilIdle()
        vm.onLinkRoulette(5L)
        advanceUntilIdle()

        roulettes.lists.value = emptyList()
        advanceUntilIdle()
        assertEquals(OracleSource.CLASSIC, vm.uiState.value.source)
        assertEquals(OracleSourceNotice.RouletteRemoved, vm.uiState.value.sourceNotice)
        assertNull(vm.uiState.value.linkedRouletteId)
    }

    @Test fun `trocar a fonte e bloqueado durante a leitura`() = runTest(mainDispatcher.dispatcher) {
        val roulettes = FakeRouletteSource()
        roulettes.lists.value = listOf(ItemList(id = 5L, topic = "Carreira", items = listOf(RaffleItem("Vai", 1L))))
        val vm = OracleViewModel(FakeEntropySource(), rouletteSource = roulettes)
        advanceUntilIdle()
        vm.onAsk()
        vm.onLinkRoulette(5L)
        advanceUntilIdle()
        assertEquals(OracleSource.CLASSIC, vm.uiState.value.source)
    }

    @Test fun `com IA ativa, a leitura sai sozinha uma unica vez e a reflexao offline sai com ela`() = runTest(mainDispatcher.dispatcher) {
        val settings = FakeSettings()
        val client = CountingClient("""{"title":"Título","essence":"Abertura","reading":"Leitura","affirmation":"Confie"}""")
        val service = OracleAiService(client, settings, retryDelaysMillis = emptyList())
        val vm = OracleViewModel(FakeEntropySource(), aiSettingsRepository = settings, aiService = service)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.aiEnabled)

        vm.onAsk()
        vm.onRevealFinished(vm.uiState.value.requestId)
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.aiReading)
        assertEquals(1, client.calls)
        // A leitura encerra as ações de IA desta tiragem.
        assertEquals(AiAction.None, vm.uiState.value.aiAction)
        // A afirmação da IA é a frase final: a reflexão offline não compete com ela.
        assertNull(vm.uiState.value.visibleReflection)

        vm.onDeepenWithAi()
        advanceUntilIdle()
        assertEquals(1, client.calls)
    }

    @Test fun `sem IA, a reflexao offline fica visivel`() = runTest(mainDispatcher.dispatcher) {
        val vm = OracleViewModel(FakeEntropySource())
        vm.onAsk()
        vm.onRevealFinished(vm.uiState.value.requestId)
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.visibleReflection)
    }
}
