package com.ogos.apprandomizador.features.tarot.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class RomanNumeralTest {
    @Test fun `converte os numeros dos arcanos maiores`() {
        assertEquals("0", romanNumeral(0))
        assertEquals("I", romanNumeral(1))
        assertEquals("IV", romanNumeral(4))
        assertEquals("IX", romanNumeral(9))
        assertEquals("XIV", romanNumeral(14))
        assertEquals("XVI", romanNumeral(16))
        assertEquals("XXI", romanNumeral(21))
    }
}
