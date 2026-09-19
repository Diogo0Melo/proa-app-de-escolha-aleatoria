package com.ogos.apprandomizador.ui.view.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ogos.apprandomizador.model.RaffleItem
import com.ogos.apprandomizador.ui.theme.LocalRouletteBackground
import com.ogos.apprandomizador.viewmodel.SpinTarget
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

private const val MIN_TOTAL_SLOTS = 21
internal const val WHEEL_FULL_TURNS = 2
private const val SPIN_DURATION_MS = 2500

private const val MAX_VISIBLE_SLOT_OFFSET = 5f

private const val BACK_FACE_FADE_START_DEG = 70f
private const val BACK_FACE_LIMIT_DEG = 90f

private val CardShape = RoundedCornerShape(18.dp)
private const val WHITE_ARGB = 0xFFFFFFFFL
private const val BLACK_ARGB = 0xFF000000L
private val ITEM_VERTICAL_GAP = 6.dp

internal object RouletteMath {

    // Cilindro sempre com 21 slots → perspectiva idêntica pra qualquer lista
    fun totalSlots(itemCount: Int): Int =
        if (itemCount <= 0) 0 else MIN_TOTAL_SLOTS

    fun thetaDegrees(totalSlots: Int): Float = 360f / totalSlots

    // R = (altura + gap) / (2 * tan(θ/2)) — gap cria respiro entre cartões
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
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val itemCount = items.size
    val totalSlots = RouletteMath.totalSlots(itemCount)
    val theta = RouletteMath.thetaDegrees(totalSlots)

    val ringAngle = remember { Animatable(0f) }

    // Slot central discreto: recomposição só quando cruza fronteira de slot
    var centerSlot by remember { mutableIntStateOf(0) }
    LaunchedEffect(totalSlots) {
        if (totalSlots == 0) return@LaunchedEffect
        snapshotFlow { ringAngle.value }
            .map { angle ->
                (((angle / theta).roundToInt()) % totalSlots + totalSlots) % totalSlots
            }
            .distinctUntilChanged()
            .collect { centerSlot = it }
    }

    // Driver do giro: ângulo contínuo/monotônico entre giros
    LaunchedEffect(spinTarget?.requestId, totalSlots) {
        val target = spinTarget ?: return@LaunchedEffect
        if (totalSlots == 0) return@LaunchedEffect
        val slot = target.targetIndex % totalSlots
        val start = ringAngle.value
        val end = RouletteMath.targetAngle(start, slot, totalSlots, WHEEL_FULL_TURNS)
        ringAngle.animateTo(
            end,
            animationSpec = tween(
                durationMillis = SPIN_DURATION_MS,
                easing = CubicBezierEasing(0.12f, 0.8f, 0.2f, 1f)
            )
        )
        onSpinFinished()
    }

    BoxWithConstraints(modifier.background(LocalRouletteBackground.current)) {
        val containerHeightPx = constraints.maxHeight.toFloat()
        val cardHeightPx = max(
            min(with(density) { 96.dp.toPx() }, containerHeightPx * 0.11f),
            1f
        )
        val cardHeight = with(density) { cardHeightPx.toDp() }
        val gapPx = with(density) { ITEM_VERTICAL_GAP.toPx() }
        // slotHeight = altura do cartão + gap → cria respiro vertical
        val slotHeightPx = cardHeightPx + gapPx
        val cylinderRadius = RouletteMath.radiusPx(slotHeightPx, theta)
        // Câmera distante (~5R) evita distorção de perspectiva
        val cameraDistance = 5f * cylinderRadius

        Box(modifier = Modifier.fillMaxSize()) {
            for (slot in 0 until totalSlots) {
                val itemIndex = slot % itemCount
                RouletteCard(
                    slot = slot,
                    centerSlot = centerSlot,
                    totalSlots = totalSlots,
                    theta = theta,
                    cylinderRadius = cylinderRadius,
                    cameraDistance = cameraDistance,
                    cardHeight = cardHeight,
                    item = items[itemIndex],
                    itemIndex = itemIndex,
                    isCenter = centerHighlighted && slot == centerSlot,
                    ringAngle = ringAngle,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun RouletteCard(
    slot: Int,
    centerSlot: Int,
    totalSlots: Int,
    theta: Float,
    cylinderRadius: Float,
    cameraDistance: Float,
    cardHeight: Dp,
    item: RaffleItem,
    itemIndex: Int,
    isCenter: Boolean,
    ringAngle: Animatable<Float, AnimationVector1D>,
    modifier: Modifier = Modifier
) {
    val cardBaseColor = RouletteMath.baseColorFor(item, itemIndex)
    val (gradientTop, gradientBottom) = RouletteMath.gradientFor(item, itemIndex)
    val topColor = if (isCenter) RouletteMath.brighten(gradientTop) else gradientTop
    val bottomColor = if (isCenter) RouletteMath.brighten(gradientBottom) else gradientBottom
    val textColor = if (RouletteMath.isLightBase(cardBaseColor)) Color.Black else Color.White
    val glowColor = Color(cardBaseColor and 0xFFFFFFFFL)

    val rawSlotDistance = abs(slot - centerSlot)
    val slotDistance = min(rawSlotDistance, totalSlots - rawSlotDistance)

    Box(
        modifier = modifier
            .zIndex(-slotDistance.toFloat())
            .padding(horizontal = 28.dp)
            .height(cardHeight)
            .fillMaxWidth()
            .graphicsLayer {
                // Ângulo lido SOMENTE na draw phase (zero recomposição por frame)
                val relativeAngle = RouletteMath.normalizeDegrees(slot * theta - ringAngle.value)
                val radians = Math.toRadians(relativeAngle.toDouble())
                rotationX = relativeAngle
                translationY = (cylinderRadius * sin(radians)).toFloat()
                this.cameraDistance = cameraDistance
                val absoluteAngle = abs(relativeAngle)
                val offsetFromCenter = absoluteAngle / theta
                alpha = when {
                    offsetFromCenter > MAX_VISIBLE_SLOT_OFFSET || absoluteAngle >= BACK_FACE_LIMIT_DEG -> 0f
                    else -> {
                        val distanceFade = 1f - 0.55f *
                                (offsetFromCenter / MAX_VISIBLE_SLOT_OFFSET).coerceIn(0f, 1f)
                        val backFaceFade =
                            if (absoluteAngle <= BACK_FACE_FADE_START_DEG) 1f
                            else ((BACK_FACE_LIMIT_DEG - absoluteAngle) /
                                    (BACK_FACE_LIMIT_DEG - BACK_FACE_FADE_START_DEG)).coerceIn(
                                0f,
                                1f
                            )
                        distanceFade * backFaceFade
                    }
                }
            }
            .then(
                if (isCenter) {
                    Modifier.shadow(
                        elevation = 14.dp,
                        shape = CardShape,
                        ambientColor = glowColor,
                        spotColor = glowColor
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
            )
            .then(
                if (isCenter) {
                    Modifier.border(1.dp, Color.White.copy(alpha = 0.55f), CardShape)
                } else {
                    Modifier
                }
            )
            .drawBehind {
                if (isCenter) {
                    val tintRadius = size.maxDimension * 0.75f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(glowColor.copy(alpha = 0.35f), Color.Transparent),
                            center = center,
                            radius = tintRadius
                        ),
                        radius = tintRadius
                    )
                }
            },
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
