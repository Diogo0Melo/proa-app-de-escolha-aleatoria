package com.ogos.apprandomizador.fingers.domain

import com.ogos.apprandomizador.core.entropy.deriveFingerStream
import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class FingersPickerTest {
    private fun seed(i: Int): ByteArray = MessageDigest.getInstance("SHA-256")
        .digest(ByteArray(4) { (i ushr (it * 8)).toByte() })

    @Test fun `mesma semente e mesmo requestId produzem o mesmo sorteio`() {
        val first = FingersPicker.pick(4, deriveFingerStream(seed(7), 1))
        val second = FingersPicker.pick(4, deriveFingerStream(seed(7), 1))
        assertEquals(first, second)
    }

    @Test fun `exige pelo menos dois dedos`() {
        assertThrows(IllegalArgumentException::class.java) {
            FingersPicker.pick(1, deriveFingerStream(seed(1), 1))
        }
    }

    @Test fun `vencedor fica sempre dentro da faixa de dedos`() {
        for (fingers in 2..10) {
            repeat(200) { i ->
                val pick = FingersPicker.pick(fingers, deriveFingerStream(seed(i), 1))
                assertTrue("índice ${pick.winnerIndex} fora de 0..${fingers - 1}", pick.winnerIndex in 0 until fingers)
            }
        }
    }

    @Test fun `vencedor e uniforme entre 3 dedos`() {
        val counts = IntArray(3)
        repeat(9_000) { i ->
            counts[FingersPicker.pick(3, deriveFingerStream(seed(i), 1)).winnerIndex]++
        }
        counts.forEach { assertTrue("contagem fora do esperado: $it", it in 2_700..3_300) }
    }

    @Test fun `sentido e aproximadamente 50 a 50`() {
        var clockwise = 0
        repeat(10_000) { i ->
            if (FingersPicker.pick(2, deriveFingerStream(seed(i), 1)).direction == FingersDirection.CLOCKWISE) {
                clockwise++
            }
        }
        assertTrue("horário=$clockwise", clockwise in 4_700..5_300)
    }
}
