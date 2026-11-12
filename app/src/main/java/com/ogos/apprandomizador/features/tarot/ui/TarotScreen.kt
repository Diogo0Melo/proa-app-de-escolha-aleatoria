package com.ogos.apprandomizador.features.tarot.ui

import android.animation.ValueAnimator
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ogos.apprandomizador.R
import com.ogos.apprandomizador.core.ui.ai.AiAction
import com.ogos.apprandomizador.core.ui.theme.AppRandomizadorTheme
import com.ogos.apprandomizador.core.ui.voice.TickPlayer
import com.ogos.apprandomizador.core.ui.voice.rememberSpeechInput
import com.ogos.apprandomizador.core.ui.voice.rememberSpeechPlayer
import com.ogos.apprandomizador.features.tarot.domain.Reading
import com.ogos.apprandomizador.features.tarot.domain.Spread

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
    val speechPlayer = rememberSpeechPlayer()
    val speech by speechPlayer.state.collectAsStateWithLifecycle()
    val voiceInput = rememberSpeechInput(prompt = stringResource(R.string.voice_prompt)) { spoken ->
        viewModel.onQuestionChange(listOf(state.question, spoken).filter(String::isNotBlank).joinToString(" "))
    }
    LaunchedEffect(state.phase) {
        if (state.phase != TarotPhase.Revealed) speechPlayer.stop()
    }
    // Com animações desligadas no sistema, a sequência de revelação é encerrada de uma vez.
    LaunchedEffect(state.requestId, state.phase) {
        if (state.phase == TarotPhase.Revealing && !ValueAnimator.areAnimatorsEnabled()) {
            viewModel.onRevealFinished(state.requestId)
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
        onCardLanded = { index -> viewModel.onCardLanded(state.requestId, index) },
        voiceInputAvailable = voiceInput.available,
        onVoiceInput = voiceInput.start,
        speechAvailable = speech.available,
        speaking = speech.speaking,
        onSpeakReading = { reading -> speechPlayer.speak(reading.toSpeechText()) },
        onStopSpeaking = speechPlayer::stop,
        modifier = modifier
    )
}

/**
 * Tela do Tarô em três fases. Na configuração, tudo rola. Durante a tiragem, as cartas ficam
 * fixadas no topo e a rolagem só é liberada depois que a última carta for fixada.
 */
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
    onCardLanded: (Int) -> Unit = {},
    voiceInputAvailable: Boolean = false,
    onVoiceInput: () -> Unit = {},
    speechAvailable: Boolean = false,
    speaking: Boolean = false,
    onSpeakReading: (Reading) -> Unit = {},
    onStopSpeaking: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val setup = state.phase == TarotPhase.Idle
    // Só libera a rolagem depois que todas as cartas da tiragem foram fixadas.
    val scrollUnlocked = state.drawn.isNotEmpty() && state.landedCount == state.drawn.size
    var detailIndex by rememberSaveable { mutableIntStateOf(-1) }

    Box(modifier = modifier.fillMaxSize()) {
        if (setup) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                SetupContent(
                    state = state,
                    onQuestionChange = onQuestionChange,
                    onSpreadChange = onSpreadChange,
                    onAllowReversedChange = onAllowReversedChange,
                    onDraw = onDraw,
                    voiceInputAvailable = voiceInputAvailable,
                    onVoiceInput = onVoiceInput
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                if (state.drawn.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            stringResource(R.string.tarot_your_cards),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.semantics { heading() }
                        )
                        TarotSpreadSlots(
                            cards = state.drawn,
                            landedCount = state.landedCount,
                            onOpen = { detailIndex = it }
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState(), enabled = scrollUnlocked)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (state.phase) {
                        TarotPhase.Idle -> Unit
                        TarotPhase.Revealing -> RevealingStatus()
                        TarotPhase.Interpreting -> InterpretingStatus()
                        TarotPhase.Revealed -> ReadingContent(
                            state = state,
                            speechAvailable = speechAvailable,
                            speaking = speaking,
                            onSpeakReading = onSpeakReading,
                            onStopSpeaking = onStopSpeaking,
                            onDeepenAi = onDeepenAi,
                            onCancelAi = onCancelAi,
                            onReset = onReset
                        )
                    }
                }
            }
        }

        val pending = state.drawn.getOrNull(state.landedCount)
        if (state.phase == TarotPhase.Revealing && pending != null) {
            TarotRevealOverlay(
                card = pending,
                index = state.landedCount,
                onFlipped = onFlip,
                onLanded = onCardLanded
            )
        }

        val detail = state.drawn.getOrNull(detailIndex)
        if (detail != null && detailIndex < state.landedCount) {
            TarotCardDetailSheet(drawn = detail, onDismiss = { detailIndex = -1 })
        }
    }
}

