package com.ogos.apprandomizador.modes.domain

import com.ogos.apprandomizador.viewmodel.deriveOracleStream
import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class YesNoOracleTest {
    @Test
    fun `same seed and request produce same answer`() {
        val seed = ByteArray(32) { it.toByte() }
        assertEquals(
            YesNoOracle.ask(deriveOracleStream(seed, 1)),
            YesNoOracle.ask(deriveOracleStream(seed, 1))
        )
    }

    @Test
    fun `ten thousand deterministic seeds stay within expected balance`() {
        var yes = 0
        repeat(10_000) { index ->
            val seed = MessageDigest.getInstance("SHA-256")
                .digest(ByteArray(4) { offset -> (index ushr (offset * 8)).toByte() })
            if (YesNoOracle.ask(deriveOracleStream(seed, 1)) == YesNoAnswer.YES) yes++
        }
        assertTrue("yes count was $yes", yes in 4_700..5_300)
    }
}
