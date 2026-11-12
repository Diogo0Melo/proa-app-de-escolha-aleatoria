package com.ogos.apprandomizador.core.ui.format

import com.ogos.apprandomizador.core.model.RaffleItem

/**
 * Matemática de cor pura sobre ARGB empacotado em `Long`, sem dependência de Compose.
 *
 * Vive no `core` porque a roleta (`features/randomize`) e o oráculo (`features/oracle`)
 * desenham os mesmos gradientes. Nenhuma feature precisa conhecer a outra.
 */
object ColorUtils {

    private const val WHITE_ARGB = 0xFFFFFFFFL
    private const val BLACK_ARGB = 0xFF000000L

    // Luminância Rec.709 (0..1)
    fun luminance(argb: Long): Float {
        val v = argb and 0xFFFFFFFFL
        val r = ((v shr 16) and 0xFF) / 255f
        val g = ((v shr 8) and 0xFF) / 255f
        val b = (v and 0xFF) / 255f
        return 0.2126f * r + 0.7152f * g + 0.0722f * b
    }

    fun isLightBase(argb: Long): Boolean = luminance(argb) >= 0.5f

    fun lerpArgb(from: Long, to: Long, t: Float): Long {
        val fromVal = from and 0xFFFFFFFFL
        val toVal = to and 0xFFFFFFFFL
        val k = t.coerceIn(0f, 1f)
        fun channel(shift: Int): Long {
            val a = ((fromVal shr shift) and 0xFF).toFloat()
            val b = ((toVal shr shift) and 0xFF).toFloat()
            return (a + (b - a) * k + 0.5f).toLong()
        }
        return (channel(24) shl 24) or (channel(16) shl 16) or
                (channel(8) shl 8) or channel(0)
    }

    private val PALETTE = listOf(
        0xFF9D4EDDL to 0xFF5B1A96L,
        0xFF4CAF50L to 0xFF1E5E22L,
        0xFFD6E024L to 0xFF8A8F1FL,
        0xFFE5484DL to 0xFF7A1215L,
        0xFF2BD4C8L to 0xFF0E8F86L,
        0xFF8FA3B0L to 0xFF4A5A66L,
        0xFFB07A50L to 0xFF6B4226L
    )

    fun paletteSize(): Int = PALETTE.size

    fun palettePair(index: Int): Pair<Long, Long> =
        PALETTE[((index % PALETTE.size) + PALETTE.size) % PALETTE.size]

    // Se o item tem cor própria usa ela; senão cicla a paleta por índice
    fun baseColorFor(item: RaffleItem, indexInList: Int): Long =
        if (item.color != 0L) item.color else palettePair(indexInList).first

    fun gradientFor(item: RaffleItem, indexInList: Int): Pair<Long, Long> {
        val base = item.color
        return if (base != 0L) {
            lerpArgb(base, WHITE_ARGB, 0.18f) to lerpArgb(base, BLACK_ARGB, 0.35f)
        } else {
            palettePair(indexInList)
        }
    }

    /**
     * Paleta visual exclusiva da roleta. Respostas verdes/vermelhas recebem
     * tons esmeralda/rubi consistentes; as demais cores mantêm o gradiente
     * original, sem alterar o contrato consumido por ListCollection e Oracle.
     */
    fun curatedGradientFor(item: RaffleItem, indexInList: Int): List<Long> {
        val hue = hueDegrees(item.color)
        val saturation = saturation(item.color)
        return when {
            item.color != 0L && saturation >= 0.35f && isGreenHue(hue) -> GREEN_GRADIENT
            item.color != 0L && saturation >= 0.35f && isRedHue(hue) -> RED_GRADIENT
            else -> {
                val (top, bottom) = gradientFor(item, indexInList)
                listOf(top, lerpArgb(top, bottom, 0.5f), bottom)
            }
        }
    }

    private fun isGreenHue(hue: Float): Boolean = hue in 90f..160f

    private fun isRedHue(hue: Float): Boolean = hue >= 345f || hue <= 15f

    private fun saturation(argb: Long): Float {
        val value = argb and 0xFFFFFFFFL
        val red = ((value shr 16) and 0xFF).toFloat() / 255f
        val green = ((value shr 8) and 0xFF).toFloat() / 255f
        val blue = (value and 0xFF).toFloat() / 255f
        val maximum = maxOf(red, green, blue)
        val minimum = minOf(red, green, blue)
        return if (maximum == 0f) 0f else (maximum - minimum) / maximum
    }

    private fun hueDegrees(argb: Long): Float {
        val value = argb and 0xFFFFFFFFL
        val red = ((value shr 16) and 0xFF).toFloat() / 255f
        val green = ((value shr 8) and 0xFF).toFloat() / 255f
        val blue = (value and 0xFF).toFloat() / 255f
        val maximum = maxOf(red, green, blue)
        val minimum = minOf(red, green, blue)
        val delta = maximum - minimum
        if (delta == 0f) return 0f

        val hue = when (maximum) {
            red -> 60f * (((green - blue) / delta) % 6f)
            green -> 60f * (((blue - red) / delta) + 2f)
            else -> 60f * (((red - green) / delta) + 4f)
        }
        return if (hue < 0f) hue + 360f else hue
    }

    private val GREEN_GRADIENT = listOf(
        0xFF2E7D32L,
        0xFF388E3CL,
        0xFF1B5E20L
    )

    private val RED_GRADIENT = listOf(
        0xFF7F1D1DL,
        0xFFB91C1CL,
        0xFF450A0AL
    )

    // Brightness ~1.15 sem RenderEffect (minSdk 27)
    fun brighten(argb: Long): Long = lerpArgb(argb, WHITE_ARGB, 0.15f)
}
