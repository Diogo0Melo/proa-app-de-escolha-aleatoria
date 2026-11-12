package com.ogos.apprandomizador.features.tarot.domain

import org.junit.Assert.*
import org.junit.Test

class PromptBuilderTest {
    @Test fun `inclui pergunta nome orientacao e posicao sem chave`() {
        val card = TarotCard("tower", 16, "A Torre", CardMeaning(listOf("ruptura"), "essência"), CardMeaning(listOf("resistência"), "essência invertida"))
        val prompt = PromptBuilder.build("Qual caminho?", listOf(DrawnCard(card, true, SpreadPosition.PAST)))
        assertTrue(prompt.contains("Qual caminho?"))
        assertTrue(prompt.contains("A Torre"))
        assertTrue(prompt.contains("PAST"))
        assertTrue(prompt.contains("invertida"))
        assertFalse(prompt.contains("api_key"))
        assertFalse(prompt.contains("segredo-de-teste"))
        assertTrue(prompt.contains("\"synthesis\""))
        assertTrue(prompt.contains("\"guidance\""))
    }
}
