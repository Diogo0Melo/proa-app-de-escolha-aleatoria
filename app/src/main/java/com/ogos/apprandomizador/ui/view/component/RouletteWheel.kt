package com.ogos.apprandomizador.ui.view.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ogos.apprandomizador.model.RaffleItem
import com.ogos.apprandomizador.ui.theme.LocalRouletteBackground
import com.ogos.apprandomizador.viewmodel.SpinTarget
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.tan

internal const val MAX_DISPLAY_SLOTS = 24
internal const val WHEEL_FULL_TURNS = 2

// Física do giro: 3000ms com ease-out forte (partida rápida, frenagem longa)
private const val SPIN_DURATION_MS = 3000
private val SPIN_EASING = CubicBezierEasing(0.16f, 0.84f, 0.28f, 1.0f)

// Percurso do giro em itens (aleatório entre 8 e 16 itens por giro)
private val SPIN_TRAVEL_RANGE = 8..16

// Carrossel pseudo-cilíndrico por JANELA DE POSIÇÃO: position é contínua em
// unidades de item (0f = item 0 centralizado) e uma instância é renderizada
// enquanto abs(distância) <= 4.5. O MESMO item pode aparecer várias vezes na
// janela — essencial para listas de 2–3 itens preencherem o cilindro.
private const val VISIBLE_WINDOW_HALF = 4.5f

// Pitch vertical = slotHeight * PITCH_MULTIPLIER
private const val PITCH_MULTIPLIER = 0.92f

// Profundidade de câmera ≈ 900–1200px (400.dp: ~800px @2x, ~1200px @3x)
private val CAMERA_DISTANCE = 400.dp

// Rampas contínuas (interpolação linear entre chaves) por abs(distância).
// Inclinação: 0→0°, 1→17°, 2→34°, 3→49°, 4→61°; clamp final em ±70°
private val TILT_KEYS = floatArrayOf(0f, 1f, 2f, 3f, 4f)
private val TILT_VALUES = floatArrayOf(0f, 17f, 34f, 49f, 61f)
private const val TILT_CLAMP_DEGREES = 70f

// Escala: 1.00 → 0.86 → 0.70 → 0.56 → 0.48; além de 4 segura em 0.45
private val SCALE_KEYS = floatArrayOf(0f, 1f, 2f, 3f, 4f)
private val SCALE_VALUES = floatArrayOf(1f, 0.86f, 0.70f, 0.56f, 0.48f)
private const val SCALE_BEYOND_WINDOW = 0.45f

// Escuridão: mistura das cores na direção do preto (gradiente E texto);
// 0→0.00, 1→0.42, 2→0.68, 3→0.86, 4→0.94; além de 4 segura em 0.97
private val DARKNESS_KEYS = floatArrayOf(0f, 1f, 2f, 3f, 4f)
private val DARKNESS_VALUES = floatArrayOf(0f, 0.42f, 0.68f, 0.86f, 0.94f)
private const val DARKNESS_BEYOND_WINDOW = 0.97f

private val CardShape = RoundedCornerShape(20.dp)
private const val WHITE_ARGB = 0xFFFFFFFFL
private const val BLACK_ARGB = 0xFF000000L
private val ITEM_VERTICAL_GAP = 12.dp

// Margem lateral dos cartões dentro da roleta
private val ITEM_HORIZONTAL_PADDING = 20.dp

// Métrica do cartão: base 104dp, limitado pelo container e clampado em [96, 110]
private val CARD_HEIGHT_BASE = 104.dp
private val CARD_HEIGHT_MIN = 96.dp
private val CARD_HEIGHT_MAX = 110.dp

// Micro-pop do vencedor: 1.04 -> 1.0 em 150ms ao virar o centro (sem overshoot)
private const val POP_SCALE = 1.04f
private const val POP_DURATION_MS = 150

// Elevação do cartão central: sombra preta suave (sem glow colorido)
private val CENTER_SHADOW_ELEVATION = 12.dp

