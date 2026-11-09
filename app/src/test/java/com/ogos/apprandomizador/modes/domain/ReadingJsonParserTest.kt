package com.ogos.apprandomizador.modes.domain

import org.junit.Assert.*
import org.junit.Test

class ReadingJsonParserTest {
    private val fool = TarotCard("00", 0, "O Louco", CardMeaning(listOf("começo"), "essência"), CardMeaning(listOf("medo"), "essência invertida"))
    private val magician = TarotCard("01", 1, "O Mago", CardMeaning(listOf("foco"), "essência"), CardMeaning(listOf("bloqueio"), "essência invertida"))
    private val tower = TarotCard("16", 16, "A Torre", CardMeaning(listOf("ruptura"), "essência"), CardMeaning(listOf("tensão"), "essência invertida"))
    private val single = listOf(DrawnCard(fool, false, SpreadPosition.SINGLE))
    private val threeCards = listOf(
        DrawnCard(magician, false, SpreadPosition.PAST),
        DrawnCard(tower, true, SpreadPosition.PRESENT),
        DrawnCard(fool, false, SpreadPosition.FUTURE)
    )

    @Test fun `remove cerca JSON e ignora texto ao redor`() {
        val raw = "antes ```json\n{\"summary\":\"Resumo\",\"sections\":[{\"position\":\"SINGLE\",\"text\":\"Texto\"}]}\n``` depois"
        val result = ReadingJsonParser.parse(raw, single)
        assertNotNull(result)
        assertEquals("Resumo", result!!.summary)
        assertEquals("Texto", result.sections.single().text)
    }

    @Test fun `texto sem JSON valido nao vira leitura`() {
        assertNull(ReadingJsonParser.parse("Leitura em texto livre", single))
    }

    @Test fun `JSON sem secoes aproveitaveis retorna null sem lancar`() {
        assertNull(ReadingJsonParser.parse("{\"other\":true}", single))
        assertNull(ReadingJsonParser.parse("{\"sections\":[{\"position\":\"SINGLE\",\"text\":\"   \"}]}", single))
    }

    @Test fun `posicao inventada e descartada`() {
        val raw = "{\"sections\":[{\"position\":\"FUTURE\",\"text\":\"inventada\"},{\"position\":\"SINGLE\",\"text\":\"valida\"}]}"
        val result = ReadingJsonParser.parse(raw, single)!!
        assertEquals(1, result.sections.size)
        assertEquals("valida", result.sections.single().text)
    }

    @Test fun `titulo da secao vem da carta sorteada e nao do modelo`() {
        val raw = "{\"sections\":[{\"position\":\"SINGLE\",\"title\":\"Titulo inventado\",\"text\":\"Texto\"}]}"
        assertEquals("O Louco", ReadingJsonParser.parse(raw, single)!!.sections.single().title)
    }

    @Test fun `secoes seguem a ordem do sorteio e ignoram posicoes repetidas`() {
        val raw = "{\"sections\":[" +
            "{\"position\":\"FUTURE\",\"text\":\"f\"}," +
            "{\"position\":\"PAST\",\"text\":\"p1\"}," +
            "{\"position\":\"PAST\",\"text\":\"p2\"}," +
            "{\"position\":\"PRESENT\",\"text\":\"pr\"}]}"
        val sections = ReadingJsonParser.parse(raw, threeCards)!!.sections
        assertEquals(listOf(SpreadPosition.PAST, SpreadPosition.PRESENT, SpreadPosition.FUTURE), sections.map { it.position })
        assertEquals("p1", sections.first().text)
    }

    @Test fun `posicao ausente usa a ordem do sorteio`() {
        val result = ReadingJsonParser.parse("{\"sections\":[{\"text\":\"Texto\"}]}", single)!!
        assertEquals(SpreadPosition.SINGLE, result.sections.single().position)
        assertEquals("O Louco", result.sections.single().title)
    }

    @Test fun `campos novos da leitura sao preenchidos e textos longos sao limitados`() {
        val long = "x".repeat(5_000)
        val raw = "{\"title\":\"Um caminho\",\"opening\":\"Abertura\",\"summary\":\"Resumo\"," +
            "\"sections\":[{\"position\":\"SINGLE\",\"text\":\"Texto\"}],\"guidance\":\"Orientação\",\"synthesis\":\"$long\"}"
        val result = ReadingJsonParser.parse(raw, single)!!
        assertEquals("Um caminho", result.title)
        assertEquals("Abertura", result.opening)
        assertEquals("Orientação", result.guidance)
        assertEquals(2_000, result.synthesis!!.length)
    }

    @Test fun `resumo ausente recebe texto padrao`() {
        val result = ReadingJsonParser.parse("{\"sections\":[{\"position\":\"SINGLE\",\"text\":\"Texto\"}]}", single)!!
        assertFalse(result.summary.isBlank())
    }
}
