package com.ogos.apprandomizador.modes.ui.oracle

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ogos.apprandomizador.R
import com.ogos.apprandomizador.core.ui.ai.AiAction
import com.ogos.apprandomizador.core.ui.voice.rememberSpeechInput
import com.ogos.apprandomizador.core.ui.voice.rememberSpeechPlayer
import com.ogos.apprandomizador.core.ui.voice.toSpeechText
import com.ogos.apprandomizador.modes.domain.OracleReading
import kotlinx.coroutines.delay

/** Ações da tela agrupadas para manter a UI independente da implementação do ViewModel. */
data class YesNoOracleActions(
    val onQuestionChange: (String) -> Unit = {},
    val onAsk: () -> Unit = {},
    val onReset: () -> Unit = {},
    val onVoice: () -> Unit = {},
    val voiceAvailable: Boolean = false,
    val onDeepenAi: () -> Unit = {},
    val onCancelAi: () -> Unit = {},
    val onSpeak: (OracleReading) -> Unit = {},
    val onStopSpeaking: () -> Unit = {},
    val onLinkRoulette: (Long) -> Unit = {},
    val onUseClassic: () -> Unit = {}
)

@Composable
fun YesNoOracleRoute(viewModel: OracleViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val voice = rememberSpeechInput(prompt = stringResource(R.string.voice_prompt)) { spoken ->
        viewModel.onSpokenQuestion(spoken)
    }
    val speechPlayer = rememberSpeechPlayer()
    val speech by speechPlayer.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.requestId, state.phase) {
        if (state.phase == OraclePhase.Revealing) {
            delay(state.durationMillis)
            viewModel.onRevealFinished(state.requestId)
        }
    }
    LaunchedEffect(state.phase) {
        if (state.phase != OraclePhase.Revealed) speechPlayer.stop()
    }

    YesNoOracleScreen(
        state = state,
        actions = YesNoOracleActions(
            onQuestionChange = viewModel::onQuestionChange,
            onAsk = viewModel::onAsk,
            onReset = viewModel::onReset,
            onVoice = voice.start,
            voiceAvailable = voice.available,
            onDeepenAi = viewModel::onDeepenWithAi,
            onCancelAi = viewModel::onCancelAi,
            onSpeak = { reading -> speechPlayer.speak(reading.toSpeechText()) },
            onStopSpeaking = speechPlayer::stop,
            onLinkRoulette = viewModel::onLinkRoulette,
            onUseClassic = viewModel::onUseClassic
        ),
        speechAvailable = speech.available,
        speaking = speech.speaking,
        modifier = modifier
    )
}

