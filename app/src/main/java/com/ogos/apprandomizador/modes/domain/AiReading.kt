package com.ogos.apprandomizador.modes.domain

import kotlinx.coroutines.flow.first
import java.io.IOException

sealed interface AiReadingResult {
    data class Success(val reading: Reading, val modelId: String) : AiReadingResult
    data class Failure(val reason: String) : AiReadingResult
}

class LlmHttpException(val statusCode: Int, message: String) : IOException("HTTP $statusCode: $message")

interface LlmClient {
    suspend fun listModels(baseUrl: String, apiKey: String): List<String>
    suspend fun ping(baseUrl: String, apiKey: String, model: String)
    suspend fun complete(baseUrl: String, apiKey: String, model: String, prompt: String): String
}

class AiReadingService(
    private val client: LlmClient,
    private val settings: AiSettingsRepository
) {
    suspend fun deepen(question: String?, draw: List<DrawnCard>): AiReadingResult {
        return try {
            val configured = settings.settingsSnapshot()
                ?: return AiReadingResult.Failure("Configure a IA e aceite o envio de dados para aprofundar esta leitura.")
            val key = settings.apiKey()
                ?: return AiReadingResult.Failure("A chave da API não está disponível. Confira as configurações.")
            val prompt = PromptBuilder.build(question, draw)
            val raw = client.complete(configured.baseUrl, key, configured.model, prompt)
            AiReadingResult.Success(ReadingJsonParser.parse(raw, draw), configured.model)
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            AiReadingResult.Failure(ApiErrorMapper.message(error))
        }
    }
}

// A leitura das configurações é mantida num repositório de fluxo; este helper lê o valor atual sem expor a chave.
suspend fun AiSettingsRepository.settingsSnapshot(): AiConfig? = settings.first().let { state -> state.config?.takeIf { it.enabled && state.consentedAuthority == BaseUrlNormalizer.authority(it.baseUrl) } }