@Composable
private fun SetupContent(
    state: TarotUiState,
    onQuestionChange: (String) -> Unit,
    onSpreadChange: (Spread) -> Unit,
    onAllowReversedChange: (Boolean) -> Unit,
    onDraw: () -> Unit,
    voiceInputAvailable: Boolean,
    onVoiceInput: () -> Unit
) {
    Hero()
    QuestionField(
        question = state.question,
        onQuestionChange = onQuestionChange,
        voiceInputAvailable = voiceInputAvailable,
        onVoiceInput = onVoiceInput
    )
    SuggestionRow(onQuestionChange = onQuestionChange)
    SpreadPicker(selected = state.spread, onSpreadChange = onSpreadChange)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.4f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(R.string.tarot_allow_reversed), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(stringResource(R.string.tarot_allow_reversed_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = state.allowReversed, onCheckedChange = onAllowReversedChange)
    }
    Button(onClick = onDraw, modifier = Modifier.fillMaxWidth().height(56.dp)) {
        Text(stringResource(R.string.tarot_draw))
    }
}

@Composable
private fun Hero() {
    val transition = rememberInfiniteTransition(label = "mystic-halo")
    val pulse by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "mystic-halo-alpha"
    )
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .graphicsLayer {
                        alpha = pulse
                        scaleX = 0.98f + 0.04f * pulse
                        scaleY = 0.98f + 0.04f * pulse
                    }
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            )
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Text(
            stringResource(R.string.tarot_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            stringResource(R.string.tarot_intro_title),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun QuestionField(
    question: String,
    onQuestionChange: (String) -> Unit,
    voiceInputAvailable: Boolean,
    onVoiceInput: () -> Unit
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val voiceIcon: @Composable () -> Unit = {
        IconButton(onClick = onVoiceInput) {
            Icon(Icons.Filled.Mic, contentDescription = stringResource(R.string.voice_input))
        }
    }
    OutlinedTextField(
        value = question,
        onValueChange = onQuestionChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.tarot_intent_label)) },
        placeholder = { Text(stringResource(R.string.tarot_intent_placeholder)) },
        supportingText = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.question_not_saved), style = MaterialTheme.typography.bodySmall)
                Text(
                    "${question.length}/140",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (question.length >= 130) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                )
            }
        },
        trailingIcon = if (voiceInputAvailable) voiceIcon else null,
        minLines = 2,
        maxLines = 3,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { keyboard?.hide() })
    )
}

@Composable
private fun SuggestionRow(onQuestionChange: (String) -> Unit) {
    val suggestions = listOf(
        stringResource(R.string.tarot_suggestion_decision) to stringResource(R.string.tarot_suggestion_decision_question),
        stringResource(R.string.tarot_suggestion_relationship) to stringResource(R.string.tarot_suggestion_relationship_question),
        stringResource(R.string.tarot_suggestion_next_step) to stringResource(R.string.tarot_suggestion_next_step_question)
    )
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        suggestions.forEach { (label, question) ->
            SuggestionChip(onClick = { onQuestionChange(question) }, label = { Text(label) })
        }
    }
}

