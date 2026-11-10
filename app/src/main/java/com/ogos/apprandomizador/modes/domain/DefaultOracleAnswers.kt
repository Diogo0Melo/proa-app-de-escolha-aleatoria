package com.ogos.apprandomizador.modes.domain

import com.ogos.apprandomizador.model.RaffleItem

/**
 * As seis respostas do Sim ou Não clássico. Ficam fora do banco de propósito: uma roleta do
 * usuário pode ser editada ou apagada, mas o oráculo clássico não.
 */
object DefaultOracleAnswers {
    val items: List<RaffleItem> = listOf(
        RaffleItem("DEFINITIVAMENTE SIM", 0xFF00FF00L),
        RaffleItem("SIM", 0xFF009600L),
        RaffleItem("TALVEZ SIM", 0xFF005500L),
        RaffleItem("TALVEZ NÃO", 0xFF550000L),
        RaffleItem("NÃO", 0xFF960000L),
        RaffleItem("ABSOLUTAMENTE NÃO", 0xFFFF0000L),
    )
}
