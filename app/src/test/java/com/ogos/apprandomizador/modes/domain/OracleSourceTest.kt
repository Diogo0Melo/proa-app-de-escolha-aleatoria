package com.ogos.apprandomizador.modes.domain

import com.ogos.apprandomizador.core.model.ItemList
import com.ogos.apprandomizador.core.model.RaffleItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OracleSourceTest {
    @Test fun `itens em branco nunca entram no sorteio`() {
        val list = ItemList(
            id = 1L,
            topic = "Carreira",
            items = listOf(RaffleItem("Sim", 1L), RaffleItem("  ", 1L), RaffleItem("Talvez", 0L))
        )
        val source = list.asOracleSource()
        assertEquals("Carreira", source.topic)
        assertEquals(listOf(RaffleItem("Sim", 1L)), source.items)
    }

    @Test fun `classico usa as seis respostas e nenhum tema`() {
        assertNull(OracleSource.CLASSIC.topic)
        assertEquals(DefaultOracleAnswers.items, OracleSource.CLASSIC.items)
    }
}