@Composable
private fun SpreadPicker(selected: Spread, onSpreadChange: (Spread) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(stringResource(R.string.tarot_spread), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth().selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SpreadOption(
                label = stringResource(R.string.tarot_one_card),
                count = 1,
                selected = selected == Spread.SINGLE,
                onClick = { onSpreadChange(Spread.SINGLE) },
                modifier = Modifier.weight(1f)
            )
            SpreadOption(
                label = stringResource(R.string.tarot_three_cards),
                count = 3,
                selected = selected == Spread.PAST_PRESENT_FUTURE,
                onClick = { onSpreadChange(Spread.PAST_PRESENT_FUTURE) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SpreadOption(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .height(120.dp)
            .clip(shape)
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
            .border(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, shape)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(count) {
                Box(
                    modifier = Modifier
                        .size(width = if (count == 1) 22.dp else 16.dp, height = if (count == 1) 32.dp else 26.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(2.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(3.dp))
                )
            }
        }
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RevealingStatus() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        Text(
            stringResource(R.string.tarot_revealing_cards),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            stringResource(R.string.tarot_revealing_subtext),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun InterpretingStatus() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        Text(
            stringResource(R.string.tarot_interpreting),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReadingContent(
    state: TarotUiState,
    speechAvailable: Boolean,
    speaking: Boolean,
    onSpeakReading: (Reading) -> Unit,
    onStopSpeaking: () -> Unit,
    onDeepenAi: () -> Unit,
    onCancelAi: () -> Unit,
    onReset: () -> Unit
) {
    val reading = state.reading
    if (reading != null) {
        Card(
            modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SpeechHeader(
                    title = stringResource(R.string.tarot_reading),
                    showSpeech = speechAvailable && !(state.aiEnabled && state.aiReading != null),
                    speaking = speaking,
                    onSpeak = { onSpeakReading(reading) },
                    onStop = onStopSpeaking
                )
                Text(reading.summary, style = MaterialTheme.typography.bodyLarge)
                reading.sections.forEach { section ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(section.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(section.text, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Text(
                    stringResource(R.string.tarot_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (state.aiEnabled) {
        AiSection(
            state = state,
            speechAvailable = speechAvailable,
            speaking = speaking,
            onSpeakReading = onSpeakReading,
            onStopSpeaking = onStopSpeaking,
            onDeepenAi = onDeepenAi,
            onCancelAi = onCancelAi
        )
    }

    if (state.aiAction != AiAction.Loading) {
        OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text(stringResource(R.string.tarot_new_reading))
        }
    }
}

@Composable
private fun AiSection(
    state: TarotUiState,
    speechAvailable: Boolean,
    speaking: Boolean,
    onSpeakReading: (Reading) -> Unit,
    onStopSpeaking: () -> Unit,
    onDeepenAi: () -> Unit,
    onCancelAi: () -> Unit
) {
    if (state.aiAction == AiAction.Deepen) {
        OutlinedButton(onClick = onDeepenAi, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.tarot_deepen_with_ai))
        }
    }
    if (state.aiAction == AiAction.Loading) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        if (state.aiAttempt > 1) {
            Text(
                stringResource(R.string.tarot_ai_retrying),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedButton(onClick = onCancelAi, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.cancel)) }
    }
    if (state.aiAction == AiAction.Retry) {
        val failure = state.aiError
        if (failure != null) {
            Text(failure, color = MaterialTheme.colorScheme.error)
        } else {
            Text(
                stringResource(R.string.tarot_ai_cancelled),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedButton(onClick = onDeepenAi, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(if (failure != null) R.string.try_again else R.string.tarot_deepen_with_ai))
        }
    }
    state.aiReading?.let { ai ->
        Card(
            modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SpeechHeader(
                    title = stringResource(R.string.tarot_ai_reading, state.aiModel.orEmpty()),
                    showSpeech = speechAvailable,
                    speaking = speaking,
                    onSpeak = { onSpeakReading(ai) },
                    onStop = onStopSpeaking
                )
                ai.title?.let { Text(it, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                ai.opening?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                Text(ai.summary, style = MaterialTheme.typography.bodyLarge)
                ai.sections.forEach { section ->
                    Text(section.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(section.text, style = MaterialTheme.typography.bodyMedium)
                }
                ai.guidance?.let { LabeledBlock(stringResource(R.string.tarot_guidance), it) }
                ai.synthesis?.let { LabeledBlock(stringResource(R.string.tarot_synthesis), it) }
                Text(
                    stringResource(R.string.tarot_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SpeechHeader(
    title: String,
    showSpeech: Boolean,
    speaking: Boolean,
    onSpeak: () -> Unit,
    onStop: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (showSpeech) {
            if (speaking) {
                IconButton(onClick = onStop) {
                    Icon(Icons.Filled.Stop, contentDescription = stringResource(R.string.tarot_stop_reading))
                }
            } else {
                IconButton(onClick = onSpeak) {
                    Icon(Icons.Filled.VolumeUp, contentDescription = stringResource(R.string.tarot_listen_reading))
                }
            }
        }
    }
}

@Composable
private fun LabeledBlock(label: String, text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Tarô · configuração")
@Composable
private fun TarotScreenPreview() {
    com.ogos.apprandomizador.core.ui.theme.AppRandomizadorTheme {
        TarotScreen(state = TarotUiState(), onFlip = {}, onQuestionChange = {}, onSpreadChange = {}, onAllowReversedChange = {}, onDraw = {}, onReset = {})
    }
}
