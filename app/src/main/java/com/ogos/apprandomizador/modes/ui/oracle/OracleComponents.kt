package com.ogos.apprandomizador.modes.ui.oracle

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ogos.apprandomizador.R
import com.ogos.apprandomizador.model.RaffleItem
import com.ogos.apprandomizador.modes.domain.OracleSource
import com.ogos.apprandomizador.ui.view.component.RouletteMath

/** Botão grande de ditado por voz, ação principal da tela. */
@Composable
fun BigMicButton(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val label = stringResource(R.string.voice_input)
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(120.dp).semantics { contentDescription = label }
    ) {
        Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(48.dp))
    }
}

/** Chip da fonte. Com roleta vinculada, mostra também a quantidade de itens. */
@Composable
fun SourceChip(source: OracleSource, enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val label = stringResource(R.string.oracle_source_label)
    val text = if (source.topic == null) {
        "$label: ${stringResource(R.string.oracle_source_classic)}"
    } else {
        "$label: ${source.topic} · ${stringResource(R.string.oracle_source_items, source.items.size)}"
    }
    AssistChip(
        onClick = onClick,
        enabled = enabled,
        label = { Text(text) },
        modifier = modifier
    )
}

/**
 * Resposta em destaque. Enquanto a animação roda, mostra um marcador pulsante; depois da
 * revelação, usa o gradiente da cor do item e anuncia o resultado ao TalkBack.
 */
@Composable
fun BigAnswerCard(answer: RaffleItem?, colorIndex: Int, modifier: Modifier = Modifier) {
    if (answer == null) {
        AnswerPlaceholder(modifier = modifier)
        return
    }
    val shape = MaterialTheme.shapes.extraLarge
    val (start, end) = RouletteMath.gradientFor(answer, colorIndex)
    val textColor = if (RouletteMath.isLightBase(answer.color)) Color(0xFF1B1B1B) else Color.White
    val announcement = stringResource(R.string.oracle_answer_announcement, answer.name)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color(start), Color(end))))
            .padding(vertical = 40.dp, horizontal = 20.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = announcement
                liveRegion = LiveRegionMode.Polite
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            answer.name,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AnswerPlaceholder(modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "answer-placeholder")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 900), repeatMode = RepeatMode.Reverse),
        label = "answer-placeholder-alpha"
    )
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "✦ ✧ ✦",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = alpha)
            )
        }
    }
}
