package com.ogos.apprandomizador.core.ai.domain

/**
 * Política e mensagens compartilhadas pelos serviços de leitura com IA
 * (aprofundamento do tarô e oráculo).
 *
 * Fica no `core` para que as features não precisem importar o serviço uma da outra:
 * orçamento de tempo, backoff e textos de falha são iguais nos dois fluxos.
 */
object AiServicePolicy {

    val DEFAULT_RETRY_DELAYS_MILLIS: List<Long> = listOf(1_000L, 2_000L)

    const val TOTAL_BUDGET_MILLIS: Long = 90_000L

    const val TIMEOUT_MESSAGE = "A IA demorou demais para responder. Tente novamente em instantes."

    const val NOT_CONFIGURED = "Configure a IA e aceite o envio de dados para aprofundar esta leitura."

    const val MISSING_KEY = "A chave da API não está disponível. Confira as configurações."

    const val INVALID_RESPONSE = "A resposta da IA não pôde ser validada. A leitura offline continua disponível."
}
