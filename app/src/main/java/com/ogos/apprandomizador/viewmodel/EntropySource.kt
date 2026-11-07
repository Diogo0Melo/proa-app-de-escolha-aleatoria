package com.ogos.apprandomizador.viewmodel

import java.security.SecureRandom

/**
 * Abstração pública para fornecer uma semente criptográfica por operação.
 * A interface não expõe detalhes de plataforma e permite fontes determinísticas em testes.
 */
fun interface EntropySource {
    fun nextSeed(): ByteArray
}

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
