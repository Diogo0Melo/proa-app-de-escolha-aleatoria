package com.ogos.apprandomizador.features.oracle.domain

import com.ogos.apprandomizador.core.ai.domain.AiServicePolicy
import com.ogos.apprandomizador.core.ai.domain.AiConfig
import com.ogos.apprandomizador.core.ai.domain.AiSettings
import com.ogos.apprandomizador.core.ai.domain.AiSettingsRepository
import com.ogos.apprandomizador.core.ai.domain.LlmClient
import com.ogos.apprandomizador.core.ai.domain.LlmHttpException
import com.ogos.apprandomizador.core.model.RaffleItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OracleAiServiceTest {
    private class FakeSettings(enabled: Boolean = true) : AiSettingsRepository {
        override val settings = MutableStateFlow(
            AiSettings(AiConfig("https://api.openai.com/v1", "model", enabled), true, "api.openai.com")
        )
        override suspend fun save(config: AiConfig, apiKey: String?) = Unit
        override suspend fun removeCredentials() = Unit
        override suspend fun apiKey(): String? = "fake-key"
        override suspend fun saveConsent(authority: String) = Unit
    }

    /** Cada chamada usa o próximo passo; o último passo se repete. */
    private class ScriptedClient(private val steps: List<() -> String>) : LlmClient {
        var calls = 0
            private set
        override suspend fun listModels(baseUrl: String, apiKey: String): List<String> = emptyList()
        override suspend fun ping(baseUrl: String, apiKey: String, model: String) = Unit
        override suspend fun complete(baseUrl: String, apiKey: String, model: String, prompt: String): String {
            val step = steps[minOf(calls, steps.lastIndex)]
            calls++
            return step()
        }
    }

    private val answer = RaffleItem("SIM", 0xFF009600L)
    private val validJson = """{"title":"T","reading":"Leitura","affirmation":"A"}"""

    @Test fun `resposta valida vira leitura e informa o modelo`() = runTest {
        val service = OracleAiService(ScriptedClient(listOf<() -> String>({ validJson })), FakeSettings(), retryDelaysMillis = emptyList())
        val result = service.deepen("Devo ir?", answer, null)
        val success = result as OracleAiResult.Success
        assertEquals("Leitura", success.reading.reading)
        assertEquals("model", success.modelId)
    }

    @Test fun `erro transitorio e retentado e a tentativa e informada`() = runTest {
        val client = ScriptedClient(listOf<() -> String>({ throw LlmHttpException(503, "indisponível") }, { validJson }))
        val attempts = mutableListOf<Int>()
        val service = OracleAiService(client, FakeSettings(), retryDelaysMillis = listOf(0L))
        val result = service.deepen("Devo ir?", answer, null, onRetry = { attempt -> attempts.add(attempt) })
        assertTrue(result is OracleAiResult.Success)
        assertEquals(2, client.calls)
        assertEquals(listOf(2), attempts)
    }

    @Test fun `erro permanente nao e retentado`() = runTest {
        val client = ScriptedClient(listOf<() -> String>({ throw LlmHttpException(401, "chave inválida") }))
        val service = OracleAiService(client, FakeSettings(), retryDelaysMillis = listOf(0L, 0L))
        val result = service.deepen(null, answer, null)
        assertTrue(result is OracleAiResult.Failure)
        assertEquals(1, client.calls)
    }

    @Test fun `sem IA configurada nao chama o provedor`() = runTest {
        val client = ScriptedClient(listOf<() -> String>({ validJson }))
        val service = OracleAiService(client, FakeSettings(enabled = false), retryDelaysMillis = emptyList())
        val failure = service.deepen(null, answer, null) as OracleAiResult.Failure
        assertEquals(AiServicePolicy.NOT_CONFIGURED, failure.reason)
        assertEquals(0, client.calls)
    }

    @Test fun `resposta fora do formato vira falha`() = runTest {
        val client = ScriptedClient(listOf<() -> String>({ "texto sem json" }))
        val service = OracleAiService(client, FakeSettings(), retryDelaysMillis = emptyList())
        val failure = service.deepen(null, answer, null) as OracleAiResult.Failure
        assertEquals(AiServicePolicy.INVALID_RESPONSE, failure.reason)
    }
}
