package com.ogos.apprandomizador.features.oracle.domain

import com.ogos.apprandomizador.core.ai.domain.AiServicePolicy
import com.ogos.apprandomizador.core.ai.domain.AiSettingsRepository
import com.ogos.apprandomizador.core.ai.domain.ApiErrorMapper
import com.ogos.apprandomizador.core.ai.domain.LlmClient
import com.ogos.apprandomizador.core.ai.domain.completeWithLlmRetry
import com.ogos.apprandomizador.core.ai.domain.settingsSnapshot
import com.ogos.apprandomizador.core.model.RaffleItem
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

sealed interface OracleAiResult {
    data class Success(val reading: OracleReading, val modelId: String) : OracleAiResult
    data class Failure(val reason: String) : OracleAiResult
}

/**
 * Leitura do oráculo com IA. A resposta sorteada entra no prompt como fato já decidido;
 * a IA não tem como alterá-la nem decidi-la.
 */
class OracleAiService(
    private val client: LlmClient,
    private val settings: AiSettingsRepository,
    private val retryDelaysMillis: List<Long> = AiServicePolicy.DEFAULT_RETRY_DELAYS_MILLIS,
    private val totalBudgetMillis: Long = AiServicePolicy.TOTAL_BUDGET_MILLIS
) {
    suspend fun deepen(
        question: String?,
        answer: RaffleItem,
        topic: String?,
        onRetry: (attempt: Int) -> Unit = {}
    ): OracleAiResult {
        return try {
            withTimeout(totalBudgetMillis.milliseconds) { runAttempt(question, answer, topic, onRetry) }
        } catch (_: TimeoutCancellationException) {
            // Vem antes de CancellationException: o timeout também herda dela.
            OracleAiResult.Failure(AiServicePolicy.TIMEOUT_MESSAGE)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            OracleAiResult.Failure(ApiErrorMapper.message(error))
        }
    }

    private suspend fun runAttempt(
        question: String?,
        answer: RaffleItem,
        topic: String?,
        onRetry: (Int) -> Unit
    ): OracleAiResult {
        val configured = settings.settingsSnapshot() ?: return OracleAiResult.Failure(AiServicePolicy.NOT_CONFIGURED)
        val key = settings.apiKey() ?: return OracleAiResult.Failure(AiServicePolicy.MISSING_KEY)
        val prompt = OraclePromptBuilder.build(question, answer, topic)
        val raw = completeWithLlmRetry(client, configured, key, prompt, retryDelaysMillis, onRetry)
        val reading = OracleJsonParser.parse(raw) ?: return OracleAiResult.Failure(AiServicePolicy.INVALID_RESPONSE)
        return OracleAiResult.Success(reading, configured.model)
    }
}
