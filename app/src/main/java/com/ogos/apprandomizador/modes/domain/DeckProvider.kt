package com.ogos.apprandomizador.modes.domain

fun interface DeckProvider {
    fun getDeck(): List<TarotCard>
}
