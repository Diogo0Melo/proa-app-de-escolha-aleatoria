package com.ogos.apprandomizador.core.entropy

import java.security.SecureRandom

/** Implementação Android/JVM: SecureRandom + relógio monotônico + relógio de parede + thread. */
internal class SecureEntropySource(
    private val secureRandom: SecureRandom = SecureRandom(),
    private val nanoTime: () -> Long = System::nanoTime,
    private val wallClockMillis: () -> Long = System::currentTimeMillis,
    private val threadId: () -> Long = { Thread.currentThread().id }
) : EntropySource {
    override fun nextSeed(): ByteArray =
        spinSeedMaterial(
            secureBytes = ByteArray(SECURITY_SEED_BYTES).also(secureRandom::nextBytes),
            nanoTime = nanoTime(),
            wallClockMillis = wallClockMillis(),
            threadId = threadId()
        )

    private companion object {
        const val SECURITY_SEED_BYTES = 32
    }
}
