package com.ogos.apprandomizador.features.tarot.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ogos.apprandomizador.R
import com.ogos.apprandomizador.features.tarot.domain.DrawnCard
import com.ogos.apprandomizador.features.tarot.domain.SpreadPosition
import com.ogos.apprandomizador.features.tarot.domain.Suit

/** Proporção das imagens de carta (424×632), para não distorcer a arte. */
internal const val CARD_ASPECT = 424f / 632f

internal val CardShape = RoundedCornerShape(12.dp)

/** Face da carta: imagem quando existe e carrega; caso contrário, desenho tipográfico. */
@Composable
internal fun TarotCardFace(drawn: DrawnCard, modifier: Modifier = Modifier) {
    val image = remember(drawn.card.id) { TarotCardImages.assetPathFor(drawn.card.id)?.let(TarotCardImages::assetUri) }
    var imageFailed by remember(drawn.card.id) { mutableStateOf(false) }
    val picture = image?.takeUnless { imageFailed }
    Box(
        modifier = modifier
            .aspectRatio(CARD_ASPECT)
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CardShape)
    ) {
        if (picture != null) {
            AsyncImage(
                model = picture,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                onError = { imageFailed = true },
                // Carta invertida: a arte gira 180°; o rótulo "Invertida" aparece no fallback e na folha de detalhe.
                modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = if (drawn.reversed) 180f else 0f }
            )
        } else {
            CardTypography(drawn, Modifier.fillMaxSize().padding(10.dp))
        }
    }
}

/** Verso da carta, usado enquanto ela está virada. */
@Composable
internal fun TarotCardBack(modifier: Modifier = Modifier) {
    val back = remember { TarotCardImages.assetUri(TarotCardImages.CARD_BACK_PATH) }
    var backFailed by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .aspectRatio(CARD_ASPECT)
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), CardShape),
        contentAlignment = Alignment.Center
    ) {
        if (!backFailed) {
            AsyncImage(
                model = back,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                onError = { backFailed = true },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(
                    stringResource(R.string.tarot_card_back),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun CardTypography(drawn: DrawnCard, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            positionLabel(drawn.position).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Números romanos só fazem sentido nos Arcanos Maiores; os Menores já trazem o nome completo.
            if (drawn.card.suit == Suit.MAJOR) {
                Text(
                    romanNumeral(drawn.card.number),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                drawn.card.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            if (drawn.reversed) {
                Text(
                    stringResource(R.string.tarot_reversed),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
internal fun positionLabel(position: SpreadPosition): String = when (position) {
    SpreadPosition.SINGLE -> stringResource(R.string.tarot_position_single)
    SpreadPosition.PAST -> stringResource(R.string.tarot_position_past)
    SpreadPosition.PRESENT -> stringResource(R.string.tarot_position_present)
    SpreadPosition.FUTURE -> stringResource(R.string.tarot_position_future)
}

/** Descrição acessível completa de uma carta, lida pelo TalkBack. */
@Composable
internal fun cardDescription(drawn: DrawnCard): String {
    val orientation = if (drawn.reversed) ", ${stringResource(R.string.tarot_reversed).lowercase()}" else ""
    return "${positionLabel(drawn.position)}: ${drawn.card.name}$orientation. " +
        "${stringResource(R.string.tarot_keywords)}: ${drawn.meaning.keywords.joinToString(", ")}"
}

/** Anúncio curto durante a revelação: posição, nome e orientação (sem palavras-chave, para não atrasar a sequência). */
@Composable
internal fun revealAnnouncement(drawn: DrawnCard): String {
    val orientation = if (drawn.reversed) ", ${stringResource(R.string.tarot_reversed).lowercase()}" else ""
    return stringResource(R.string.tarot_revealing_card, positionLabel(drawn.position), drawn.card.name, orientation)
}

internal fun romanNumeral(number: Int): String {
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
