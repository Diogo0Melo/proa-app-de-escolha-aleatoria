package com.ogos.apprandomizador.features.fingers.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ogos.apprandomizador.R
import com.ogos.apprandomizador.features.fingers.domain.FingersDirection
import kotlin.math.roundToInt

/** Diâmetro da bolinha: folgado para sobrar cor em volta da ponta do dedo. */
private val FINGER_SIZE = 150.dp

/** Escala máxima da vencedora durante o pulso. */
private const val PULSE_SCALE = 1.18f

/** Opacidade das bolinhas que não venceram. */
private const val DIMMED_ALPHA = 0.35f

@Composable
fun FingersRoute(viewModel: FingersViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Sair da tela no meio da contagem ou do resultado zera o estado. Assim nada
    // continua rodando em segundo plano nem revela um resultado que ninguém vê.
    DisposableEffect(viewModel) {
        onDispose { viewModel.onReset() }
    }

    FingersScreen(
        state = state,
        onFingerDown = viewModel::onFingerDown,
        onFingerMove = viewModel::onFingerMove,
        onFingerUp = viewModel::onFingerUp,
        onReset = viewModel::onReset,
        modifier = modifier
    )
}

@Composable
fun FingersScreen(
    state: FingersUiState,
    onFingerDown: (Long, Offset) -> Unit,
    onFingerMove: (Long, Offset) -> Unit,
    onFingerUp: (Long) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    // O gesto roda uma vez só. Os callbacks ficam atualizados sem reiniciá-lo.
    // O ViewModel decide o que cada evento significa em cada fase.
    val currentDown by rememberUpdatedState(onFingerDown)
    val currentMove by rememberUpdatedState(onFingerMove)
    val currentUp by rememberUpdatedState(onFingerUp)

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent().changes.forEach { change ->
                            when {
                                change.pressed && !change.previousPressed ->
                                    currentDown(change.id.value, change.position)
                                !change.pressed && change.previousPressed ->
                                    currentUp(change.id.value)
                                change.pressed && change.position != change.previousPosition ->
                                    currentMove(change.id.value, change.position)
                                else -> Unit
                            }
                        }
                    }
                }
            }
    ) {
        if (state.phase == FingersPhase.Idle) {
            FingersInstructions(modifier = Modifier.align(Alignment.Center))
        }

        state.fingers.forEachIndexed { index, finger ->
            key(finger.id) {
                FingerBubble(
                    finger = finger,
                    index = index,
                    isWinner = state.phase == FingersPhase.Revealed && finger.id == state.winnerId,
                    isDimmed = state.phase == FingersPhase.Revealed && finger.id != state.winnerId
                )
            }
        }

        if (state.phase == FingersPhase.Counting) {
            FingersCountdownPopup(countdown = state.countdown, fingerCount = state.fingers.size)
        }

        val winner = state.winner
        val direction = state.winnerDirection
        if (state.phase == FingersPhase.Revealed && winner != null && direction != null) {
            FingersResultPopup(winner = winner, direction = direction, onDismiss = onReset)
        }
    }
}

