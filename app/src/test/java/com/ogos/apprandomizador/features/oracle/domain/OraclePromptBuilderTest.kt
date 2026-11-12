package com.ogos.apprandomizador.features.oracle.domain

import com.ogos.apprandomizador.core.model.RaffleItem
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

    @Test fun `a leitura pede um paragrafo curto e o campo essence`() {
        val prompt = OraclePromptBuilder.build("?", RaffleItem("SIM", 1L), null)
        assertTrue(prompt.contains("\"essence\""))
        assertTrue(prompt.contains("UM parágrafo"))
        assertTrue(prompt.contains("máximo 60 palavras"))
    }
}
