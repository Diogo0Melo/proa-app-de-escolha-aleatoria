package com.ogos.apprandomizador.core.ai.domain

interface LlmClient {
    suspend fun listModels(baseUrl: String, apiKey: String): List<String>
    suspend fun ping(baseUrl: String, apiKey: String, model: String)
    suspend fun complete(baseUrl: String, apiKey: String, model: String, prompt: String): String
}
