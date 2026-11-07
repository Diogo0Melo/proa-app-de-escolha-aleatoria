package com.ogos.apprandomizador.modes.domain

import com.ogos.apprandomizador.modes.data.MajorArcanaDeck
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MajorArcanaDeckTest {
    @Test
    fun `deck has 22 unique numbered cards with complete meanings`() {
        val deck = MajorArcanaDeck.getDeck()
        assertEquals(22, deck.size)
        assertEquals(22, deck.map { it.id }.distinct().size)
        assertEquals((0..21).toList(), deck.map { it.number })
        deck.forEach { card ->
            assertTrue(card.name.isNotBlank())
            assertFalse(card.upright.essence.isBlank())
            assertFalse(card.reversed.essence.isBlank())
            assertTrue(card.upright.keywords.isNotEmpty())
            assertTrue(card.reversed.keywords.isNotEmpty())
        }
    }
}