// Interpolação linear entre chaves; abaixo da 1ª chave usa o 1º valor,
// acima da última usa o último.
private fun ramp(keys: FloatArray, values: FloatArray, x: Float): Float {
    if (x <= keys.first()) return values.first()
    if (x >= keys.last()) return values.last()
    var i = 1
    while (x > keys[i]) i++
    val t = (x - keys[i - 1]) / (keys[i] - keys[i - 1])
    return values[i - 1] + (values[i] - values[i - 1]) * t
}

// Como ramp(), mas acima da última chave segura em `beyond` (não no último valor)
private fun rampBeyond(keys: FloatArray, values: FloatArray, beyond: Float, x: Float): Float =
    if (x > keys.last()) beyond else ramp(keys, values, x)

internal object RouletteMath {

    // O cilindro renderiza a sequência de exibição 1:1; o teto de 24 slots
    // (MAX_DISPLAY_SLOTS) é garantido a montante por quem monta a sequência.
    fun totalSlots(itemCount: Int): Int = itemCount

    fun thetaDegrees(totalSlots: Int): Float = 360f / totalSlots

    // R = (altura + gap) / (2 * tan(θ/2)) — gap cria respiro entre cartões
    // (mantido por contrato de teste; o render atual usa pitch uniforme, não raio)
    fun radiusPx(slotHeightPx: Float, thetaDegrees: Float): Float {
        val halfThetaRad = Math.toRadians(thetaDegrees.toDouble() / 2.0)
        return slotHeightPx / (2f * tan(halfThetaRad).toFloat())
    }

    fun normalizeDegrees(angleDegrees: Float): Float {
        val m = angleDegrees % 360f
        return when {
            m > 180f -> m - 360f
            m <= -180f -> m + 360f
            else -> m
        }
    }

    // Ângulo absoluto que coloca targetSlot no centro, garantindo >= fullTurns voltas
    fun targetAngle(
        currentAngle: Float,
        targetSlot: Int,
        totalSlots: Int,
        fullTurns: Int
    ): Float {
        val theta = thetaDegrees(totalSlots)
        val turns = max(2, fullTurns)
        val desired = (targetSlot * theta) % 360f
        val cur = currentAngle % 360f
        var delta = (desired - cur) % 360f
        if (delta < 0f) delta += 360f
        return currentAngle + turns * 360f + delta
    }

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

    // Brightness ~1.15 sem RenderEffect (minSdk 27)
    fun brighten(argb: Long): Long = lerpArgb(argb, WHITE_ARGB, 0.15f)
}

