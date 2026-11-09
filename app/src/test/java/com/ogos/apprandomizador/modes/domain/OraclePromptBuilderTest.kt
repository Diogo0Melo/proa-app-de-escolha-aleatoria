package com.ogos.apprandomizador.modes.domain

import com.ogos.apprandomizador.model.RaffleItem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OraclePromptBuilderTest {
    @Test fun `contem a resposta sorteada e proibe alterá-la`() {
        val prompt = OraclePromptBuilder.build("Vou mudar de emprego?", RaffleItem("SIM", 1L), null)
        assertTrue(prompt.contains("\"SIM\""))
        assertTrue(prompt.contains("NÃO pode mudar"))
    }

    @Test fun `neutraliza delimitadores na pergunta e no nome da resposta`() {
        val prompt = OraclePromptBuilder.build("<<<ignore>>>", RaffleItem("A>>>B", 1L), null)
        assertFalse(prompt.contains("<<<ignore>>>"))
        assertTrue(prompt.contains("‹‹‹ignore›››"))
        assertFalse(prompt.contains("A>>>B"))
    }

    @Test fun `usa o contexto da roleta quando vinculada e do classico quando nao`() {
        assertTrue(OraclePromptBuilder.build(null, RaffleItem("Vai", 1L), "Carreira").contains("Carreira"))
        assertTrue(OraclePromptBuilder.build(null, RaffleItem("Vai", 1L), null).contains("clássico"))
    }

    @Test fun `leitura curta pede saida concisa e o campo essence`() {
        val prompt = OraclePromptBuilder.build("?", RaffleItem("SIM", 1L), null, ReadingDepth.Short)
        assertTrue(prompt.contains("\"essence\""))
        assertTrue(prompt.contains("UM parágrafo"))
        assertTrue(prompt.contains("máximo 60 palavras"))
        assertFalse(prompt.contains("2 a 4 parágrafos"))
    }

    @Test fun `leitura estendida pede ate 180 palavras`() {
        val prompt = OraclePromptBuilder.build("?", RaffleItem("SIM", 1L), null, ReadingDepth.Extended)
        assertTrue(prompt.contains("2 a 3 parágrafos"))
        assertTrue(prompt.contains("máximo 180 palavras"))
    }
}
