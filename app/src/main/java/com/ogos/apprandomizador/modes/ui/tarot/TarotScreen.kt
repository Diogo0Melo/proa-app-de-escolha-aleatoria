package com.ogos.apprandomizador.modes.ui.tarot

import android.animation.ValueAnimator
import android.os.SystemClock
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ogos.apprandomizador.R
import com.ogos.apprandomizador.modes.domain.Spread
import com.ogos.apprandomizador.ui.sound.TickPlayer
import kotlinx.coroutines.delay

@Composable
fun TarotRoute(
    viewModel: TarotViewModel,
    soundEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboard = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val tickPlayer = remember(context.applicationContext) {
        TickPlayer(context.applicationContext, R.raw.roleta_audio)
    }
    DisposableEffect(tickPlayer) {
        onDispose { tickPlayer.release() }
    }

    LaunchedEffect(state.requestId, state.phase) {
        if (state.phase == TarotPhase.Revealing) {
            if (!ValueAnimator.areAnimatorsEnabled()) {
                viewModel.onRevealFinished(state.requestId)
            } else {
                val remaining = state.revealStartElapsedRealtime + state.durationMillis - SystemClock.elapsedRealtime()
                if (remaining > 0) delay(remaining)
                viewModel.onRevealFinished(state.requestId)
            }
        }
    }
    TarotScreen(
        state = state,
        onFlip = { index -> if (soundEnabled) tickPlayer.play(volume = 0.42f, pitch = 0.92f + (index * 0.08f)) },
        onQuestionChange = viewModel::onQuestionChange,
        onSpreadChange = viewModel::onSpreadChange,
        onAllowReversedChange = viewModel::onAllowReversedChange,
        onDraw = { keyboard?.hide(); viewModel.onDraw() },
        onReset = viewModel::onReset,
        onDeepenAi = viewModel::onDeepenWithAi,
        onCancelAi = viewModel::onCancelAi,
        modifier = modifier
    )
}

@Composable
fun TarotScreen(
    state: TarotUiState,
    onFlip: (Int) -> Unit,
    onQuestionChange: (String) -> Unit,
    onSpreadChange: (Spread) -> Unit,
    onAllowReversedChange: (Boolean) -> Unit,
    onDraw: () -> Unit,
    onReset: () -> Unit,
    onDeepenAi: () -> Unit = {},
    onCancelAi: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val fontScale = LocalDensity.current.fontScale
    val controlsEnabled = state.phase == TarotPhase.Idle
    val animationsEnabled = ValueAnimator.areAnimatorsEnabled()
    val remainingRevealMillis = if (state.phase == TarotPhase.Revealing) {
        (state.revealStartElapsedRealtime + state.durationMillis - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
    } else state.durationMillis
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(R.string.tarot_intro_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.tarot_intro_description), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = state.question,
                    onValueChange = onQuestionChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.optional_question)) },
                    supportingText = { Text(stringResource(R.string.question_not_saved)) },
                    enabled = controlsEnabled,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { keyboard?.hide() })
                )
                Text(stringResource(R.string.tarot_spread), style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth().selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.spread == Spread.SINGLE,
                        onClick = { onSpreadChange(Spread.SINGLE) },
                        label = { Text(stringResource(R.string.tarot_one_card)) },
                        enabled = controlsEnabled
                    )
                    FilterChip(
                        selected = state.spread == Spread.PAST_PRESENT_FUTURE,
                        onClick = { onSpreadChange(Spread.PAST_PRESENT_FUTURE) },
                        label = { Text(stringResource(R.string.tarot_three_cards)) },
                        enabled = controlsEnabled
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Switch(checked = state.allowReversed, onCheckedChange = onAllowReversedChange, enabled = controlsEnabled)
                    Text(stringResource(R.string.tarot_allow_reversed), style = MaterialTheme.typography.bodyMedium)
                }
                Button(
                    onClick = onDraw,
                    enabled = controlsEnabled,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(stringResource(if (state.phase == TarotPhase.Idle) R.string.tarot_draw else R.string.revealing))
                }
            }
        }

        if (state.phase == TarotPhase.Revealing || state.phase == TarotPhase.Interpreting) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text(
                stringResource(if (state.phase == TarotPhase.Revealing) R.string.tarot_revealing_cards else R.string.tarot_interpreting),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (state.drawn.isNotEmpty()) {
            Text(stringResource(R.string.tarot_your_cards), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.semantics { heading() })
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val vertical = maxWidth < 420.dp || fontScale > 1.3f
                val segment = (remainingRevealMillis / state.drawn.size.coerceAtLeast(1)).toInt().coerceAtLeast(1)
                val animationWindow = remainingRevealMillis.toInt().coerceAtLeast(1)
                val animate = animationsEnabled && state.phase == TarotPhase.Revealing
                if (vertical) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.drawn.forEachIndexed { index, card ->
                            TarotCardView(
                                drawn = card,
                                requestId = state.requestId,
                                revealed = state.phase != TarotPhase.Idle,
                                animate = animate,
                                startDelayMillis = index * (segment / 2),
                                animationDurationMillis = animationWindow - index * (segment / 2),
                                onFlip = { onFlip(index) }
                            )
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.drawn.forEachIndexed { index, card ->
                            TarotCardView(
                                drawn = card,
                                requestId = state.requestId,
                                revealed = state.phase != TarotPhase.Idle,
                                animate = animate,
                                startDelayMillis = index * (segment / 2),
                                animationDurationMillis = animationWindow - index * (segment / 2),
                                onFlip = { onFlip(index) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        if (state.phase == TarotPhase.Revealed && state.reading != null) {
            Card(
                modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.tarot_reading), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(state.reading.summary, style = MaterialTheme.typography.bodyLarge)
                    state.reading.sections.forEach { section ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(section.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(section.text, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Text(stringResource(R.string.tarot_disclaimer), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (state.aiEnabled) {
                OutlinedButton(onClick = onDeepenAi, modifier = Modifier.fillMaxWidth(), enabled = !state.aiLoading) {
                    Text(stringResource(R.string.tarot_deepen_with_ai))
                }
                if (state.aiLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    OutlinedButton(onClick = onCancelAi, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.cancel)) }
                }
                state.aiError?.let { error ->
                    Text(error, color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = onDeepenAi, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.try_again)) }
                }
                state.aiReading?.let { ai ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(R.string.tarot_ai_reading, state.aiModel.orEmpty()), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(ai.summary, style = MaterialTheme.typography.bodyLarge)
                            ai.sections.forEach { section ->
                                Text(section.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(section.text, style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(stringResource(R.string.tarot_disclaimer), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(stringResource(R.string.tarot_new_reading))
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}


@Preview(showBackground = true, name = "Tarô · configuração")
@Composable
private fun TarotScreenPreview() {
    com.ogos.apprandomizador.ui.theme.AppRandomizadorTheme {
        TarotScreen(
            state = TarotUiState(),
            onFlip = {},
            onQuestionChange = {},
            onSpreadChange = {},
            onAllowReversedChange = {},
            onDraw = {},
            onReset = {}
        )
    }
}
