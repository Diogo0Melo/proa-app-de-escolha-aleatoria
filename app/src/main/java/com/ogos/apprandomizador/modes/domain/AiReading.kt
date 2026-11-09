package com.ogos.apprandomizador.modes.domain

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import java.io.IOException
import javax.net.ssl.SSLException

sealed interface AiReadingResult {
    data class Success(val reading: Reading, val modelId: String) : AiReadingResult
    data class Failure(val reason: String) : AiReadingResult
}

class LlmHttpException(val statusCode: Int, message: String) : IOException("HTTP $statusCode: $message")

/** Resposta recebida do provedor, porém fora do formato esperado. Não é retentada. */
class LlmResponseFormatException(message: String) : IOException(message)

interface LlmClient {
    suspend fun listModels(baseUrl: String, apiKey: String): List<String>
    suspend fun ping(baseUrl: String, apiKey: String, model: String)
    suspend fun complete(baseUrl: String, apiKey: String, model: String, prompt: String): String
}

/**
 * Aprofundamento da leitura com IA.
 *
 * Erros transitórios (rede, 408, 429 e 5xx) são retentados com backoff; erros permanentes
 * (chave, modelo, formato) falham de imediato. Todo o processo respeita um orçamento total
 * de tempo, para que o usuário nunca fique esperando indefinidamente.
 */
class AiReadingService(
    private val client: LlmClient,
    private val settings: AiSettingsRepository,
    private val retryDelaysMillis: List<Long> = DEFAULT_RETRY_DELAYS_MILLIS,
    private val totalBudgetMillis: Long = TOTAL_BUDGET_MILLIS
) {
    /**
     * @param onRetry chamado com o número da tentativa que vai começar (2 ou mais),
     * antes da espera do backoff, para a interface poder informar o usuário.
     */
    suspend fun deepen(
        question: String?,
        draw: List<DrawnCard>,
        onRetry: (attempt: Int) -> Unit = {}
    ): AiReadingResult {
        return try {
            withTimeout(totalBudgetMillis) { runAttempt(question, draw, onRetry) }
        } catch (_: TimeoutCancellationException) {
            // Deve vir antes de CancellationException: o timeout também herda dela.
            AiReadingResult.Failure(TIMEOUT_MESSAGE)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            AiReadingResult.Failure(ApiErrorMapper.message(error))
        }
    }

    private suspend fun runAttempt(question: String?, draw: List<DrawnCard>, onRetry: (Int) -> Unit): AiReadingResult {
        val configured = settings.settingsSnapshot() ?: return AiReadingResult.Failure(NOT_CONFIGURED)
        val key = settings.apiKey() ?: return AiReadingResult.Failure(MISSING_KEY)
        val prompt = PromptBuilder.build(question, draw)
        val raw = completeWithRetry(configured, key, prompt, onRetry)
        val reading = ReadingJsonParser.parse(raw, draw) ?: return AiReadingResult.Failure(INVALID_RESPONSE)
        return AiReadingResult.Success(reading, configured.model)
    }

    private suspend fun completeWithRetry(config: AiConfig, key: String, prompt: String, onRetry: (Int) -> Unit): String {
        var attempt = 1
        while (attempt <= retryDelaysMillis.size) {
            try {
                return client.complete(config.baseUrl, key, config.model, prompt)
            } catch (error: Exception) {
                if (error is CancellationException || !isTransient(error)) throw error
                onRetry(attempt + 1)
                delay(retryDelaysMillis[attempt - 1])
                attempt++
            }
        }
        // Última tentativa: qualquer erro segue para o chamador.
        return client.complete(config.baseUrl, key, config.model, prompt)
    }

    companion object {
        val DEFAULT_RETRY_DELAYS_MILLIS: List<Long> = listOf(1_000L, 2_000L)
        const val TOTAL_BUDGET_MILLIS: Long = 90_000L
        const val TIMEOUT_MESSAGE = "A IA demorou demais para responder. Tente novamente em instantes."
        const val NOT_CONFIGURED = "Configure a IA e aceite o envio de dados para aprofundar esta leitura."
        const val MISSING_KEY = "A chave da API não está disponível. Confira as configurações."
        const val INVALID_RESPONSE = "A resposta da IA não pôde ser validada. A leitura offline continua disponível."
    }
}

/** Classifica se um erro pode ser resolvido por nova tentativa. */
internal fun isTransient(error: Exception): Boolean = when (error) {
    is LlmHttpException -> error.statusCode == 408 || error.statusCode == 429 || error.statusCode in 500..599
    is LlmResponseFormatException -> false
    is SSLException -> false
    is IOException -> true
    else -> false
}

// A leitura das configurações é mantida num repositório de fluxo; este helper lê o valor atual sem expor a chave.
suspend fun AiSettingsRepository.settingsSnapshot(): AiConfig? = settings.first().let { state ->
    state.config?.takeIf { it.enabled && state.consentedAuthority == BaseUrlNormalizer.authority(it.baseUrl) }
}
