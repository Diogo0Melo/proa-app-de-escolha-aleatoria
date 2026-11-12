package com.ogos.apprandomizador.core.entropy

/**
 * Abstração pública para fornecer uma semente criptográfica por operação.
 * A interface não expõe detalhes de plataforma e permite fontes determinísticas em testes.
 */
fun interface EntropySource {
    fun nextSeed(): ByteArray
}
