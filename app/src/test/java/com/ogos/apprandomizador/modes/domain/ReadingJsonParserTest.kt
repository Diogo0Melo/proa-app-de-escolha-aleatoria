package com.ogos.apprandomizador.modes.domain

import org.junit.Assert.*
import org.junit.Test

class ReadingJsonParserTest {
    @Test fun `remove cerca JSON e ignora texto ao redor`() {
        val result = ReadingJsonParser.parse("antes ```json\n{\"summary\":\"Resumo\",\"sections\":[{\"position\":\"SINGLE\",\"text\":\"Texto\"}]}\n``` depois", emptyList())
        assertEquals("Resumo", result.summary)
        assertEquals("Texto", result.sections.single().text)
    }

    @Test fun `texto invalido vira secao livre`() {
        val result = ReadingJsonParser.parse("Leitura em texto livre", emptyList())
        assertEquals("Leitura em texto livre", result.sections.single().text)
    }

    @Test fun `campos ausentes recebem fallback sem lancar`() {
        val result = ReadingJsonParser.parse("{\"other\":true}", emptyList())
        assertFalse(result.summary.isBlank())
        assertFalse(result.sections.single().text.isBlank())
    }
}
