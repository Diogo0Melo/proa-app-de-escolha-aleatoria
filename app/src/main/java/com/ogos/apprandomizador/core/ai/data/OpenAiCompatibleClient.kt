package com.ogos.apprandomizador.core.ai.data

import com.ogos.apprandomizador.core.ai.domain.BaseUrlNormalizer
import com.ogos.apprandomizador.core.ai.domain.LlmClient
import com.ogos.apprandomizador.core.ai.domain.LlmHttpException
import com.ogos.apprandomizador.core.ai.domain.LlmResponseFormatException
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

@OptIn(ExperimentalCoroutinesApi::class, InternalCoroutinesApi::class)
class OpenAiCompatibleClient(
    private val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .callTimeout(65, TimeUnit.SECONDS)
        .followRedirects(false)
        .followSslRedirects(false)
        .build(),
    private val normalizeBaseUrl: (String) -> String = BaseUrlNormalizer::normalize
) : LlmClient {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun listModels(baseUrl: String, apiKey: String): List<String> {
        val root = json.parseToJsonElement(request("GET", endpoint(baseUrl, "/models"), apiKey)).jsonObject
        return root["data"]?.jsonArray.orEmpty().mapNotNull { item ->
            runCatching { item.jsonObject["id"]?.jsonPrimitive?.contentOrNull }.getOrNull()
        }.distinct().sorted()
    }

    override suspend fun ping(baseUrl: String, apiKey: String, model: String) {
        chatCompletion(baseUrl, apiKey, model, "Responda apenas: OK", maxTokens = 1)
    }

    override suspend fun complete(baseUrl: String, apiKey: String, model: String, prompt: String): String =
        chatCompletion(baseUrl, apiKey, model, prompt, maxTokens = 700)

    private suspend fun chatCompletion(
        baseUrl: String,
        apiKey: String,
        model: String,
        prompt: String,
        maxTokens: Int
    ): String {
        val payload = buildJsonObject {
            put("model", model)
            put("temperature", 0.7)
            put("max_tokens", maxTokens)
            putJsonArray("messages") {
                addJsonObject { put("role", "system"); put("content", "Responda em português brasileiro. Seja cuidadosa e reflexiva.") }
                addJsonObject { put("role", "user"); put("content", prompt) }
            }
        }.toString()
        val root = json.parseToJsonElement(request("POST", endpoint(baseUrl, "/chat/completions"), apiKey, payload)).jsonObject
        return root["choices"]?.jsonArray?.firstOrNull()?.jsonObject
            ?.get("message")?.jsonObject?.get("content")?.jsonPrimitive?.contentOrNull
            ?.takeIf(String::isNotBlank) ?: throw LlmResponseFormatException("Formato inesperado do provedor")
    }

    private suspend fun request(method: String, url: String, apiKey: String, body: String? = null): String {
        val requestBody = body?.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder().url(url).header("Authorization", "Bearer $apiKey")
            .header("Accept", "application/json").method(method, requestBody).build()
        val call = http.newCall(request)
        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { call.cancel() }
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, error: IOException) {
                    continuation.tryResumeWithException(error)?.let(continuation::completeResume)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        val responseText = it.body?.string().orEmpty()
                        if (!it.isSuccessful) {
                            continuation.tryResumeWithException(LlmHttpException(it.code, responseText.take(300)))?.let(continuation::completeResume)
                        } else {
                            continuation.tryResume(responseText)?.let(continuation::completeResume)
                        }
                    }
                }
            })
        }
    }

    private fun endpoint(base: String, path: String): String = normalizeBaseUrl(base) + path
}
