package com.ogos.apprandomizador.core.entropy

import com.ogos.apprandomizador.features.randomize.domain.SpinTarget
import java.security.MessageDigest

/**
 * Fonte única de aleatoriedade de um sorteio.
 *
 * O vencedor, a encenação visual (percurso, excesso, vacilo, ritmo, duração) e o
 * embaralhamento da sequência de exibição saem do MESMO fluxo criptográfico,
 * derivado de um coquetel de entropia. Assim o giro não parece calculado: cada
 * rodada percorre um caminho diferente e nenhum padrão se repete.
 *
 * O fluxo é uma cadeia de hashes SHA-256 lida em blocos: 4 bytes para índices,
 * 8 bytes para duplas precisas. Quando o bloco atual seca, a chave avança para
 * `SHA-256(chave anterior)` — uma cadeia de hashes, então o fluxo nunca acaba e
 * nenhum byte se repete entre rodadas.
 */
internal class EntropyStream(initialKey: ByteArray) {

    private var key: ByteArray
    private var cursor = 0

    init {
        require(initialKey.size >= MIN_READABLE_BLOCK) {
            "seed must carry at least $MIN_READABLE_BLOCK bytes, was ${initialKey.size}"
        }
        key = initialKey.copyOf()
    }

    /** Próximo inteiro não negativo de 31 bits (os 2 bits de topo são descartados). */
    fun nextIntNonNegative(): Int {
        val b = read(INT_BLOCK)
        return ((b[0].toInt() and 0x7F) shl 24) or
            ((b[1].toInt() and 0xFF) shl 16) or
            ((b[2].toInt() and 0xFF) shl 8) or
            (b[3].toInt() and 0xFF)
    }

    /** Próximo double em `[0.0, 1.0)` de 53 bits. */
    fun nextDouble(): Double {
        val b = read(LONG_BLOCK)
        var v = 0L
        for (i in 0 until LONG_BLOCK) {
            v = (v shl 8) or (b[i].toLong() and 0xFFL)
        }
        return (v ushr DOUBLE_BITS).toDouble() / (1L shl DOUBLE_BITS).toDouble()
    }

    private fun read(n: Int): ByteArray {
        if (cursor + n > key.size) advanceKey()
        val out = key.copyOfRange(cursor, cursor + n)
        cursor += n
        return out
    }

    /** Avança a cadeia de hashes: a chave vira o hash dela mesma. */
    private fun advanceKey() {
        key = sha256(key)
        cursor = 0
    }

    private companion object {
        const val INT_BLOCK = 4
        const val LONG_BLOCK = 8
        const val MIN_READABLE_BLOCK = 8
        const val DOUBLE_BITS = 53
    }
}

/**
 * Inteiro uniforme em `[0, bound)` por rejeição sobre blocos de 4 bytes.
 *
 * `limit = Int.MAX_VALUE - Int.MAX_VALUE % bound` descarta os restos acima do
 * maior múltiplo de `bound`, eliminando o viés do `%` simples. A cadeia de
 * hashes garante que sempre haverá bytes para tentar.
 */
internal fun EntropyStream.uniformInt(bound: Int): Int {
    require(bound > 0) { "bound must be positive, was $bound" }
    val limit = Int.MAX_VALUE - Int.MAX_VALUE % bound
    while (true) {
        val value = nextIntNonNegative()
        if (value < limit) return value % bound
    }
}

/**
 * Índice do vencedor em `[0, size)` por rejeição, conforme o contrato do sorteio:
 * até [MAX_REJECTION_ATTEMPTS] tentativas sobre o fluxo e, se elas secarem,
 * fallback para [fallback] — que em produção é `SecureRandom.nextInt(size)`,
 * igualmente uniforme.
 */
internal fun uniformIndexOf(
    stream: EntropyStream,
    size: Int,
    fallback: (Int) -> Int
): Int {
    require(size > 0) { "size must be positive, was $size" }
    val limit = Int.MAX_VALUE - Int.MAX_VALUE % size
    repeat(MAX_REJECTION_ATTEMPTS) {
        val value = stream.nextIntNonNegative()
        if (value < limit) return value % size
    }
    return fallback(size)
}

/** Índice do vencedor a partir do fluxo do sorteio (caminho de produção). */
internal fun pickWinnerIndex(stream: EntropyStream, size: Int, fallback: (Int) -> Int): Int =
    uniformIndexOf(stream, size, fallback)

/** Inteiro uniforme em `[min, max]` (ambos inclusos). */
internal fun EntropyStream.nextIntInRange(min: Int, max: Int): Int {
    require(min <= max) { "min ($min) must not exceed max ($max)" }
    return min + uniformInt(max - min + 1)
}

/** Double uniforme em `[min, max]`. */
internal fun EntropyStream.nextDoubleInRange(min: Double, max: Double): Double {
    require(min <= max) { "min ($min) must not exceed max ($max)" }
    return min + nextDouble() * (max - min)
}

