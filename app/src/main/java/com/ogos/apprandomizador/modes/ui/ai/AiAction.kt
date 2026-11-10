package com.ogos.apprandomizador.modes.ui.ai

/** Ação de IA disponível numa leitura. Derivada do estado, nunca montada a partir de flags soltas na tela. */
enum class AiAction { None, Deepen, Loading, Retry }

/**
 * Regra única para todas as leituras: depois de uma leitura da IA com sucesso, nenhum botão de IA
 * volta para aquela tiragem. Só há nova chamada depois de erro ou de cancelamento.
 */
fun aiActionFor(
    enabled: Boolean,
    revealed: Boolean,
    hasReading: Boolean,
    loading: Boolean,
    error: String?,
    cancelled: Boolean
): AiAction = when {
    !enabled || !revealed -> AiAction.None
    hasReading -> AiAction.None
    loading -> AiAction.Loading
    error != null || cancelled -> AiAction.Retry
    else -> AiAction.Deepen
}
