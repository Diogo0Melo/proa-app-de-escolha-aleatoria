package com.ogos.apprandomizador.core.ui.format

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorUtilsTest {

    @Test
    fun luminanceThresholdPicksBlackTextOnLightBases() {
        assertTrue(ColorUtils.isLightBase(0xFFFFFFFF))
        assertTrue(ColorUtils.isLightBase(0xFFD6E024))
        assertTrue(ColorUtils.isLightBase(0xFF4CAF50))
        assertTrue(ColorUtils.isLightBase(0xFFA8E01F))
        assertTrue(ColorUtils.isLightBase(0xFF8FA3B0))
    }

    @Test
    fun luminanceThresholdPicksWhiteTextOnDarkBases() {
        assertFalse(ColorUtils.isLightBase(0xFF000000))
        assertFalse(ColorUtils.isLightBase(0xFF9D4EDD))
        assertFalse(ColorUtils.isLightBase(0xFFE5484D))
    }

    @Test
    fun tealIsLightBase() {
        assertTrue(ColorUtils.isLightBase(0xFF2BD4C8))
    }

    @Test
    fun luminanceMatchesRec709() {
        assertEquals(0.2126f, ColorUtils.luminance(0xFFFF0000), 1e-3f)
        assertEquals(0.7152f, ColorUtils.luminance(0xFF00FF00), 1e-3f)
        assertEquals(0.0722f, ColorUtils.luminance(0xFF0000FF), 1e-3f)
    }

    @Test
    fun lerpArgbInterpolatesChannels() {
        assertEquals(0xFF000000L, ColorUtils.lerpArgb(0xFF000000L, 0xFFFFFFFFL, 0f))
        assertEquals(0xFFFFFFFFL, ColorUtils.lerpArgb(0xFF000000L, 0xFFFFFFFFL, 1f))
        val mid = ColorUtils.lerpArgb(0xFF000000L, 0xFEFEFEFEL, 0.5f)
        assertEquals(0xFF7F7F7FL, mid)
    }

    @Test
    fun brightenLightensTowardWhite() {
        val b = ColorUtils.brighten(0xFF808080L)
        assertTrue(ColorUtils.luminance(b) > ColorUtils.luminance(0xFF808080L))
        assertEquals(0xFFL, (b shr 24) and 0xFFL)
    }

    @Test
    fun paletteCyclesByIndex() {
        val first = ColorUtils.palettePair(0)
        assertEquals(first, ColorUtils.palettePair(ColorUtils.paletteSize()))
        assertEquals(first, ColorUtils.palettePair(2 * ColorUtils.paletteSize()))
    }
}
