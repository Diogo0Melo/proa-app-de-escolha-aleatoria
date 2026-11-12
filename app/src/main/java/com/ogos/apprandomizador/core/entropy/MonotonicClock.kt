package com.ogos.apprandomizador.core.entropy

import android.os.SystemClock

fun interface MonotonicClock {
    fun nowMillis(): Long
}

object AndroidMonotonicClock : MonotonicClock {
    override fun nowMillis(): Long = SystemClock.elapsedRealtime()
}
