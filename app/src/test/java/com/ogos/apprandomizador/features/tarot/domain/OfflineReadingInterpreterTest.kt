package com.ogos.apprandomizador.features.tarot.domain

import com.ogos.apprandomizador.features.tarot.data.FullTarotDeck
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineReadingInterpreterTest {
    @Test fun `all card orientations produce useful offline text`() = runBlocking {
        val interpreter = OfflineReadingInterpreter()
        FullTarotDeck.getDeck().forEach { card ->
            listOf(card.upright, card.reversed).forEachIndexed { index, meaning ->
                val drawn = DrawnCard(card, index == 1, SpreadPosition.SINGLE)
                val reading = interpreter.interpret(null, listOf(drawn))
                assertTrue(reading.summary.isNotBlank())
                assertTrue(reading.sections.single().text.isNotBlank())
                assertFalse(reading.sections.single().text.contains('{'))
                assertFalse(reading.sections.single().text.contains('}'))
                assertTrue(meaning.essence.isNotBlank())
            }
        }
    }

    @Test fun `reading uses question context and position labels`() = runBlocking {
        val card = FullTarotDeck.getDeck().first()
        val reading = OfflineReadingInterpreter().interpret("Devo mudar?", listOf(
            DrawnCard(card, false, SpreadPosition.PAST),
            DrawnCard(FullTarotDeck.getDeck()[1], true, SpreadPosition.PRESENT),
            DrawnCard(FullTarotDeck.getDeck()[2], false, SpreadPosition.FUTURE)
        ))
        assertTrue(reading.summary.contains("Devo mudar?"))
        assertTrue(reading.sections.map { it.title }.any { it.startsWith("Passado") })
        assertTrue(reading.sections.map { it.title }.any { it.startsWith("Presente") })
        assertTrue(reading.sections.map { it.title }.any { it.startsWith("Futuro") })
    }
}
