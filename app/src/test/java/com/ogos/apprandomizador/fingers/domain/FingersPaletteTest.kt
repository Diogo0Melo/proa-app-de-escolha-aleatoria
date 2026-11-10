package com.ogos.apprandomizador.fingers.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class FingersPaletteTest {

    @Test fun `os primeiros toques recebem cores todas distintas`() {
        val colors = (0 until FingersPalette.size).map { FingersPalette.colorAt(it) }
        assertEquals(FingersPalette.size, colors.toSet().size)
    }

    @Test fun `cada toque novo tem cor diferente da do toque anterior`() {
        for (i in 0 until FingersPalette.size * 3) {
            assertNotEquals("toques $i e ${i + 1}", FingersPalette.colorAt(i), FingersPalette.colorAt(i + 1))
        }
    }

    @Test fun `cicla depois de esgotar a paleta`() {
        assertEquals(FingersPalette.colorAt(0), FingersPalette.colorAt(FingersPalette.size))
    }

    @Test fun `indice negativo e recusado`() {
        assertThrows(IllegalArgumentException::class.java) { FingersPalette.colorAt(-1) }
    }
}
