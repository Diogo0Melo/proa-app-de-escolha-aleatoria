package com.ogos.apprandomizador.modes.ui.yesno

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import android.os.SystemClock
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ogos.apprandomizador.R
import kotlinx.coroutines.delay

@Composable
fun YesNoRoute(
    viewModel: YesNoViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var question by rememberSaveable { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(state.requestId, state.phase) {
        if (state.phase == YesNoPhase.Revealing) {
            val deadline = state.revealStartElapsedRealtime + state.durationMillis
            val remaining = deadline - SystemClock.elapsedRealtime()
            if (remaining > 0L) delay(remaining)
            viewModel.onRevealFinished(state.requestId)
        }
    }

    YesNoScreen(
        state = state,
        question = question,
        onQuestionChange = { question = it.take(140) },
        onAsk = {
            keyboard?.hide()
            viewModel.onAsk()
        },
        onReset = {
            viewModel.onReset()
            question = ""
        },
        modifier = modifier
    )
}

@Composable
fun YesNoScreen(
    state: YesNoUiState,
    question: String,
    onQuestionChange: (String) -> Unit,
    onAsk: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.yes_no_prompt),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = question,
            onValueChange = onQuestionChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.optional_question)) },
            supportingText = { Text(stringResource(R.string.question_not_saved)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (state.phase != YesNoPhase.Revealed) onAsk()
            })
        )
        Spacer(Modifier.height(28.dp))
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(vertical = 32.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(
                    targetState = state.answer.takeIf { state.phase == YesNoPhase.Revealed },
                    transitionSpec = { (fadeIn() + scaleIn()).togetherWith(fadeOut()) },
                    label = "yes-no-answer"
                ) { answer ->
                    Text(
                        text = when {
                            answer == null && (state.phase == YesNoPhase.Revealing || state.phase == YesNoPhase.Recording) -> "…"
                            answer == null -> stringResource(R.string.ask)
                            answer == com.ogos.apprandomizador.modes.domain.YesNoAnswer.YES ->
                                stringResource(R.string.answer_yes)
                            else -> stringResource(R.string.answer_no)
                        },
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                    )
                }
            }
        }
        Spacer(Modifier.height(28.dp))
        if (state.phase == YesNoPhase.Revealed) {
            Button(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.ask_again))
            }
        } else {
            Button(
                onClick = onAsk,
                enabled = state.phase == YesNoPhase.Idle,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(
                    if (state.phase == YesNoPhase.Revealing || state.phase == YesNoPhase.Recording) stringResource(R.string.revealing)
                    else stringResource(R.string.ask)
                )
            }
        }
    }
}


@Preview(showBackground = true, name = "Sim ou Não")
@Composable
private fun YesNoScreenPreview() {
    com.ogos.apprandomizador.ui.theme.AppRandomizadorTheme {
        YesNoScreen(
            state = YesNoUiState(),
            question = "",
            onQuestionChange = {},
            onAsk = {},
            onReset = {}
        )
    }
}
