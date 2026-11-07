package com.ogos.apprandomizador.modes.ui.tarot

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ogos.apprandomizador.R
import com.ogos.apprandomizador.modes.domain.DrawnCard
import com.ogos.apprandomizador.modes.domain.Spread
import com.ogos.apprandomizador.modes.domain.SpreadPosition
import kotlinx.coroutines.delay

@Composable
fun TarotRoute(viewModel: TarotViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(state.requestId, state.phase) {
        if (state.phase == TarotPhase.Revealing) {
            val remaining =
                state.revealStartElapsedRealtime + state.durationMillis - SystemClock.elapsedRealtime()
            if (remaining > 0) delay(remaining)
            viewModel.onRevealFinished(state.requestId)
        }
    }
    TarotScreen(
        state = state,
        onQuestionChange = viewModel::onQuestionChange,
        onSpreadChange = viewModel::onSpreadChange,
        onAllowReversedChange = viewModel::onAllowReversedChange,
        onDraw = { keyboard?.hide(); viewModel.onDraw() },
        onReset = viewModel::onReset,
        modifier = modifier
    )
}

@Composable
fun TarotScreen(
    state: TarotUiState,
    onQuestionChange: (String) -> Unit,
    onSpreadChange: (Spread) -> Unit,
    onAllowReversedChange: (Boolean) -> Unit,
    onDraw: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val fontScale = LocalDensity.current.fontScale
    val controlsEnabled = state.phase == TarotPhase.Idle
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            stringResource(R.string.tarot_prompt),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
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
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Switch(
                checked = state.allowReversed,
                onCheckedChange = onAllowReversedChange,
                enabled = controlsEnabled
            )
            Text(stringResource(R.string.tarot_allow_reversed))
        }
        Button(
            onClick = onDraw,
            enabled = controlsEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                when (state.phase) {
                    TarotPhase.Revealing -> stringResource(R.string.revealing)
                    TarotPhase.Interpreting -> stringResource(R.string.tarot_interpreting)
                    else -> stringResource(R.string.tarot_draw)
                }
            )
        }
        if (state.drawn.isNotEmpty()) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val vertical = maxWidth < 420.dp || fontScale > 1.3f
                if (vertical) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.drawn.forEach { TarotCardView(it) }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.drawn.forEach { TarotCardView(it, Modifier.weight(1f)) }
                    }
                }
            }
        }
        if (state.phase == TarotPhase.Revealed && state.reading != null) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(R.string.tarot_reading),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    SelectionContainer {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(state.reading.summary, style = MaterialTheme.typography.bodyLarge)
                            state.reading.sections.forEach { section ->
                                Text(
                                    section.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(section.text, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    Text(
                        stringResource(R.string.tarot_disclaimer),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Button(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.tarot_new_reading)) }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun TarotCardView(drawn: DrawnCard, modifier: Modifier = Modifier) {
    val position = when (drawn.position) {
        SpreadPosition.SINGLE -> stringResource(R.string.tarot_position_single)
        SpreadPosition.PAST -> stringResource(R.string.tarot_position_past)
        SpreadPosition.PRESENT -> stringResource(R.string.tarot_position_present)
        SpreadPosition.FUTURE -> stringResource(R.string.tarot_position_future)
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription =
                    "$position: ${drawn.card.name}${if (drawn.reversed) ", invertida" else ""}. ${
                        drawn.meaning.keywords.joinToString(", ")
                    }"
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                position.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                toRoman(drawn.card.number),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                drawn.card.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            if (drawn.reversed) Text(
                stringResource(R.string.tarot_reversed),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                drawn.meaning.keywords.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

private fun toRoman(number: Int): String {
    if (number == 0) return "0"
    val values = listOf(
        1000 to "M",
        900 to "CM",
        500 to "D",
        400 to "CD",
        100 to "C",
        90 to "XC",
        50 to "L",
        40 to "XL",
        10 to "X",
        9 to "IX",
        5 to "V",
        4 to "IV",
        1 to "I"
    )
    var remaining = number
    return buildString {
        values.forEach { (value, symbol) ->
            while (remaining >= value) {
                append(symbol); remaining -= value
            }
        }
    }
}
