package com.ogos.apprandomizador.modes.ui.ai

import com.ogos.apprandomizador.modes.domain.LlmHttpException
import com.ogos.apprandomizador.modes.domain.AiConfig
import com.ogos.apprandomizador.modes.domain.AiSettings
import com.ogos.apprandomizador.modes.domain.AiSettingsRepository
import com.ogos.apprandomizador.modes.domain.LlmClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class AiSettingsViewModelTest {
    @get:Rule val mainDispatcher = MainDispatcherRule()

    @Test fun `connection test lists models and stores masked key type`() = runTest(mainDispatcher.dispatcher) {
        val repository = FakeSettingsRepository()
        val client = FakeLlmClient(models = listOf("chat-model", "text-embedding-model"))
        val vm = AiSettingsViewModel(repository, client)
        advanceUntilIdle()
        vm.updateApiKey("my-secret")
        vm.updateModel("chat-model")
        vm.testConnection()
        advanceUntilIdle()
        assertTrue(vm.state.value.connection is ConnectionState.Success)
        assertEquals(listOf("chat-model"), vm.visibleModels())
        assertEquals("Secret(••••••••)", vm.state.value.apiKeyInput.toString())
    }

    @Test fun `404 from models endpoint falls back to model ping`() = runTest(mainDispatcher.dispatcher) {
        val repository = FakeSettingsRepository()
        val client = FakeLlmClient(failListModels = 404)
        val vm = AiSettingsViewModel(repository, client)
        advanceUntilIdle()
        vm.updateApiKey("key")
        vm.updateModel("model-x")
        vm.testConnection()
        advanceUntilIdle()
        assertTrue(vm.state.value.connection is ConnectionState.Success)
        assertEquals(1, client.pingCalls)
    }

    @Test fun `changing provider authority requires fresh consent`() = runTest(mainDispatcher.dispatcher) {
        val repository = FakeSettingsRepository()
        val client = FakeLlmClient(models = listOf("model"))
        val vm = AiSettingsViewModel(repository, client)
        advanceUntilIdle()
        vm.updateBaseUrl("https://openrouter.ai/api/v1")
        vm.updateApiKey("key")
        vm.testConnection()
        advanceUntilIdle()
        assertEquals("Aceite o envio de dados para testar a conexão.", vm.state.value.message)
        assertEquals(0, client.pingCalls)
    }

    @Test fun `invalid key error is presented`() = runTest(mainDispatcher.dispatcher) {
        val repository = FakeSettingsRepository()
        val vm = AiSettingsViewModel(repository, FakeLlmClient(failListModels = 401))
        advanceUntilIdle()
        vm.updateApiKey("bad")
        vm.testConnection()
        advanceUntilIdle()
        assertEquals("A chave foi recusada pelo provedor.", (vm.state.value.connection as ConnectionState.Error).message)
    }

    private class FakeSettingsRepository : AiSettingsRepository {
        private val mutable = MutableStateFlow(AiSettings(consentedAuthority = "api.openai.com"))
        override val settings: Flow<AiSettings> = mutable
        override suspend fun save(config: AiConfig, apiKey: String?) { mutable.value = AiSettings(config, apiKey != null, "api.openai.com") }
        override suspend fun removeCredentials() { mutable.value = AiSettings() }
        override suspend fun apiKey(): String? = null
        override suspend fun saveConsent(authority: String) { mutable.value = mutable.value.copy(consentedAuthority = authority) }
    }

    private class FakeLlmClient(
        private val models: List<String> = emptyList(),
        private val failListModels: Int? = null
    ) : LlmClient {
        var pingCalls = 0
        override suspend fun listModels(baseUrl: String, apiKey: String): List<String> {
            failListModels?.let { throw LlmHttpException(it, "test") }
            return models
        }
        override suspend fun ping(baseUrl: String, apiKey: String, model: String) { pingCalls++ }
        override suspend fun complete(baseUrl: String, apiKey: String, model: String, prompt: String): String = "{}"
    }
}