/** Tela de consulta Sim/Não. O ViewModel continua responsável pelas regras e sorteio. */
@Composable
fun YesNoOracleScreen(
    state: OracleUiState,
    actions: YesNoOracleActions,
    speechAvailable: Boolean = false,
    speaking: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showSources by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val idle = state.phase == OraclePhase.Idle
    val revealed = state.phase == OraclePhase.Revealed
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(state.aiReading) {
        if (state.aiReading != null) {
            withFrameNanos { }
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(scrollState).padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.oracle_prompt),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() }
            )
            Text(
                text = stringResource(R.string.oracle_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        if (idle) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OracleVoiceButton(enabled = actions.voiceAvailable, onClick = actions.onVoice)
                Text(
                    text = stringResource(if (actions.voiceAvailable) R.string.oracle_hold_to_speak else R.string.oracle_voice_unavailable),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OutlinedTextField(
            value = state.question,
            onValueChange = actions.onQuestionChange,
            enabled = idle,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.optional_question)) },
            supportingText = { Text(stringResource(R.string.question_not_saved)) },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                keyboard?.hide()
                if (state.canAsk) actions.onAsk()
            })
        )

        if (idle) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OracleSuggestionChip("Devo seguir em frente com essa ideia?") {
                    actions.onQuestionChange("Devo seguir em frente com essa ideia?")
                }
                OracleSuggestionChip("Este é o momento certo para agir?") {
                    actions.onQuestionChange("Este é o momento certo para agir?")
                }
                OracleSuggestionChip("Posso confiar nesta decisão?") {
                    actions.onQuestionChange("Posso confiar nesta decisão?")
                }
            }
        }

        Surface(
            onClick = { if (idle) showSources = true },
            enabled = idle,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (state.source.topic == null) {
                        "${stringResource(R.string.oracle_source_label)}: ${stringResource(R.string.oracle_source_classic)}"
                    } else {
                        "${stringResource(R.string.oracle_source_label)}: ${state.source.topic} · ${state.source.items.size}"
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        if (state.sourceNotice == OracleSourceNotice.RouletteRemoved) {
            Text(stringResource(R.string.oracle_source_removed), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        if (state.source.items.isEmpty()) {
            Text(stringResource(R.string.oracle_source_empty), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        if (!revealed) {
            Button(
                onClick = actions.onAsk,
                enabled = state.canAsk,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                Spacer(Modifier.size(10.dp))
                Text(stringResource(if (idle) R.string.oracle_ask else R.string.oracle_revealing))
            }
        }

        AnimatedContent(
            targetState = state.revealedAnswer,
            transitionSpec = {
                (fadeIn(tween(280)) + scaleIn(initialScale = 0.88f, animationSpec = tween(360)))
                    .togetherWith(fadeOut(tween(150)))
            },
            label = "yes-no-oracle-answer"
        ) { answer ->
            val colorIndex = answer?.let { state.source.items.indexOf(it) }?.coerceAtLeast(0) ?: 0
            BigAnswerCard(answer = answer, colorIndex = colorIndex)
        }

        if (state.phase == OraclePhase.Revealing || state.phase == OraclePhase.Recording) {
            Text(
                text = stringResource(R.string.oracle_synchronizing),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (revealed) {
            state.visibleReflection?.let { ReflectionQuote(it) }
            OracleAiSection(state = state, speechAvailable = speechAvailable, speaking = speaking, actions = actions)
            if (state.aiAction != AiAction.Loading) {
                OutlinedButton(
                    onClick = actions.onReset,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = CircleShape
                ) {
                    Text(stringResource(R.string.oracle_ask_again))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }

    if (showSources) {
        OracleSourcePicker(
            sources = state.availableSources,
            selectedRouletteId = state.linkedRouletteId,
            onSelectClassic = actions.onUseClassic,
            onSelectRoulette = actions.onLinkRoulette,
            onDismiss = { showSources = false }
        )
    }
}

@Composable
private fun OracleVoiceButton(enabled: Boolean, onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "oracle-voice-halo")
    val haloAlpha by transition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.42f,
        animationSpec = infiniteRepeatable(tween(1800), repeatMode = RepeatMode.Reverse),
        label = "oracle-voice-halo-alpha"
    )
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(116.dp)) {
        Box(
            modifier = Modifier.size(108.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = haloAlpha))
                .alpha(if (enabled) 1f else 0.45f)
        )
        FilledIconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(88.dp),
            shape = CircleShape
        ) {
            Icon(Icons.Filled.Mic, contentDescription = stringResource(R.string.voice_input), modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
private fun OracleSuggestionChip(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        modifier = modifier,
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    )
}

@Composable
private fun ReflectionQuote(text: String) {
    Text(
        "“$text”",
        style = MaterialTheme.typography.titleLarge,
        fontStyle = FontStyle.Italic,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
    )
}

@Composable
private fun OracleAiSection(
    state: OracleUiState,
    speechAvailable: Boolean,
    speaking: Boolean,
    actions: YesNoOracleActions
) {
    val action = state.aiAction
    val deepenLabel = stringResource(R.string.oracle_deepen_with_ai)
    if (action == AiAction.Deepen) {
        OutlinedButton(onClick = actions.onDeepenAi, modifier = Modifier.fillMaxWidth()) {
            Text(deepenLabel)
        }
    }
    if (action == AiAction.Loading) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        if (state.aiAttempt > 1) {
            Text(
                stringResource(R.string.oracle_ai_retrying),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedButton(onClick = actions.onCancelAi, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.cancel))
        }
    }
    if (action == AiAction.Retry) {
        val failure = state.aiError
        if (failure != null) {
            Text(failure, color = MaterialTheme.colorScheme.error)
        } else {
            Text(
                stringResource(R.string.oracle_ai_cancelled),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedButton(onClick = actions.onDeepenAi, modifier = Modifier.fillMaxWidth()) {
            Text(if (failure != null) stringResource(R.string.try_again) else deepenLabel)
        }
    }
    state.aiReading?.let { reading ->
        OracleAiCard(
            reading = reading,
            model = state.aiModel,
            speechAvailable = speechAvailable,
            speaking = speaking,
            actions = actions
        )
    }
}

/**
 * Hierarquia de cima para baixo: título, essência, leitura curta e, por fim, a afirmação.
 * A afirmação é o clímax do bloco e fica isolada por um divisor.
 */
@Composable
private fun OracleAiCard(
    reading: OracleReading,
    model: String?,
    speechAvailable: Boolean,
    speaking: Boolean,
    actions: YesNoOracleActions
) {
    val onTertiary = MaterialTheme.colorScheme.onTertiaryContainer
    Card(
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.oracle_ai_title, model.orEmpty()),
                    style = MaterialTheme.typography.labelMedium,
                    color = onTertiary.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f)
                )
                if (speechAvailable) {
                    if (speaking) {
                        IconButton(onClick = actions.onStopSpeaking) {
                            Icon(Icons.Filled.Stop, contentDescription = stringResource(R.string.oracle_stop_reading))
                        }
                    } else {
                        IconButton(onClick = { actions.onSpeak(reading) }) {
                            Icon(Icons.Filled.VolumeUp, contentDescription = stringResource(R.string.oracle_listen_reading))
                        }
                    }
                }
            }

            reading.title?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = onTertiary
                )
            }

            reading.essence?.let {
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.tertiary) {
                    Text(
                        "✦ $it",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }

            Text(
                reading.reading,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 26.sp,
                color = onTertiary
            )

            reading.affirmation?.let {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    "“$it”",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    color = onTertiary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text(
                stringResource(R.string.oracle_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = onTertiary.copy(alpha = 0.6f)
            )
        }
    }
}
