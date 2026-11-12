package com.ogos.apprandomizador.modes.domain

import com.ogos.apprandomizador.core.model.RaffleItem

/**
 * Frase de reflexão para cada resposta do oráculo clássico. Aparece quando a IA está desligada
 * ou indisponível, para que a resposta sempre venha com um contexto breve.
 */
object DefaultOracleReflections {
    private val byName = mapOf(
        "DEFINITIVAMENTE SIM" to "O caminho está aberto. Confie e avance.",
        "SIM" to "A resposta é positiva. Siga com confiança.",
        "TALVEZ SIM" to "Há abertura, mas fatores ainda se movem. Observe.",
        "TALVEZ NÃO" to "Algo pede cautela. Reavalie antes de decidir.",
        "NÃO" to "A resposta é negativa. Considere outras rotas.",
        "ABSOLUTAMENTE NÃO" to "Um limite claro se apresenta. Respeite-o."
    )

    fun forAnswer(answer: RaffleItem): String =
        byName[answer.name] ?: "O oráculo escolheu: ${answer.name}. Reflita sobre o que isso significa para você."
}
