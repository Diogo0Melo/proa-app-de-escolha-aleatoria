package com.ogos.apprandomizador.modes.domain

import com.ogos.apprandomizador.core.entropy.deriveOracleStream
import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class OracleDrawerTest {
    private fun seed(index: Int) = MessageDigest.getInstance("SHA-256").digest(ByteArray(4) { (index ushr (it * 8)).toByte() })
    private val items = DefaultOracleAnswers.items

    @Test fun `mesma semente produz a mesma resposta`() {
        val first = OracleDrawer.draw(items, deriveOracleStream(seed(7), 1))
        val second = OracleDrawer.draw(items, deriveOracleStream(seed(7), 1))
        assertEquals(first, second)
    }

    @Test fun `as seis respostas saem com distribuicao uniforme`() {
        val counts = mutableMapOf<String, Int>()
        repeat(6_000) { index ->
            val name = OracleDrawer.draw(items, deriveOracleStream(seed(index), 1)).name
            counts[name] = (counts[name] ?: 0) + 1
        }
        assertEquals(items.size, counts.size)
        counts.values.forEach { assertTrue("contagem fora do esperado: $it", it in 800..1_200) }
    }

    @Test fun `rejeita fonte vazia`() {
        assertThrows(IllegalArgumentException::class.java) {
            OracleDrawer.draw(emptyList(), deriveOracleStream(seed(1), 1))
        }
    }

    @Test fun `seis respostas padrao com nomes unicos`() {
        assertEquals(6, items.size)
        assertEquals(6, items.map { it.name }.distinct().size)
    }
}
