package com.ogos.apprandomizador.modes.ui.tarot

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.ogos.apprandomizador.R
import coil.compose.AsyncImage
import com.ogos.apprandomizador.modes.domain.DrawnCard
import com.ogos.apprandomizador.modes.domain.Suit
import com.ogos.apprandomizador.modes.domain.SpreadPosition
import kotlinx.coroutines.delay

@Composable
internal fun TarotCardView(
    drawn: DrawnCard,
    requestId: Long,
    revealed: Boolean,
    animate: Boolean,
    startDelayMillis: Int,
    animationDurationMillis: Int,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation = remember(requestId) { Animatable(0f) }
    var showFront by remember(requestId) { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val currentOnFlip by rememberUpdatedState(onFlip)
    // Falha de carregamento troca para o desenho tipográfico; o estado é por carta e por verso.
    var frontImageFailed by remember(drawn.card.id) { mutableStateOf(false) }
    var backImageFailed by remember { mutableStateOf(false) }
    val frontImage = remember(drawn.card.id) { TarotCardImages.assetPathFor(drawn.card.id)?.let(TarotCardImages::assetUri) }
    val backImage = remember { TarotCardImages.assetUri(TarotCardImages.CARD_BACK_PATH) }

    LaunchedEffect(requestId, revealed, animate) {
        if (!revealed) {
            rotation.snapTo(0f)
            showFront = false
        } else if (!animate) {
            rotation.snapTo(180f)
            showFront = true
        } else {
            delay(startDelayMillis.toLong())
            var feedbackSent = false
            rotation.animateTo(
                targetValue = 180f,
                animationSpec = tween(
                    durationMillis = animationDurationMillis.coerceAtLeast(1),
                    easing = FastOutSlowInEasing
                )
            ) {
                if (!feedbackSent && value >= 90f) {
                    feedbackSent = true
                    showFront = true
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    currentOnFlip()
                }
            }
            showFront = true
        }
    }

    val position = when (drawn.position) {
        SpreadPosition.SINGLE -> stringResource(R.string.tarot_position_single)
        SpreadPosition.PAST -> stringResource(R.string.tarot_position_past)
        SpreadPosition.PRESENT -> stringResource(R.string.tarot_position_present)
        SpreadPosition.FUTURE -> stringResource(R.string.tarot_position_future)
    }
    val reversedLabel = stringResource(R.string.tarot_reversed).lowercase()
    val keywordsLabel = stringResource(R.string.tarot_keywords)
    val description = if (showFront) {
        buildString {
            append(position)
            append(": ")
            append(drawn.card.name)
            if (drawn.reversed) append(", ").append(reversedLabel)
            append(". ")
            append(keywordsLabel)
            append(": ")
            append(drawn.meaning.keywords.joinToString(", "))
        }
    } else {
        stringResource(R.string.tarot_card_hidden)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                rotationY = rotation.value
                cameraDistance = 12f * density.density
            }
            .semantics(mergeDescendants = true) { contentDescription = description },
        colors = CardDefaults.cardColors(
            containerColor = if (showFront) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { rotationY = if (showFront) 180f else 0f }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (showFront) {
                val image = frontImage?.takeUnless { frontImageFailed }
                if (image != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(position.uppercase(), style = MaterialTheme.typography.labelMedium)
                        AsyncImage(
                            model = image,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            onError = { frontImageFailed = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(CARD_ASPECT)
                                .graphicsLayer { rotationZ = if (drawn.reversed) 180f else 0f }
                        )
                        Text(drawn.card.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (drawn.reversed) {
                            Text(
                                stringResource(R.string.tarot_reversed),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(position.uppercase(), style = MaterialTheme.typography.labelMedium)
                        // Números romanos só fazem sentido nos Arcanos Maiores; os Menores já trazem o nome completo.
                        if (drawn.card.suit == Suit.MAJOR) {
                            Text(toRoman(drawn.card.number), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        }
                        Text(drawn.card.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (drawn.reversed) {
                            Text(
                                stringResource(R.string.tarot_reversed),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Text(drawn.meaning.keywords.joinToString(" · "), style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else if (!backImageFailed) {
                AsyncImage(
                    model = backImage,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    onError = { backImageFailed = true },
                    modifier = Modifier.fillMaxWidth().aspectRatio(CARD_ASPECT)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(stringResource(R.string.tarot_card_back), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("✦  ✧  ✦", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}

/** Proporção das imagens de carta (424×632), para não distorcer a arte. */
private const val CARD_ASPECT = 424f / 632f

private fun toRoman(number: Int): String {
    if (number == 0) return "0"
    val values = listOf(1000 to "M", 900 to "CM", 500 to "D", 400 to "CD", 100 to "C", 90 to "XC", 50 to "L", 40 to "XL", 10 to "X", 9 to "IX", 5 to "V", 4 to "IV", 1 to "I")
    var remaining = number
    return buildString {
        values.forEach { (value, symbol) ->
            while (remaining >= value) {
                append(symbol)
                remaining -= value
            }
        }
    }
}
