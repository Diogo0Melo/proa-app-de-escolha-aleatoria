package com.ogos.apprandomizador.core.ui.format

import com.ogos.apprandomizador.core.model.RaffleItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorUtilsCuratedGradientTest {

    @Test
    fun greenBaseUsesEmeraldGradient() {
        val expected = listOf(0xFF2E7D32L, 0xFF388E3CL, 0xFF1B5E20L)
        val item = RaffleItem("Verde", 0xFF00FF00L)

        assertEquals(expected, ColorUtils.curatedGradientFor(item, 0))
    }

    @Test
    fun redBaseUsesRubyGradient() {
        val expected = listOf(0xFF7F1D1DL, 0xFFB91C1CL, 0xFF450A0AL)
        val item = RaffleItem("Vermelho", 0xFFFF0000L)

        assertEquals(expected, ColorUtils.curatedGradientFor(item, 0))
    }

    @Test
    fun arbitraryColorKeepsLegacyGradientAsThreeEquivalentStops() {
        val item = RaffleItem("Personalizado", 0xFF315A9AL)
        val (top, bottom) = ColorUtils.gradientFor(item, 2)
        val actual = ColorUtils.curatedGradientFor(item, 2)

        assertEquals(3, actual.size)
        assertEquals(top, actual.first())
        assertEquals(ColorUtils.lerpArgb(top, bottom, 0.5f), actual[1])
        assertEquals(bottom, actual.last())
    }

    @Test
    fun achromaticColorUsesLegacyGradient() {
        val item = RaffleItem("Cinza", 0xFF777777L)
        val (top, bottom) = ColorUtils.gradientFor(item, 0)

        assertEquals(
            listOf(top, ColorUtils.lerpArgb(top, bottom, 0.5f), bottom),
            ColorUtils.curatedGradientFor(item, 0)
        )
    }

    @Test
    fun positiveAndNegativePalettesRemainDistinct() {
        val positive = ColorUtils.curatedGradientFor(RaffleItem("Sim", 0xFF00FF00L), 0)
        val negative = ColorUtils.curatedGradientFor(RaffleItem("Nao", 0xFFFF0000L), 0)

        assertNotEquals(positive, negative)
        assertTrue(positive.all { (it and 0xFF000000L) == 0xFF000000L })
    }
}
