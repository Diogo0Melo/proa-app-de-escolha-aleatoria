package com.ogos.apprandomizador.features.oracle.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.ogos.apprandomizador.core.model.RaffleItem
import com.ogos.apprandomizador.features.randomize.ui.component.RouletteMath

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
