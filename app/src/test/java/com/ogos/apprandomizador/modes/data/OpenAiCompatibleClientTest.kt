package com.ogos.apprandomizador.modes.data

import com.ogos.apprandomizador.core.ai.data.OpenAiCompatibleClient
import com.ogos.apprandomizador.core.ai.domain.LlmHttpException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class OpenAiCompatibleClientTest {
    private lateinit var server: MockWebServer
    private lateinit var client: OpenAiCompatibleClient

    @Before fun setUp() { server = MockWebServer().also { it.start() }; client = OpenAiCompatibleClient(normalizeBaseUrl = { it.removeSuffix("/") }) }
    @After fun tearDown() { server.close() }

    @Test fun `lista modelos e envia bearer`() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"data":[{"id":"gpt-test"}]}"""))
        val models = client.listModels(server.url("/v1").toString(), "secret-token")
        assertEquals(listOf("gpt-test"), models)
        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("Bearer secret-token", request.headers["Authorization"])
        assertEquals("/v1/models", request.requestUrl?.encodedPath)
    }

    @Test fun `ignora itens invalidos ao listar modelos`() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"data":[1,{"id":"gpt-safe"},null]}"""))
        assertEquals(listOf("gpt-safe"), client.listModels(server.url("/v1").toString(), "key"))
    }

    @Test fun `completa leitura pelo chat completions`() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"choices":[{"message":{"content":"{\"summary\":\"Ok\"}"}}]}"""))
        val text = client.complete(server.url("/v1").toString(), "key", "model-x", "prompt")
        assertTrue(text.contains("summary"))
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/v1/chat/completions", request.requestUrl?.encodedPath)
        assertTrue(request.body?.readUtf8().orEmpty().contains("model-x"))
        assertEquals("Bearer key", request.headers["Authorization"])
    }

    @Test fun `404 em models permite ping minimo no chat completions`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(404).setBody("not found"))
        server.enqueue(MockResponse().setBody("""{"choices":[{"message":{"content":"OK"}}]}"""))
        val error = runCatching { client.listModels(server.url("/v1").toString(), "key") }.exceptionOrNull()
        assertEquals(404, (error as LlmHttpException).statusCode)
        client.ping(server.url("/v1").toString(), "key", "model-x")
        val get = server.takeRequest()
        val post = server.takeRequest()
        assertEquals("GET", get.method)
        assertEquals("POST", post.method)
        assertTrue(post.body?.readUtf8().orEmpty().contains("\"max_tokens\":1"))
    }

    @Test fun `cancelamento da coroutine cancela a chamada HTTP`() = runBlocking {
        server.enqueue(MockResponse().setBodyDelay(5, TimeUnit.SECONDS).setBody("{\"choices\":[{\"message\":{\"content\":\"OK\"}}]}"))
        val request = async(Dispatchers.IO) { client.complete(server.url("/v1").toString(), "key", "model-x", "prompt") }
        assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
        request.cancelAndJoin()
        assertTrue(request.isCancelled)
    }
}
