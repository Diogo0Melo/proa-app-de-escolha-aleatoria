package com.ogos.apprandomizador.ui.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import androidx.annotation.RawRes

/**
 * Lightweight SoundPool wrapper for short UI sound effects.
 * Uses USAGE_ASSISTANCE_SONIFICATION — no audio focus needed.
 */
class TickPlayer(context: Context, @RawRes tickRes: Int) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var soundId: Int = 0
    private var ready = false
    private var released = false

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0 && sampleId == soundId) {
                ready = true
            } else if (status != 0) {
                Log.w("TickPlayer", "Failed to load sound resource, status=$status")
            }
        }
        soundId = soundPool.load(context, tickRes, 1)
    }

    fun play(volume: Float): Int {
        if (released || volume <= 0f || !ready) return 0
        return soundPool.play(soundId, volume, volume, 1, 0, 1f)
    }

    fun pause() {
        if (!released) soundPool.autoPause()
    }

    fun resume() {
        if (!released) soundPool.autoResume()
    }

    fun release() {
        if (released) return
        released = true
        soundPool.setOnLoadCompleteListener(null)
        soundPool.release()
    }
}
