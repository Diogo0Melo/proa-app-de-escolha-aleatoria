package com.ogos.apprandomizador.modes.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OracleJsonParserTest {
    @Test fun `le json valido com essence`() {
        assertEquals(
            OracleReading("T", "E", "R", "A", ReadingDepth.Short),
            OracleJsonParser.parse("""{"title":"T","essence":"E","reading":"R","affirmation":"A"}""", ReadingDepth.Short)
        )
    }

    @Test fun `aceita cerca de markdown`() {
        assertEquals(
            OracleReading(null, null, "R", null, ReadingDepth.Short),
            OracleJsonParser.parse("```json\n{\"reading\":\"R\"}\n```", ReadingDepth.Short)
        )
    }

    @Test fun `sem texto de leitura nao ha resultado`() {
        assertNull(OracleJsonParser.parse("""{"title":"T"}""", ReadingDepth.Short))
    }

    @Test fun `texto vazio nao e leitura`() {
        assertNull(OracleJsonParser.parse("""{"reading":"   "}""", ReadingDepth.Short))
    }

    @Test fun `ignora texto fora do json`() {
        assertEquals(
            OracleReading(null, null, "R", null, ReadingDepth.Short),
            OracleJsonParser.parse("Claro! {\"reading\":\"R\"} Espero ajudar.", ReadingDepth.Short)
        )
    }

    @Test fun `leitura curta e cortada em 600 caracteres`() {
        val parsed = OracleJsonParser.parse("""{"reading":"${"x".repeat(1_500)}"}""", ReadingDepth.Short)
        assertEquals(600, parsed?.reading?.length)
    }

    @Test fun `leitura estendida aceita ate 1200 caracteres`() {
        val parsed = OracleJsonParser.parse("""{"reading":"${"x".repeat(1_500)}"}""", ReadingDepth.Extended)
        assertEquals(1_200, parsed?.reading?.length)
    }

    @Test fun `essence e cortada em 40 caracteres`() {
        val parsed = OracleJsonParser.parse("""{"essence":"${"e".repeat(80)}","reading":"R"}""", ReadingDepth.Short)
        assertEquals(40, parsed?.essence?.length)
    }
}