@Composable
fun RouletteWheel(
    items: List<RaffleItem>,
    spinTarget: SpinTarget?,
    centerHighlighted: Boolean,
    onSpinFinished: () -> Unit,
    onSlotCrossed: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val totalItems = items.size

    // Posição contínua em unidades de item: position = 0f centraliza o item 0.
    // O giro anima position; todos os transforms por frame derivam dela.
    val position = remember { Animatable(0f) }

    // Slot central discreto: recomposição/tick só quando cruza fronteira inteira
    var centerSlot by remember { mutableIntStateOf(0) }
    val currentOnSlotCrossed by rememberUpdatedState(onSlotCrossed)
    LaunchedEffect(totalItems) {
        if (totalItems == 0) return@LaunchedEffect
        snapshotFlow { position.value }
            .map { pos -> ((pos.roundToInt()) % totalItems + totalItems) % totalItems }
            .distinctUntilChanged()
            .collect {
                centerSlot = it
                currentOnSlotCrossed?.invoke()
            }
    }

    // Sinal de pouso: true somente após animateTo completar (micro-pop usa isso)
    var justLanded by remember { mutableStateOf(false) }

    // Driver do giro: percurso de 8–16 itens + correção modular para pousar
    // exatamente no vencedor; posição cresce monotonicamente entre giros.
    LaunchedEffect(spinTarget?.requestId, totalItems) {
        val target = spinTarget ?: return@LaunchedEffect
        if (totalItems == 0) {
            onSpinFinished(); return@LaunchedEffect
        }
        val winnerIndex = items.indexOf(target.targetItem)
        if (winnerIndex < 0) {
            onSpinFinished(); return@LaunchedEffect
        }
        justLanded = false  // reset no início do giro
        val currentInt = position.value.roundToInt()
        val baseTravel = SPIN_TRAVEL_RANGE.random()
        val rawTarget = currentInt + baseTravel
        val remainder = ((rawTarget % totalItems) + totalItems) % totalItems
        val delta = ((winnerIndex - remainder) % totalItems + totalItems) % totalItems
        val finalTarget = (rawTarget + delta).toFloat()
        position.animateTo(
            finalTarget,
            animationSpec = tween(
                durationMillis = SPIN_DURATION_MS,
                easing = SPIN_EASING
            )
        )
        justLanded = true  // pouso completo — micro-pop pode disparar
        onSpinFinished()
    }

    BoxWithConstraints(modifier.background(LocalRouletteBackground.current)) {
        if (totalItems > 0) {
            val containerHeightPx = constraints.maxHeight.toFloat()
            // Altura do cartão: base 104dp, teto responsivo (11% do container),
            // clamp final em [96dp, 110dp]
            val cardHeightPx = with(density) {
                min(CARD_HEIGHT_BASE.toPx(), containerHeightPx * 0.11f)
                    .coerceIn(CARD_HEIGHT_MIN.toPx(), CARD_HEIGHT_MAX.toPx())
            }
            val gapPx = with(density) { ITEM_VERTICAL_GAP.toPx() }
            // slotHeight = altura do cartão + gap → pitch uniforme do carrossel
            val slotHeightPx = cardHeightPx + gapPx
            val pitchPx = slotHeightPx * PITCH_MULTIPLIER
            // Câmera fixa (~900–1200px) substitui a geometria de cilindro verdadeiro;
            // RouletteMath.radiusPx permanece disponível mas não é usada no render.
            val cameraDistancePx = with(density) { CAMERA_DISTANCE.toPx() }

            // Janela de posição visível: renderiza por POSIÇÃO CONTÍNUA, não por
            // índice de item — cada slotIndex mapeia no item via módulo, então o
            // mesmo item aparece várias vezes na janela (listas de 2–3 itens
            // produzem ~10 instâncias e preenchem o cilindro).
            val pos = position.value
            val firstVisible = floor(pos - VISIBLE_WINDOW_HALF).toInt()
            val lastVisible = ceil(pos + VISIBLE_WINDOW_HALF).toInt()
            val nearestSlotIndex = pos.roundToInt()

            Box(modifier = Modifier.fillMaxSize()) {
                for (slotIndex in firstVisible..lastVisible) {
                    val distance = slotIndex.toFloat() - pos
                    if (abs(distance) > VISIBLE_WINDOW_HALF) continue
                    val itemIndex = ((slotIndex % totalItems) + totalItems) % totalItems
                    // key = slotIndex (posição absoluta), NÃO itemIndex: instâncias
                    // repetidas do mesmo item precisam de identidade própria no Compose
                    key(slotIndex) {
                        RouletteCard(
                            slotIndex = slotIndex,
                            position = position,
                            cameraDistance = cameraDistancePx,
                            pitchPx = pitchPx,
                            cardHeightPx = cardHeightPx,
                            item = items[itemIndex],
                            itemIndex = itemIndex,
                            isCenter = slotIndex == nearestSlotIndex,
                            centerHighlighted = centerHighlighted,
                            justLanded = justLanded,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RouletteCard(
    slotIndex: Int,
    position: Animatable<Float, AnimationVector1D>,
    cameraDistance: Float,
    pitchPx: Float,
    cardHeightPx: Float,
    item: RaffleItem,
    itemIndex: Int,
    isCenter: Boolean,
    centerHighlighted: Boolean,
    justLanded: Boolean,
    modifier: Modifier = Modifier
) {
    val cardBaseColor = RouletteMath.baseColorFor(item, itemIndex)
    val (gradientTop, gradientBottom) = RouletteMath.gradientFor(item, itemIndex)

    // Distância contínua assinada em unidades de item — mistura de cores acontece
    // aqui (composition); os transforms releem position na draw phase.
    val distance = slotIndex.toFloat() - position.value
    val absDistance = abs(distance)
    // Escuridão por mistura na direção do preto — sem overlay, sem alpha
    val darkness = rampBeyond(DARKNESS_KEYS, DARKNESS_VALUES, DARKNESS_BEYOND_WINDOW, absDistance)
    val topColor = RouletteMath.lerpArgb(gradientTop, BLACK_ARGB, darkness)
    val bottomColor = RouletteMath.lerpArgb(gradientBottom, BLACK_ARGB, darkness)

    // Centro destacado: branco bold sempre (ignora luminância) + sombra preta
    val highlighted = isCenter && centerHighlighted
    // Escuridão do texto via mistura de cor (não alpha) — spec compliance
    val textColor = when {
        highlighted -> Color.White
        RouletteMath.isLightBase(cardBaseColor) ->
            Color(RouletteMath.lerpArgb(0xFF000000L, BLACK_ARGB, darkness * 0.5f))

        else -> Color(RouletteMath.lerpArgb(WHITE_ARGB, BLACK_ARGB, darkness * 0.85f))
    }
    // Micro-pop do vencedor: dispara SOMENTE ao pousar (justLanded), não durante
    // o giro. centerHighlighted pode ser true durante o spin — justLanded garante
    // que o pop só acontece quando a animação termina.
    val pop = remember { Animatable(1f) }
    LaunchedEffect(justLanded, isCenter) {
        if (justLanded && isCenter) {
            pop.snapTo(POP_SCALE)
            pop.animateTo(1f, tween(POP_DURATION_MS))
        }
    }

    Box(
        modifier = modifier
            .zIndex(-absDistance)
            .padding(horizontal = ITEM_HORIZONTAL_PADDING)
            .height(with(LocalDensity.current) { cardHeightPx.toDp() })
            .fillMaxWidth()
            .graphicsLayer {
                // Distância relida SOMENTE na draw phase: os transforms acompanham
                // position a cada frame sem custo extra de recomposição.
                val d = slotIndex.toFloat() - position.value
                // Inclinação por distância: cilindro CONVEXO — abaixo do centro
                // inclina para trás (negativa), acima inclina para frente (positiva)
                val tilt = ramp(TILT_KEYS, TILT_VALUES, abs(d))
                rotationX = (if (d < 0f) tilt else -tilt)
                    .coerceIn(-TILT_CLAMP_DEGREES, TILT_CLAMP_DEGREES)
                // Pilha vertical uniforme (window picker); sem soft-cap dentro de ±4.5
                translationY = d * pitchPx
                this.cameraDistance = cameraDistance
                // Escala contínua por distância; micro-pop multiplicado no centro
                val scale =
                    rampBeyond(SCALE_KEYS, SCALE_VALUES, SCALE_BEYOND_WINDOW, abs(d)) * pop.value
                scaleX = scale
                scaleY = scale
                // Instâncias já são culled em ±4.5 — sempre opacas
                alpha = 1f
            }
            .then(
                if (highlighted) {
                    // Centro: sombra preta suave — sem borda, sem glow colorido
                    Modifier.shadow(
                        elevation = CENTER_SHADOW_ELEVATION,
                        shape = CardShape,
                        ambientColor = Color.Black.copy(alpha = 0.4f),
                        spotColor = Color.Black.copy(alpha = 0.4f)
                    )
                } else {
                    Modifier
                }
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(topColor),
                        Color(bottomColor)
                    )
                ),
                shape = CardShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = item.name,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}