internal const val MAX_REJECTION_ATTEMPTS = 8

/**
 * Mistura as quatro fontes de entropia do sorteio em 32 bytes:
 * SecureRandom (32B) + nanoTime + relógio de parede + id da thread -> SHA-256.
 *
 * Função pura: as fontes entram como parâmetros, então o teste consegue
 * reproduzir a mistura byte a byte.
 */
internal fun spinSeedMaterial(
    secureBytes: ByteArray,
    nanoTime: Long,
    wallClockMillis: Long,
    threadId: Long
): ByteArray {
    val input = ByteArray(secureBytes.size + 24)
    secureBytes.copyInto(input)
    putLong(input, secureBytes.size, nanoTime)
    putLong(input, secureBytes.size + 8, wallClockMillis)
    putLong(input, secureBytes.size + 16, threadId)
    return sha256(input)
}

/**
 * Fluxo do sorteio em si: vencedor + encenação. Derivado do seed junto com o
 * `requestId`, de modo que duas rodadas seguidas — mesmo com o mesmo vencedor —
 * produzam percurso, excesso e ritmo diferentes.
 */
internal fun deriveSpinStream(seed: ByteArray, requestId: Long): EntropyStream =
    EntropyStream(sha256(tagged(seed, SPIN_STREAM_TAG, requestId)))

/**
 * Fluxo separado para o embaralhamento da sequência de exibição. Domínio próprio
 * (tag distinta) para que os bytes usados no vencedor não reapareçam na ordem
 * dos cartões.
 */
internal fun deriveShuffleStream(seed: ByteArray, requestId: Long): EntropyStream =
    EntropyStream(sha256(tagged(seed, SHUFFLE_STREAM_TAG, requestId)))

/**
 * Reconstrói o fluxo de embaralhamento a partir apenas da semente de 64 bits que
 * viaja no [SpinTarget]. Oito bytes de origem criptográfica abastecem a cadeia de
 * hashes, então o fluxo se estende indefinidamente sem carregar o digest inteiro
 * até a View.
 */
internal fun shuffleStreamFrom(seedLong: Long): EntropyStream {
    val bytes = ByteArray(8)
    putLong(bytes, 0, seedLong)
    return EntropyStream(bytes)
}

/**
 * Fisher-Yates dirigido por um gerador de inteiros uniformes em `[0, n)`.
 * Embaralha a lista sem repetir elemento nem perder nenhum, sempre.
 */
internal inline fun <T> cryptoShuffled(source: List<T>, randomBound: (Int) -> Int): List<T> {
    if (source.size <= 1) return source.toList()
    val result = source.toMutableList()
    for (i in result.lastIndex downTo 1) {
        val j = randomBound(i + 1)
        val tmp = result[i]
        result[i] = result[j]
        result[j] = tmp
    }
    return result
}

private fun tagged(seed: ByteArray, tag: Int, requestId: Long): ByteArray {
    val input = ByteArray(seed.size + 12)
    seed.copyInto(input)
    input[seed.size] = (tag ushr 24).toByte()
    input[seed.size + 1] = (tag ushr 16).toByte()
    input[seed.size + 2] = (tag ushr 8).toByte()
    input[seed.size + 3] = tag.toByte()
    putLong(input, seed.size + 4, requestId)
    return input
}

private const val SPIN_STREAM_TAG = 0x53_50_49_4E  // "SPIN"
private const val SHUFFLE_STREAM_TAG = 0x53_48_46_54  // "SHFT"

internal fun sha256(input: ByteArray): ByteArray =
    MessageDigest.getInstance("SHA-256").digest(input)

private fun putLong(target: ByteArray, offset: Int, value: Long) {
    target[offset] = (value ushr 56).toByte()
    target[offset + 1] = (value ushr 48).toByte()
    target[offset + 2] = (value ushr 40).toByte()
    target[offset + 3] = (value ushr 32).toByte()
    target[offset + 4] = (value ushr 24).toByte()
    target[offset + 5] = (value ushr 16).toByte()
    target[offset + 6] = (value ushr 8).toByte()
    target[offset + 7] = value.toByte()
}


/** Tag de domínio independente: bytes do oráculo nunca reutilizam o fluxo da roleta. */
private const val ORACLE_STREAM_TAG = 0x4F_52_43_4C // "ORCL"

internal fun deriveOracleStream(seed: ByteArray, requestId: Long): EntropyStream =
    EntropyStream(sha256(tagged(seed, ORACLE_STREAM_TAG, requestId)))

/** Tag de domínio da tela de dedos: nenhum byte se repete entre ela e as demais. */
private const val FINGER_STREAM_TAG = 0x46_49_4E_47 // "FING"

internal fun deriveFingerStream(seed: ByteArray, requestId: Long): EntropyStream =
    EntropyStream(sha256(tagged(seed, FINGER_STREAM_TAG, requestId)))
