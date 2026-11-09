package com.ogos.apprandomizador.ui.speech

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/** Entrada por voz: [available] indica se há um reconhecedor no aparelho; [start] abre a captura. */
data class SpeechInput(val available: Boolean, val start: () -> Unit)

/**
 * Usa o reconhecedor de voz do sistema via [RecognizerIntent]. A captura acontece no app do
 * sistema, então o app não precisa pedir RECORD_AUDIO. Em Android 11+, a visibilidade do
 * reconhecedor depende do bloco <queries> do manifesto.
 */
@Composable
fun rememberSpeechInput(prompt: String, onResult: (String) -> Unit): SpeechInput {
    val context = LocalContext.current
    val currentOnResult by rememberUpdatedState(onResult)
    val available = remember(context) { isSpeechInputAvailable(context) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                ?.trim()
            if (!spoken.isNullOrEmpty()) currentOnResult(spoken)
        }
    }
    return remember(available, launcher, prompt) {
        SpeechInput(available = available) {
            try {
                launcher.launch(recognizerIntent(prompt))
            } catch (_: ActivityNotFoundException) {
                // Sem reconhecedor ativo: o botão já fica oculto nesse caso; a captura é ignorada.
            }
        }
    }
}

fun isSpeechInputAvailable(context: Context): Boolean =
    recognizerIntent(prompt = "").resolveActivity(context.packageManager) != null

internal fun recognizerIntent(prompt: String): Intent =
    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.forLanguageTag("pt-BR").toLanguageTag())
        .putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
        .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
