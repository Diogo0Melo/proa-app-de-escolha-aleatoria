package com.ogos.apprandomizador.modes.data

import com.ogos.apprandomizador.modes.domain.DeckProvider
import com.ogos.apprandomizador.modes.domain.TarotCard

/** Baralho completo: Arcanos Maiores seguidos dos Menores. IDs são únicos entre os dois conjuntos. */
object FullTarotDeck : DeckProvider {
    private val cards: List<TarotCard> = MajorArcanaDeck.getDeck() + MinorArcanaDeck.getDeck()
    override fun getDeck(): List<TarotCard> = cards
}
