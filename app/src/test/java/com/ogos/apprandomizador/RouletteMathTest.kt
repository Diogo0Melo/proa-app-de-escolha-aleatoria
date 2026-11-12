package com.ogos.apprandomizador

import com.ogos.apprandomizador.features.randomize.ui.component.RouletteMath
import kotlin.math.abs
import kotlin.math.tan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouletteMathTest {

    // --- landing ------------------------------------------------------------

    @Test
    fun targetAngleLandsTargetExactlyAtCenter() {
        for (itemCount in listOf(1, 2, 3, 7, 12, 21)) {
            val total = RouletteMath.totalSlots(itemCount)
            val theta = RouletteMath.thetaDegrees(total)
            for (target in 0 until itemCount) {
                val slot = target % total
                val end = RouletteMath.targetAngle(0f, slot, total, 2)
                val a = RouletteMath.normalizeDegrees(slot * theta - end)
                assertTrue(
                    "itemCount=$itemCount target=$target landed at $a deg",
                    abs(a) < 1e-2f
                )
            }
        }
    }

    @Test
    fun targetAngleLandsAtCenterFromArbitraryStart() {
        val total = RouletteMath.totalSlots(5)
        val theta = RouletteMath.thetaDegrees(total)
        val start = 987.654f
        val end = RouletteMath.targetAngle(start, 3, total, 2)
        val a = RouletteMath.normalizeDegrees(3 * theta - end)
        assertTrue(abs(a) < 1e-2f)
    }

    // --- full turns / monotonicity ------------------------------------------

    @Test
    fun targetAngleGuaranteesAtLeastTwoFullTurns() {
        val total = RouletteMath.totalSlots(9)
        for (start in listOf(0f, 123.45f, 720.9f, -45f)) {
            for (target in 0 until 9) {
                val end = RouletteMath.targetAngle(start, target, total, 2)
                assertTrue(
                    "sweep=${end - start} from start=$start target=$target",
                    end - start >= 2f * 360f
                )
            }
        }
    }

    @Test
    fun targetAngleClampsFullTurnsParameterUpToTwo() {
        val total = RouletteMath.totalSlots(4)
        val end = RouletteMath.targetAngle(10f, 1, total, 0)
        assertTrue(end - 10f >= 720f)
    }

    @Test
    fun targetAngleIsStrictlyForwardMonotonic() {
        val total = RouletteMath.totalSlots(6)
        var previous = 0f
        for (roll in 0 until 10) {
            val target = (roll * 5) % 6
            val end = RouletteMath.targetAngle(previous, target, total, 2)
            assertTrue("roll $roll: $end must exceed $previous", end > previous)
            previous = end
        }
    }

    // --- totalSlots (mapeamento 1:1) ------------------------------------------

    @Test
    fun totalSlotsMatchesItemCount() {
        for (n in listOf(1, 2, 3, 4, 7, 10, 15, 21, 24, 50, 100)) {
            assertEquals("itemCount=$n", n, RouletteMath.totalSlots(n))
        }
    }

    @Test
    fun totalSlotsIsZeroForEmptyList() {
        assertEquals(0, RouletteMath.totalSlots(0))
    }

    // --- geometry -------------------------------------------------------------

    @Test
    fun radiusProducesExactChordForSlotHeight() {
        val theta = RouletteMath.thetaDegrees(21)
        val r = RouletteMath.radiusPx(100f, theta)
        assertTrue(r > 0f)
        val chord = 2f * r * tan(Math.toRadians(theta / 2.0).toFloat())
        assertTrue("chord=$chord", abs(chord - 100f) < 1e-2f)
    }

    @Test
    fun normalizeDegreesMapsIntoOpenMinus180To180() {
        assertEquals(0f, RouletteMath.normalizeDegrees(0f), 1e-4f)
        assertEquals(0f, RouletteMath.normalizeDegrees(360f), 1e-4f)
        assertEquals(90f, RouletteMath.normalizeDegrees(450f), 1e-4f)
        assertEquals(-90f, RouletteMath.normalizeDegrees(-90f), 1e-4f)
        assertEquals(180f, RouletteMath.normalizeDegrees(180f), 1e-4f)
        assertEquals(180f, RouletteMath.normalizeDegrees(-180f), 1e-4f)
    }

    // --- luminance / text color ------------------------------------------------

    @Test
    fun luminanceThresholdPicksBlackTextOnLightBases() {
        assertTrue(RouletteMath.isLightBase(0xFFFFFFFF))
        assertTrue(RouletteMath.isLightBase(0xFFD6E024))
        assertTrue(RouletteMath.isLightBase(0xFF4CAF50))
        assertTrue(RouletteMath.isLightBase(0xFFA8E01F))
        assertTrue(RouletteMath.isLightBase(0xFF8FA3B0))
    }

    @Test
    fun luminanceThresholdPicksWhiteTextOnDarkBases() {
        assertFalse(RouletteMath.isLightBase(0xFF000000))
        assertFalse(RouletteMath.isLightBase(0xFF9D4EDD))
        assertFalse(RouletteMath.isLightBase(0xFFE5484D))
    }

    @Test
    fun tealIsLightBase() {
        assertTrue(RouletteMath.isLightBase(0xFF2BD4C8))
    }

    @Test
    fun luminanceMatchesRec709() {
        assertEquals(0.2126f, RouletteMath.luminance(0xFFFF0000), 1e-3f)
        assertEquals(0.7152f, RouletteMath.luminance(0xFF00FF00), 1e-3f)
        assertEquals(0.0722f, RouletteMath.luminance(0xFF0000FF), 1e-3f)
    }

    // --- color helpers ----------------------------------------------------------

    @Test
    fun lerpArgbInterpolatesChannels() {
        assertEquals(0xFF000000L, RouletteMath.lerpArgb(0xFF000000L, 0xFFFFFFFFL, 0f))
        assertEquals(0xFFFFFFFFL, RouletteMath.lerpArgb(0xFF000000L, 0xFFFFFFFFL, 1f))
        val mid = RouletteMath.lerpArgb(0xFF000000L, 0xFEFEFEFEL, 0.5f)
        assertEquals(0xFF7F7F7FL, mid)
    }

    @Test
    fun brightenLightensTowardWhite() {
        val b = RouletteMath.brighten(0xFF808080L)
        assertTrue(RouletteMath.luminance(b) > RouletteMath.luminance(0xFF808080L))
        assertEquals(0xFFL, (b shr 24) and 0xFFL)
    }

    @Test
    fun paletteCyclesByIndex() {
        val first = RouletteMath.palettePair(0)
        assertEquals(first, RouletteMath.palettePair(RouletteMath.paletteSize()))
        assertEquals(first, RouletteMath.palettePair(2 * RouletteMath.paletteSize()))
    }
}
