package com.ogos.apprandomizador.features.tarot.data

import com.ogos.apprandomizador.features.tarot.domain.Suit
import org.junit.Assert.*
import org.junit.Test

/** Os 56 Arcanos Menores: quatorze cartas em cada um dos quatro naipes. */
class MinorArcanaDeckTest {
    private val deck = MinorArcanaDeck.getDeck()

    @Test fun `contem quatorze cartas em cada naipe`() {
        assertEquals(56, deck.size)
        assertEquals(setOf(Suit.WANDS, Suit.CUPS, Suit.SWORDS, Suit.PENTACLES), deck.map { it.suit }.toSet())
        deck.groupBy { it.suit }.values.forEach { assertEquals(14, it.size) }
    }

    @Test fun `ids sao unicos e seguem o prefixo do naipe`() {
        assertEquals(deck.size, deck.map { it.id }.distinct().size)
        val prefixes = mapOf(Suit.WANDS to "w", Suit.CUPS to "c", Suit.SWORDS to "s", Suit.PENTACLES to "p")
        prefixes.forEach { (suit, prefix) ->
            assertEquals((1..14).map { prefix + "%02d".format(it) }, deck.filter { it.suit == suit }.map { it.id })
        }
    }

    @Test fun `numeros vao de um a quatorze em cada naipe`() {
        deck.groupBy { it.suit }.values.forEach { cards ->
            assertEquals((1..14).toList(), cards.map { it.number }.sorted())
        }
    }

    @Test fun `nomes sao unicos e nao estao vazios`() {
        assertEquals(deck.size, deck.map { it.name }.distinct().size)
        assertTrue(deck.all { it.name.isNotBlank() })
    }

    @Test fun `cada orientacao tem tres palavras-chave e um texto`() {
        deck.forEach { card ->
            listOf(card.upright, card.reversed).forEach { meaning ->
                assertEquals("${card.id} keywords", 3, meaning.keywords.size)
                assertTrue("${card.id} keyword vazia", meaning.keywords.all { it.isNotBlank() })
                assertFalse("${card.id} essence vazia", meaning.essence.isBlank())
            }
        }
    }
}
