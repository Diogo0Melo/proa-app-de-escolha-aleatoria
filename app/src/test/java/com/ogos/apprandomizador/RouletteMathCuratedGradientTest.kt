package com.ogos.apprandomizador

import com.ogos.apprandomizador.model.RaffleItem
import com.ogos.apprandomizador.modes.domain.DefaultOracleAnswers
import com.ogos.apprandomizador.ui.view.component.RouletteMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RouletteMathCuratedGradientTest {

    @Test
    fun classicPositiveAnswersUseEmeraldGradient() {
        val expected = listOf(0xFF2E7D32L, 0xFF388E3CL, 0xFF1B5E20L)

        DefaultOracleAnswers.items.take(3).forEachIndexed { index, answer ->
            assertEquals("positive answer at index $index", expected, RouletteMath.curatedGradientFor(answer, index))
        }
    }

    @Test
    fun classicNegativeAnswersUseRubyGradient() {
        val expected = listOf(0xFF7F1D1DL, 0xFFB91C1CL, 0xFF450A0AL)

        DefaultOracleAnswers.items.drop(3).forEachIndexed { index, answer ->
            assertEquals("negative answer at index $index", expected, RouletteMath.curatedGradientFor(answer, index))
        }
    }

    @Test
    fun arbitraryColorKeepsLegacyGradientAsThreeEquivalentStops() {
        val item = RaffleItem("Personalizado", 0xFF315A9AL)
        val (top, bottom) = RouletteMath.gradientFor(item, 2)
        val actual = RouletteMath.curatedGradientFor(item, 2)

        assertEquals(3, actual.size)
        assertEquals(top, actual.first())
        assertEquals(RouletteMath.lerpArgb(top, bottom, 0.5f), actual[1])
        assertEquals(bottom, actual.last())
    }

    @Test
    fun achromaticColorUsesLegacyGradient() {
        val item = RaffleItem("Cinza", 0xFF777777L)
        val (top, bottom) = RouletteMath.gradientFor(item, 0)

        assertEquals(
            listOf(top, RouletteMath.lerpArgb(top, bottom, 0.5f), bottom),
            RouletteMath.curatedGradientFor(item, 0)
        )
    }

    @Test
    fun positiveAndNegativePalettesRemainDistinct() {
        val positive = RouletteMath.curatedGradientFor(DefaultOracleAnswers.items.first(), 0)
        val negative = RouletteMath.curatedGradientFor(DefaultOracleAnswers.items.last(), 0)

        assertNotEquals(positive, negative)
        assertTrue(positive.all { (it and 0xFF000000L) == 0xFF000000L })
    }
}
