package com.ogos.apprandomizador.modes.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class SecretTest {
    @Test fun `representacao textual mascara o conteudo`() {
        assertEquals("Secret(••••••••)", Secret("super-secret-token").toString())
    }
}
