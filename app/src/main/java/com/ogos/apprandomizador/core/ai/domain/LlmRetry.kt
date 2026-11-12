package com.ogos.apprandomizador.core.ai.domain

import java.io.IOException
import javax.net.ssl.SSLException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/** Classifica se um erro pode ser resolvido por nova tentativa. */
internal fun isTransient(error: Exception): Boolean = when (error) {
    is LlmHttpException -> error.statusCode == 408 || error.statusCode == 429 || error.statusCode in 500..599
    is LlmResponseFormatException -> false
    is SSLException -> false
    is IOException -> true
    else -> false
}

/**
 * Chama o provedor e retenta erros transitórios com o backoff informado.
 * Compartilhado pela leitura do tarô e pelo oráculo; o último erro sempre segue para o chamador.
 */
internal suspend fun completeWithLlmRetry(
    client: LlmClient,
    config: AiConfig,
    key: String,
    prompt: String,
    retryDelaysMillis: List<Long>,
    onRetry: (Int) -> Unit
): String {
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
