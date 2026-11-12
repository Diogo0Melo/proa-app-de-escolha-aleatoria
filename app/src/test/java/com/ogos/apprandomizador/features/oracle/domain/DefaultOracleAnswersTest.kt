package com.ogos.apprandomizador.features.oracle.domain

import com.ogos.apprandomizador.core.ui.format.ColorUtils
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Contrato visual do Sim ou Não clássico: as respostas positivas caem no
 * esmeralda e as negativas no rubi. O cálculo em si vive no core (ColorUtils);
 * este teste garante que as cores reais do oráculo continuam produzindo isso.
 */
class DefaultOracleAnswersTest {

    @Test
    fun positiveAnswersUseEmeraldGradient() {
        val expected = listOf(0xFF2E7D32L, 0xFF388E3CL, 0xFF1B5E20L)

        DefaultOracleAnswers.items.take(3).forEachIndexed { index, answer ->
            assertEquals("positive answer at index $index", expected, ColorUtils.curatedGradientFor(answer, index))
        }
    }

    @Test
    fun negativeAnswersUseRubyGradient() {
        val expected = listOf(0xFF7F1D1DL, 0xFFB91C1CL, 0xFF450A0AL)

        DefaultOracleAnswers.items.drop(3).forEachIndexed { index, answer ->
            assertEquals("negative answer at index $index", expected, ColorUtils.curatedGradientFor(answer, index))
        }
    }
}
