package com.ogos.apprandomizador.core.ai.domain

import com.ogos.apprandomizador.core.ai.domain.BaseUrlNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BaseUrlNormalizerTest {
    @Test fun `normaliza sufixo chat e barra final sem inventar v1`() {
        assertEquals("https://api.example/v1", BaseUrlNormalizer.normalize(" https://api.example/v1/chat/completions/ "))
        assertEquals("https://api.example/custom", BaseUrlNormalizer.normalize("https://api.example/custom/"))
    }

    @Test fun `recusa HTTP e URLs com credenciais query ou fragmento`() {
        assertThrows(IllegalArgumentException::class.java) { BaseUrlNormalizer.normalize("http://localhost:8080/v1") }
        assertThrows(IllegalArgumentException::class.java) { BaseUrlNormalizer.normalize("https://user:pass@example.com/v1") }
        assertThrows(IllegalArgumentException::class.java) { BaseUrlNormalizer.normalize("https://example.com/v1?key=x") }
        assertThrows(IllegalArgumentException::class.java) { BaseUrlNormalizer.normalize("https://example.com/v1#frag") }
    }

    @Test fun `authority inclui porta e normaliza host`() {
        assertEquals("api.example:8443", BaseUrlNormalizer.authority("https://API.EXAMPLE:8443/v1"))
    }
}
