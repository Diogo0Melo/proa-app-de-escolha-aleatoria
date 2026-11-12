package com.ogos.apprandomizador.features.tarot.ui

import com.ogos.apprandomizador.features.tarot.domain.Reading
import com.ogos.apprandomizador.features.tarot.domain.ReadingSection
import com.ogos.apprandomizador.features.tarot.domain.SpreadPosition
import org.junit.Assert.assertEquals
import org.junit.Test

class SpeechTextTest {
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
