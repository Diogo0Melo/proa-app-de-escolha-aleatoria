package com.ogos.apprandomizador.core.entropy

import org.junit.Assert.assertNotEquals
import org.junit.Test

class EntropyOracleStreamTest {
    @Test
    fun `same input produces same oracle bytes`() {
        val seed = ByteArray(32) { it.toByte() }
        val first = deriveOracleStream(seed, 7)
        val second = deriveOracleStream(seed, 7)
        repeat(12) { assertEqualsInt(first.nextIntNonNegative(), second.nextIntNonNegative()) }
    }

    @Test
    fun `different request id changes oracle stream`() {
        val seed = ByteArray(32) { it.toByte() }
        assertNotEquals(
            deriveOracleStream(seed, 1).nextIntNonNegative(),
            deriveOracleStream(seed, 2).nextIntNonNegative()
        )
    }

    @Test
    fun `oracle stream is domain separated from spin stream`() {
        val seed = ByteArray(32) { it.toByte() }
        assertNotEquals(
            deriveOracleStream(seed, 1).nextIntNonNegative(),
            deriveSpinStream(seed, 1).nextIntNonNegative()
        )
    }

    private fun assertEqualsInt(expected: Int, actual: Int) {
        org.junit.Assert.assertEquals(expected, actual)
    }
}
