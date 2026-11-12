package com.ogos.apprandomizador.features.tarot.data

import com.ogos.apprandomizador.features.tarot.domain.DeckProvider
import com.ogos.apprandomizador.features.tarot.domain.TarotCard

/** Baralho completo: Arcanos Maiores seguidos dos Menores. IDs são únicos entre os dois conjuntos. */
object FullTarotDeck : DeckProvider {
    private val cards: List<TarotCard> = MajorArcanaDeck.getDeck() + MinorArcanaDeck.getDeck()
    override fun getDeck(): List<TarotCard> = cards
}
