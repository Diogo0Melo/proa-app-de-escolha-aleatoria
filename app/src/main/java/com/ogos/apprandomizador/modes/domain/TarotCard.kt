package com.ogos.apprandomizador.modes.domain

/** Naipe da carta. Os Arcanos Maiores usam [MAJOR]; os Menores usam o naipe correspondente. */
enum class Suit { MAJOR, WANDS, CUPS, SWORDS, PENTACLES }

data class TarotCard(
    val id: String,
    val number: Int,
    val name: String,
    val upright: CardMeaning,
    val reversed: CardMeaning,
    val suit: Suit = Suit.MAJOR
)
data class CardMeaning(val keywords: List<String>, val essence: String)
enum class SpreadPosition { SINGLE, PAST, PRESENT, FUTURE }
enum class Spread(val cardCount: Int) { SINGLE(1), PAST_PRESENT_FUTURE(3) }
data class DrawnCard(val card: TarotCard, val reversed: Boolean, val position: SpreadPosition) {
    val meaning: CardMeaning get() = if (reversed) card.reversed else card.upright
}
