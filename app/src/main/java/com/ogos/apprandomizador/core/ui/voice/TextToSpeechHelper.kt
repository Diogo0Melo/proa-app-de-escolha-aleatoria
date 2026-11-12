package com.ogos.apprandomizador.core.ui.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SpeechState(val available: Boolean = false, val speaking: Boolean = false)

/**
 * Narração por TextToSpeech do sistema. [state] informa se há voz pt-BR disponível e se
 * algo está sendo lido. Use [release] ao sair da tela para liberar o motor de voz.
 */
class SpeechPlayer(context: Context, private val locale: Locale = Locale.forLanguageTag("pt-BR")) {
    private val _state = MutableStateFlow(SpeechState())
    val state: StateFlow<SpeechState> = _state.asStateFlow()

    private var engine: TextToSpeech? = null
    private var lastUtteranceId: String? = null
    private var released = false

    init {
        engine = TextToSpeech(context.applicationContext) { status -> onInit(status) }
    }

    fun speak(text: String) {
        val tts = engine ?: return
        if (released || !_state.value.available) return
        val chunks = splitForSpeech(text)
        if (chunks.isEmpty()) return
        chunks.forEachIndexed { index, chunk ->
            val id = "reading-$index"
            val mode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            tts.speak(chunk, mode, null, id)
        }
        lastUtteranceId = "reading-${chunks.lastIndex}"
        setSpeaking(true)
    }

    fun stop() {
        engine?.stop()
        lastUtteranceId = null
        setSpeaking(false)
    }

    fun release() {
        if (released) return
        released = true
        engine?.stop()
        engine?.shutdown()
        engine = null
        setSpeaking(false)
    }

    private fun onInit(status: Int) {
        val tts = engine ?: return
        if (status != TextToSpeech.SUCCESS) {
            _state.update { it.copy(available = false) }
            return
        }
        val result = tts.setLanguage(locale)
        val supported = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit
            override fun onDone(utteranceId: String?) = finishIfLast(utteranceId)
            override fun onError(utteranceId: String?) = finishIfLast(utteranceId)
            override fun onError(utteranceId: String?, errorCode: Int) = finishIfLast(utteranceId)
        })
        _state.update { it.copy(available = supported) }
    }

    /** Só encerra o estado "falando" quando a última parte da fala terminar. */
    private fun finishIfLast(utteranceId: String?) {
        if (utteranceId == lastUtteranceId) setSpeaking(false)
    }

    private fun setSpeaking(value: Boolean) {
        _state.update { it.copy(speaking = value) }
    }
}

@Composable
fun rememberSpeechPlayer(): SpeechPlayer {
    val context = LocalContext.current.applicationContext
    val player = remember(context) { SpeechPlayer(context) }
    DisposableEffect(player) {
        onDispose { player.release() }
    }
    return player
}
