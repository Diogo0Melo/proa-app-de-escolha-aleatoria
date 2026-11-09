package com.ogos.apprandomizador.ui.sound

import com.ogos.apprandomizador.modes.domain.CardMeaning
import com.ogos.apprandomizador.modes.domain.Reading
import com.ogos.apprandomizador.modes.domain.ReadingSection
import com.ogos.apprandomizador.modes.domain.SpreadPosition
import org.junit.Assert.*
import org.junit.Test

class SpeechTextTest {
    @Test fun `texto curto vira uma unica parte`() {
        assertEquals(listOf("Olá mundo"), splitForSpeech("  Olá mundo  ", maxLength = 50))
    }

    @Test fun `texto vazio nao gera partes`() {
        assertTrue(splitForSpeech("   ", maxLength = 10).isEmpty())
    }

    @Test fun `partes respeitam o limite e preservam as palavras`() {
        val text = "uma frase bem comprida para testar a divisão em partes menores"
        val chunks = splitForSpeech(text, maxLength = 12)
        assertTrue(chunks.all { it.length <= 12 })
        assertEquals(text.split(' '), chunks.joinToString(" ").split(' '))
    }

    @Test fun `palavra sem espaco e cortada no limite sem travar`() {
        val chunks = splitForSpeech("a".repeat(25), maxLength = 10)
        assertEquals(listOf(10, 10, 5), chunks.map { it.length })
    }

    @Test fun `leitura e narrada na ordem da tela`() {
        val reading = Reading(
            summary = "Resumo",
            sections = listOf(ReadingSection(SpreadPosition.SINGLE, "O Louco", "Texto da carta")),
            title = "Um caminho",
            opening = "Abertura",
            guidance = "Orientação",
            synthesis = "Síntese"
        )
        assertEquals(
            "Um caminho Abertura Resumo O Louco. Texto da carta Orientação Síntese",
            reading.toSpeechText()
        )
    }

    @Test fun `leitura offline sem campos extras narra resumo e secoes`() {
        val reading = Reading("Resumo", listOf(ReadingSection(SpreadPosition.SINGLE, "Sua carta", "Texto")))
        assertEquals("Resumo Sua carta. Texto", reading.toSpeechText())
    }
}
