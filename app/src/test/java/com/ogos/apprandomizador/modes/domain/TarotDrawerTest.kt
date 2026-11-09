package com.ogos.apprandomizador.modes.domain

import com.ogos.apprandomizador.modes.data.FullTarotDeck
import com.ogos.apprandomizador.viewmodel.deriveOracleStream
import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TarotDrawerTest {
    private fun seed(index: Int) = MessageDigest.getInstance("SHA-256").digest(ByteArray(4) { (index ushr (it * 8)).toByte() })

    @Test fun `single and three card spreads have expected size and positions`() {
        val deck = FullTarotDeck.getDeck()
        val one = TarotDrawer.draw(deck, Spread.SINGLE, deriveOracleStream(seed(1), 1), false)
        val three = TarotDrawer.draw(deck, Spread.PAST_PRESENT_FUTURE, deriveOracleStream(seed(1), 2), true)
        assertEquals(1, one.size)
        assertEquals(SpreadPosition.SINGLE, one.single().position)
        assertEquals(3, three.size)
        assertEquals(listOf(SpreadPosition.PAST, SpreadPosition.PRESENT, SpreadPosition.FUTURE), three.map { it.position })
        assertEquals(3, three.map { it.card.id }.distinct().size)
    }

    @Test fun `draw is deterministic and never repeats a card`() {
        val deck = FullTarotDeck.getDeck()
        val first = TarotDrawer.draw(deck, Spread.PAST_PRESENT_FUTURE, deriveOracleStream(seed(42), 7), true)
        val second = TarotDrawer.draw(deck, Spread.PAST_PRESENT_FUTURE, deriveOracleStream(seed(42), 7), true)
        assertEquals(first, second)
        repeat(10_000) { index ->
            val drawn = TarotDrawer.draw(deck, Spread.PAST_PRESENT_FUTURE, deriveOracleStream(seed(index), 1), true)
            assertEquals(3, drawn.map { it.card.id }.distinct().size)
        }
        assertNotEquals(first, TarotDrawer.draw(deck, Spread.PAST_PRESENT_FUTURE, deriveOracleStream(seed(42), 8), true))
    }

    @Test fun `reversed orientation remains balanced across deterministic seeds`() {
        val deck = FullTarotDeck.getDeck()
        var reversed = 0
        var total = 0
        repeat(1_000) { index ->
            val drawn = TarotDrawer.draw(deck, Spread.PAST_PRESENT_FUTURE, deriveOracleStream(seed(index), 1), true)
            reversed += drawn.count { it.reversed }
            total += drawn.size
        }
        assertTrue("reversed count was $reversed of $total", reversed in (total * 47 / 100)..(total * 53 / 100))
    }

    @Test fun `reversed cards are disabled when requested`() {
        val result = TarotDrawer.draw(FullTarotDeck.getDeck(), Spread.PAST_PRESENT_FUTURE, deriveOracleStream(seed(8), 1), false)
        assertTrue(result.none { it.reversed })
    }

    @Test fun `rejects a deck smaller than the spread`() {
        val smallDeck = FullTarotDeck.getDeck().take(2)
        assertThrows(IllegalArgumentException::class.java) {
            TarotDrawer.draw(smallDeck, Spread.PAST_PRESENT_FUTURE, deriveOracleStream(seed(1), 1), true)
        }
    }
}
