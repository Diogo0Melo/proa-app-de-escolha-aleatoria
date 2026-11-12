package com.ogos.apprandomizador.features.tarot.domain

import com.ogos.apprandomizador.core.ai.domain.AiServicePolicy
import com.ogos.apprandomizador.features.tarot.domain.AiReadingService
import com.ogos.apprandomizador.core.ai.domain.AiConfig
import com.ogos.apprandomizador.core.ai.domain.AiSettings
import com.ogos.apprandomizador.core.ai.domain.AiSettingsRepository
import com.ogos.apprandomizador.core.ai.domain.LlmClient
import com.ogos.apprandomizador.core.ai.domain.LlmHttpException
import javax.net.ssl.SSLException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class AiReadingServiceTest {
    private val card = TarotCard("00", 0, "O Louco", CardMeaning(listOf("começo"), "essência"), CardMeaning(listOf("medo"), "essência invertida"))
    private val draw = listOf(DrawnCard(card, false, SpreadPosition.SINGLE))
    private val validJson = """{"summary":"Resumo","sections":[{"position":"SINGLE","text":"Texto"}]}"""

    @Test fun `erro transitorio e retentado ate obter sucesso`() = runTest {
        val client = ScriptedClient(LlmHttpException(503, "busy"), validJson)
        val attempts = mutableListOf<Int>()
        val result = service(client).deepen("Pergunta", draw) { attempts += it }
        assertTrue(result is AiReadingResult.Success)
        assertEquals(2, client.calls)
        assertEquals(listOf(2), attempts)
    }

    @Test fun `limite de taxa 429 e retentado`() = runTest {
        val client = ScriptedClient(LlmHttpException(429, "rate"), validJson)
        assertTrue(service(client).deepen(null, draw) is AiReadingResult.Success)
        assertEquals(2, client.calls)
    }

    @Test fun `chave recusada nao e retentada`() = runTest {
        val client = ScriptedClient(LlmHttpException(401, "unauthorized"), validJson)
        val result = service(client).deepen(null, draw) as AiReadingResult.Failure
        assertEquals("A chave foi recusada pelo provedor.", result.reason)
        assertEquals(1, client.calls)
    }

    @Test fun `falha de certificado nao e retentada`() = runTest {
        val client = ScriptedClient(SSLException("bad cert"), validJson)
        assertTrue(service(client).deepen(null, draw) is AiReadingResult.Failure)
        assertEquals(1, client.calls)
    }

    @Test fun `formato invalido nao e retentado e nao vira leitura`() = runTest {
        val client = ScriptedClient("texto livre sem json")
        val result = service(client).deepen(null, draw) as AiReadingResult.Failure
        assertEquals(AiServicePolicy.INVALID_RESPONSE, result.reason)
        assertEquals(1, client.calls)
    }

    @Test fun `desiste depois de esgotar as tentativas`() = runTest {
        val client = ScriptedClient(LlmHttpException(500, "down"), LlmHttpException(500, "down"), LlmHttpException(500, "down"))
        assertTrue(service(client).deepen(null, draw) is AiReadingResult.Failure)
        assertEquals(3, client.calls)
    }

    @Test fun `sem configuracao nao chama o provedor`() = runTest {
        val client = ScriptedClient(validJson)
        val result = service(client, configured = false).deepen(null, draw) as AiReadingResult.Failure
        assertEquals(AiServicePolicy.NOT_CONFIGURED, result.reason)
        assertEquals(0, client.calls)
    }

    @Test fun `orcamento total estourado vira mensagem de tempo`() = runTest {
        val slow = object : LlmClient by ScriptedClient() {
            override suspend fun complete(baseUrl: String, apiKey: String, model: String, prompt: String): String {
                delay(60_000)
                return validJson
            }
        }
        val service = AiReadingService(slow, FakeSettings(), retryDelaysMillis = emptyList(), totalBudgetMillis = 100L)
        val result = service.deepen(null, draw) as AiReadingResult.Failure
        assertEquals(AiServicePolicy.TIMEOUT_MESSAGE, result.reason)
    }

    private fun service(client: LlmClient, configured: Boolean = true) =
        AiReadingService(client, FakeSettings(configured), retryDelaysMillis = listOf(1L, 1L))

    /** Respostas roteirizadas: um Throwable é lançado, uma String é devolvida. */
    private class ScriptedClient(vararg responses: Any) : LlmClient {
        private val queue = ArrayDeque(responses.toList())
        var calls = 0
            private set

        override suspend fun listModels(baseUrl: String, apiKey: String): List<String> = emptyList()
        override suspend fun ping(baseUrl: String, apiKey: String, model: String) = Unit
        override suspend fun complete(baseUrl: String, apiKey: String, model: String, prompt: String): String {
            calls++
            return when (val next = queue.removeFirst()) {
                is Throwable -> throw next
                else -> next as String
            }
        }
    }

    private class FakeSettings(configured: Boolean = true) : AiSettingsRepository {
        override val settings = MutableStateFlow(
            if (configured) AiSettings(AiConfig("https://api.openai.com/v1", "model"), true, "api.openai.com")
            else AiSettings()
        )
        override suspend fun save(config: AiConfig, apiKey: String?) = Unit
        override suspend fun removeCredentials() = Unit
        override suspend fun apiKey(): String? = "fake-key"
        override suspend fun saveConsent(authority: String) = Unit
    }
}
