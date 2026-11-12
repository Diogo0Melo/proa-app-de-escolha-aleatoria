package com.ogos.apprandomizador.features.tarot.domain

import com.ogos.apprandomizador.core.ai.domain.AiSettingsRepository
import com.ogos.apprandomizador.core.ai.domain.AiServicePolicy
import com.ogos.apprandomizador.core.ai.domain.ApiErrorMapper
import com.ogos.apprandomizador.core.ai.domain.LlmClient
import com.ogos.apprandomizador.core.ai.domain.completeWithLlmRetry
import com.ogos.apprandomizador.core.ai.domain.settingsSnapshot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

sealed interface AiReadingResult {
    data class Success(val reading: Reading, val modelId: String) : AiReadingResult
    data class Failure(val reason: String) : AiReadingResult
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
    private val settings: com.ogos.apprandomizador.core.ai.domain.AiSettingsRepository,
    private val retryDelaysMillis: List<Long> = AiServicePolicy.DEFAULT_RETRY_DELAYS_MILLIS,
    private val totalBudgetMillis: Long = AiServicePolicy.TOTAL_BUDGET_MILLIS
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
            AiReadingResult.Failure(AiServicePolicy.TIMEOUT_MESSAGE)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            AiReadingResult.Failure(ApiErrorMapper.message(error))
        }
    }

    private suspend fun runAttempt(question: String?, draw: List<DrawnCard>, onRetry: (Int) -> Unit): AiReadingResult {
        val configured = settings.settingsSnapshot() ?: return AiReadingResult.Failure(AiServicePolicy.NOT_CONFIGURED)
        val key = settings.apiKey() ?: return AiReadingResult.Failure(AiServicePolicy.MISSING_KEY)
        val prompt = PromptBuilder.build(question, draw)
        val raw = completeWithLlmRetry(client, configured, key, prompt, retryDelaysMillis, onRetry)
        val reading = ReadingJsonParser.parse(raw, draw) ?: return AiReadingResult.Failure(AiServicePolicy.INVALID_RESPONSE)
        return AiReadingResult.Success(reading, configured.model)
    }

}
