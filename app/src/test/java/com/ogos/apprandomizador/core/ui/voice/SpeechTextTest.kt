package com.ogos.apprandomizador.core.ui.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
}
