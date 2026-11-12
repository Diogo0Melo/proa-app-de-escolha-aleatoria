package com.ogos.apprandomizador.features.tarot.data

import com.ogos.apprandomizador.features.tarot.domain.Suit
import org.junit.Assert.*
import org.junit.Test

/** Baralho completo: 22 Arcanos Maiores e 56 Arcanos Menores, totalizando 78 cartas. */
class FullTarotDeckTest {
    @Test fun `combina Maiores e Menores na ordem definida`() {
        val full = FullTarotDeck.getDeck()
        assertEquals(MajorArcanaDeck.getDeck() + MinorArcanaDeck.getDeck(), full)
        assertEquals(78, full.size)
    }

    @Test fun `ids sao unicos entre Maiores e Menores`() {
        val full = FullTarotDeck.getDeck()
        assertEquals(full.size, full.map { it.id }.distinct().size)
    }

    @Test fun `Maiores mantem os ids originais para compatibilidade com o historico`() {
        val majors = FullTarotDeck.getDeck().filter { it.suit == Suit.MAJOR }
        assertEquals((0..21).map { it.toString().padStart(2, '0') }, majors.map { it.id })
    }
}
