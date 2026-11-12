package com.ogos.apprandomizador.features.oracle.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OracleJsonParserTest {
    @Test fun `le json valido com essence`() {
        assertEquals(
            OracleReading("T", "E", "R", "A"),
            OracleJsonParser.parse("""{"title":"T","essence":"E","reading":"R","affirmation":"A"}""")
        )
    }

    @Test fun `aceita cerca de markdown`() {
        assertEquals(
            OracleReading(null, null, "R", null),
            OracleJsonParser.parse("```json\n{\"reading\":\"R\"}\n```")
        )
    }

    @Test fun `sem texto de leitura nao ha resultado`() {
        assertNull(OracleJsonParser.parse("""{"title":"T"}"""))
    }

    @Test fun `texto vazio nao e leitura`() {
        assertNull(OracleJsonParser.parse("""{"reading":"   "}"""))
    }

    @Test fun `ignora texto fora do json`() {
        assertEquals(
            OracleReading(null, null, "R", null),
            OracleJsonParser.parse("Claro! {\"reading\":\"R\"} Espero ajudar.")
        )
    }

    @Test fun `leitura e cortada em 600 caracteres`() {
        val parsed = OracleJsonParser.parse("""{"reading":"${"x".repeat(1_500)}"}""")
        assertEquals(600, parsed?.reading?.length)
    }

    @Test fun `essence e cortada em 40 caracteres`() {
        val parsed = OracleJsonParser.parse("""{"essence":"${"e".repeat(80)}","reading":"R"}""")
        assertEquals(40, parsed?.essence?.length)
    }
}
