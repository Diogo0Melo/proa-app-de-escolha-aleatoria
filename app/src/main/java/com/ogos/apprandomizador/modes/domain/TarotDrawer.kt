package com.ogos.apprandomizador.modes.domain

import com.ogos.apprandomizador.core.entropy.EntropyStream
import com.ogos.apprandomizador.core.entropy.cryptoShuffled
import com.ogos.apprandomizador.core.entropy.uniformInt

object TarotDrawer {
    internal fun draw(deck: List<TarotCard>, spread: Spread, stream: EntropyStream, allowReversed: Boolean): List<DrawnCard> {
        require(deck.size >= spread.cardCount) { "Deck must contain at least ${spread.cardCount} cards" }
        require(deck.map(TarotCard::id).distinct().size == deck.size) { "Card ids must be unique" }
        val positions = when (spread) {
            Spread.SINGLE -> listOf(SpreadPosition.SINGLE)
            Spread.PAST_PRESENT_FUTURE -> listOf(SpreadPosition.PAST, SpreadPosition.PRESENT, SpreadPosition.FUTURE)
        }
        return cryptoShuffled(deck) { bound -> stream.uniformInt(bound) }
            .take(spread.cardCount)
            .mapIndexed { index, card ->
                DrawnCard(card, allowReversed && stream.uniformInt(2) == 1, positions[index])
            }
    }
}
