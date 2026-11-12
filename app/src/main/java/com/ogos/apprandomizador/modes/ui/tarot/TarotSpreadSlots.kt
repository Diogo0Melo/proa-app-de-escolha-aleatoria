package com.ogos.apprandomizador.modes.ui.tarot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ogos.apprandomizador.modes.domain.DrawnCard

/**
 * Faixa fixa com um espaço por carta da tiragem. Cada espaço começa vazio e recebe a carta
 * quando ela é fixada; tocar numa carta fixada abre o significado completo.
 */
@Composable
internal fun TarotSpreadSlots(
    cards: List<DrawnCard>,
    landedCount: Int,
    onOpen: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
    ) {
        cards.forEachIndexed { index, drawn ->
            val slotModifier = if (cards.size == 1) Modifier.width(150.dp) else Modifier.weight(1f)
            val landed = index < landedCount
            Column(
                modifier = slotModifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    positionLabel(drawn.position).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (!landed) SlotPlaceholder(Modifier.fillMaxWidth())
                    this@Column.AnimatedVisibility(
                        visible = landed,
                        enter = fadeIn(tween(320)) + scaleIn(initialScale = 0.85f, animationSpec = tween(320)),
                        exit = fadeOut(tween(160))
                    ) {
                        val description = cardDescription(drawn)
                        TarotCardFace(
                            drawn = drawn,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics(mergeDescendants = true) { contentDescription = description }
                                .clickable(role = Role.Button, onClick = { onOpen(index) })
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SlotPlaceholder(modifier: Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(CARD_ASPECT)
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CardShape)
    )
}
