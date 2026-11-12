package com.ogos.apprandomizador.core.entropy

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class EntropyTest {
    @Test
    fun `seed material is deterministic and always a sha256 digest`() {
        val bytes = ByteArray(32) { it.toByte() }
        val first = spinSeedMaterial(bytes, 10L, 20L, 30L)
        val second = spinSeedMaterial(bytes, 10L, 20L, 30L)
        assertEquals(32, first.size)
        assertArrayEquals(first, second)
    }

    @Test
    fun `each entropy input contributes to the seed`() {
        val bytes = ByteArray(32) { it.toByte() }
        val baseline = spinSeedMaterial(bytes, 10L, 20L, 30L)
        assertFalse(baseline.contentEquals(spinSeedMaterial(bytes, 11L, 20L, 30L)))
        assertFalse(baseline.contentEquals(spinSeedMaterial(bytes, 10L, 21L, 30L)))
        assertFalse(baseline.contentEquals(spinSeedMaterial(bytes, 10L, 20L, 31L)))
        assertFalse(baseline.contentEquals(spinSeedMaterial(bytes.copyOf().also { it[0]++ }, 10L, 20L, 30L)))
    }

    @Test
    fun `shuffle preserves every element`() {
        val input = (0 until 100).toList()
        val shuffled = cryptoShuffled(input) { bound -> bound - 1 }
        assertEquals(input.toSet(), shuffled.toSet())
        assertEquals(input.size, shuffled.size)
    }
}