@Composable
private fun FingersInstructions(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FingerprintBadge(badgeSize = 96.dp, iconSize = 52.dp, borderWidth = 2.dp)
        Text(
            stringResource(R.string.fingers_hint_place),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Text(
            stringResource(R.string.fingers_hint_after),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/** Escolhe a versão animada só para a vencedora. As demais não rodam animação infinita. */
@Composable
private fun FingerBubble(finger: FingerState, index: Int, isWinner: Boolean, isDimmed: Boolean) {
    if (isWinner) {
        val transition = rememberInfiniteTransition(label = "finger-pulse")
        val pulse by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "finger-pulse-progress"
        )
        BubbleBody(
            finger = finger,
            index = index,
            isWinner = true,
            pulseScale = 1f + (PULSE_SCALE - 1f) * pulse,
            glow = pulse,
            bubbleAlpha = 1f
        )
    } else {
        BubbleBody(
            finger = finger,
            index = index,
            isWinner = false,
            pulseScale = 1f,
            glow = 0f,
            bubbleAlpha = if (isDimmed) DIMMED_ALPHA else 1f
        )
    }
}

@Composable
private fun BubbleBody(
    finger: FingerState,
    index: Int,
    isWinner: Boolean,
    pulseScale: Float,
    glow: Float,
    bubbleAlpha: Float
) {
    val base = Color(finger.color)
    val description = stringResource(R.string.fingers_finger_description, index + 1)

    Box(
        modifier = Modifier
            // Lambda: a posição só é lida na fase de layout, sem recompor a cada frame.
            .offset {
                val radius = FINGER_SIZE.toPx() / 2f
                IntOffset(
                    x = (finger.position.x - radius).roundToInt(),
                    y = (finger.position.y - radius).roundToInt()
                )
            }
            .size(FINGER_SIZE)
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
                alpha = bubbleAlpha
            }
            .shadow(
                elevation = 24.dp * glow,
                shape = CircleShape,
                ambientColor = base,
                spotColor = base
            )
            .background(
                brush = Brush.radialGradient(
                    listOf(base.copy(alpha = 0.9f), base.copy(alpha = 0.6f))
                ),
                shape = CircleShape
            )
            .border(
                width = if (isWinner) 4.dp else 3.dp,
                color = Color.White.copy(alpha = if (isWinner) 0.95f else 0.6f),
                shape = CircleShape
            )
            .semantics { contentDescription = description }
    )
}

@Composable
private fun FingerprintBadge(badgeSize: Dp, iconSize: Dp, borderWidth: Dp) {
    Box(
        modifier = Modifier
            .size(badgeSize)
            .border(borderWidth, MaterialTheme.colorScheme.primary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.Fingerprint,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
private fun FingersCountdownPopup(countdown: Int, fingerCount: Int) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 6.dp,
            shadowElevation = 12.dp,
            modifier = Modifier.padding(24.dp).widthIn(max = 320.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    stringResource(R.string.fingers_ready),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    countdown.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    stringResource(R.string.fingers_ready_hint, fingerCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
@Composable
private fun FingersResultPopup(
    winner: FingerState,
    direction: FingersDirection,
    onDismiss: () -> Unit
) {
    // O ícone gira no sentido sorteado. Horário gira positivo, anti-horário negativo.
    val transition = rememberInfiniteTransition(label = "direction-arrow")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (direction == FingersDirection.CLOCKWISE) 360f else -360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "direction-arrow-rotation"
    )
    val winnerColor = Color(winner.color)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            // Engole os toques para que nada passe para a tela de baixo.
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(24.dp).widthIn(max = 340.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: título sempre no centro geométrico da Card.
                // A Box separa as extremidades (badge + X) do texto: o Text
                // é o único filho fora da Row, então contentAlignment = Center
                // o posiciona no meio exato, sem depender da largura dos ícones.
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FingerprintBadge(badgeSize = 40.dp, iconSize = 24.dp, borderWidth = 1.dp)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = stringResource(R.string.close)
                            )
                        }
                    }
                    // Padding horizontal de segurança: se o título crescer
                    // (outra fonte, texto longo), encolhe com ellipsis em vez
                    // de vazar por baixo do badge ou do X.
                    Text(
                        text = stringResource(R.string.fingers_selected_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 56.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(20.dp, CircleShape, ambientColor = winnerColor, spotColor = winnerColor)
                            .background(
                                brush = Brush.radialGradient(
                                    listOf(winnerColor, winnerColor.copy(alpha = 0.65f))
                                ),
                                shape = CircleShape
                            )
                            .border(3.dp, Color.White.copy(alpha = 0.9f), CircleShape)
                    )
                    Icon(
                        Icons.Filled.Autorenew,
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .graphicsLayer { rotationZ = rotation },
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    stringResource(R.string.fingers_direction_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    stringResource(
                        if (direction == FingersDirection.CLOCKWISE) {
                            R.string.fingers_direction_cw
                        } else {
                            R.string.fingers_direction_ccw
                        }
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = CircleShape
                ) {
                    Text(stringResource(R.string.fingers_exit))
                }
            }
        }
    }
}
