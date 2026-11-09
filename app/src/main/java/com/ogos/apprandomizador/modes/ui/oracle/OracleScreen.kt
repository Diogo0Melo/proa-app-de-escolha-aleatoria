package com.ogos.apprandomizador.modes.ui.oracle

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ogos.apprandomizador.R
import com.ogos.apprandomizador.modes.domain.OracleReading
import com.ogos.apprandomizador.modes.ui.ai.AiAction
import com.ogos.apprandomizador.ui.sound.rememberSpeechPlayer
import com.ogos.apprandomizador.ui.sound.toSpeechText
import com.ogos.apprandomizador.ui.speech.rememberSpeechInput
import kotlinx.coroutines.delay

/** Ações da tela. Agrupadas para que a tela seja testável sem ViewModel. */
data class OracleActions(
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
fun OracleRoute(viewModel: OracleViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val voice = rememberSpeechInput(prompt = stringResource(R.string.voice_prompt)) { spoken ->
        viewModel.onSpokenQuestion(spoken)
    }
    val speechPlayer = rememberSpeechPlayer()
    val speech by speechPlayer.state.collectAsStateWithLifecycle()

    // O tempo de revelação vem do sorteio; ao terminar, o ViewModel grava e revela.
    LaunchedEffect(state.requestId, state.phase) {
        if (state.phase == OraclePhase.Revealing) {
            delay(state.durationMillis)
            viewModel.onRevealFinished(state.requestId)
        }
    }
    LaunchedEffect(state.phase) {
        if (state.phase != OraclePhase.Revealed) speechPlayer.stop()
    }

    OracleScreen(
        state = state,
        actions = OracleActions(
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

@Composable
fun OracleScreen(
    state: OracleUiState,
    actions: OracleActions,
    speechAvailable: Boolean = false,
    speaking: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showSources by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val idle = state.phase == OraclePhase.Idle
    val revealed = state.phase == OraclePhase.Revealed
    val keyboard = LocalSoftwareKeyboardController.current

    // Quando a leitura da IA chega, desce até ela: sem isso ela fica abaixo da dobra.
    LaunchedEffect(state.aiReading) {
        if (state.aiReading != null) scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(scrollState).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            stringResource(R.string.oracle_prompt),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { heading() }
        )
        BigMicButton(enabled = actions.voiceAvailable && idle, onClick = actions.onVoice)
        Text(
            stringResource(if (actions.voiceAvailable) R.string.oracle_hold_to_speak else R.string.oracle_voice_unavailable),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = state.question,
            onValueChange = actions.onQuestionChange,
            enabled = idle,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.optional_question)) },
            supportingText = { Text(stringResource(R.string.question_not_saved)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                keyboard?.hide()
                if (state.canAsk) actions.onAsk()
            })
        )
        SourceChip(source = state.source, enabled = idle, onClick = { showSources = true })
        if (state.sourceNotice == OracleSourceNotice.RouletteRemoved) {
            Text(
                stringResource(R.string.oracle_source_removed),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        if (state.source.items.isEmpty()) {
            Text(
                stringResource(R.string.oracle_source_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        if (!revealed) {
            Button(
                onClick = actions.onAsk,
                enabled = state.canAsk,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(stringResource(if (idle) R.string.oracle_ask else R.string.oracle_revealing))
            }
        }

        // Gate de revelação: a resposta só entra na tela depois da animação (revealedAnswer).
        AnimatedContent(
            targetState = state.revealedAnswer,
            transitionSpec = {
                (fadeIn(tween(280)) + scaleIn(initialScale = 0.85f, animationSpec = tween(360)))
                    .togetherWith(fadeOut(tween(150)))
            },
            label = "oracle-answer"
        ) { answer ->
            val colorIndex = answer?.let { state.source.items.indexOf(it) }?.coerceAtLeast(0) ?: 0
            BigAnswerCard(answer = answer, colorIndex = colorIndex)
        }

        if (revealed) {
            state.reflection?.let { ReflectionQuote(it) }
            OracleAiSection(state = state, speechAvailable = speechAvailable, speaking = speaking, actions = actions)
            if (state.aiAction != AiAction.Loading) {
                OutlinedButton(
                    onClick = actions.onReset,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(stringResource(R.string.oracle_ask_again))
                }
            }
        }
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
    actions: OracleActions
) {
    val action = state.aiAction
    // Antes da curta, o botão aprofunda; depois dela, pede a leitura estendida.
    val deepenLabel = stringResource(
        if (state.aiReading == null) R.string.oracle_deepen_with_ai else R.string.oracle_deepen_more
    )
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
    actions: OracleActions
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
