package com.ogos.apprandomizador.features.oracle.domain

import com.ogos.apprandomizador.core.model.ItemList
import com.ogos.apprandomizador.core.model.RaffleItem

/** O que alimenta o sorteio: o oráculo clássico (topic nulo) ou uma roleta do usuário. */
data class OracleSource(val topic: String?, val items: List<RaffleItem>) {
    companion object {
        val CLASSIC = OracleSource(topic = null, items = DefaultOracleAnswers.items)
    }
}

/** Itens em branco nunca entram no sorteio. */
fun ItemList.asOracleSource(): OracleSource =
    OracleSource(topic = topic, items = items.filterNot(RaffleItem::isBlank))
