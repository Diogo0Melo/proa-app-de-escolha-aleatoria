package com.ogos.apprandomizador.features.tarot.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ogos.apprandomizador.features.tarot.domain.DrawnCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val APPEAR_MS = 350
private const val BEFORE_FLIP_MS = 250L
private const val FLIP_MS = 600
private const val HOLD_MS = 900L
private const val DISCARD_MS = 500
private val CARD_WIDTH = 240.dp

/**
 * Pop-up em tela cheia com uma carta por vez: aparece virada, revela-se e é descartada para baixo.
 * Quando a carta sai da tela, [onLanded] é chamado com o índice e ela passa a ocupar seu espaço fixo.
 * O pop-up é modal: toques não passam para a tela de baixo.
 */
@Composable
internal fun TarotRevealOverlay(
    card: DrawnCard,
    index: Int,
    onFlipped: (Int) -> Unit,
    onLanded: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Uma sequência completa por carta: trocar o índice reinicia a animação do zero.
    key(index) {
        RevealStep(card = card, index = index, onFlipped = onFlipped, onLanded = onLanded, modifier = modifier)
    }
}

@Composable
private fun RevealStep(
    card: DrawnCard,
    index: Int,
    onFlipped: (Int) -> Unit,
    onLanded: (Int) -> Unit,
    modifier: Modifier
) {
    val scrim = remember { Animatable(0f) }
    val lift = remember { Animatable(0f) }
    val flip = remember { Animatable(0f) }
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current.density
    val currentOnFlipped by rememberUpdatedState(onFlipped)
    val currentOnLanded by rememberUpdatedState(onLanded)
    val announcement = revealAnnouncement(card)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .drawBehind { drawRect(Color.Black, alpha = 0.85f * scrim.value) }
            // O pop-up é modal: consome os toques para que a tela de baixo não receba nenhum.
            .pointerInput(Unit) { detectTapGestures { } }
    ) {
        val exitPx = with(LocalDensity.current) { maxHeight.toPx() }

        LaunchedEffect(Unit) {
            scrim.animateTo(1f, tween(APPEAR_MS))
            delay(BEFORE_FLIP_MS)
            var notified = false
            flip.animateTo(180f, tween(FLIP_MS)) {
                // Feedback tátil e sonoro no meio da rotação, quando a face fica visível.
                if (!notified && value >= 90f) {
                    notified = true
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    currentOnFlipped(index)
                }
            }
            delay(HOLD_MS)
            launch { scrim.animateTo(0f, tween(DISCARD_MS)) }
            lift.animateTo(exitPx, tween(DISCARD_MS, easing = FastOutSlowInEasing))
            currentOnLanded(index)
        }

        // A descida atinge o conjunto inteiro (rótulo + carta), para que ele saia da tela junto.
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer { translationY = lift.value }
                .semantics(mergeDescendants = true) {
                    contentDescription = announcement
                    liveRegion = LiveRegionMode.Polite
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                positionLabel(card.position).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Box(
                modifier = Modifier
                    .width(CARD_WIDTH)
                    .graphicsLayer {
                        rotationY = flip.value
                        cameraDistance = 16f * density
                    }
            ) {
                if (flip.value < 90f) {
                    TarotCardBack(Modifier.fillMaxWidth())
                } else {
                    // Gira de novo para compensar o espelhamento da rotação do contêiner.
                    TarotCardFace(card, Modifier.fillMaxWidth().graphicsLayer { rotationY = 180f })
                }
            }
        }
    }
}
