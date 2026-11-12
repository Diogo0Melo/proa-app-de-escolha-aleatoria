package com.ogos.apprandomizador.core.ai.domain

import com.ogos.apprandomizador.core.ai.domain.Secret
import org.junit.Assert.assertEquals
import org.junit.Test

class SecretTest {
    @Test fun `representacao textual mascara o conteudo`() {
        assertEquals("Secret(••••••••)", Secret("super-secret-token").toString())
    }
}
