package com.ogos.apprandomizador.modes.domain

import com.ogos.apprandomizador.core.ai.domain.ApiErrorMapper
import com.ogos.apprandomizador.core.ai.domain.LlmHttpException
import com.ogos.apprandomizador.core.ai.domain.LlmResponseFormatException
import java.io.IOException
import javax.net.ssl.SSLException
import org.junit.Assert.assertEquals
import org.junit.Test

class ApiErrorMapperTest {
    @Test fun `mapeia status de autenticacao limite e endpoint`() {
        assertEquals("A chave foi recusada pelo provedor.", ApiErrorMapper.message(LlmHttpException(401, "unauthorized")))
        assertEquals("A chave foi recusada pelo provedor.", ApiErrorMapper.message(LlmHttpException(403, "forbidden")))
        assertEquals("Limite do provedor atingido. Tente mais tarde.", ApiErrorMapper.message(LlmHttpException(429, "rate limit")))
        assertEquals("Endereço ou modelo não encontrado. Confira a URL e o model ID.", ApiErrorMapper.message(LlmHttpException(404, "not found")))
    }

    @Test fun `mapeia rede certificado e resposta inesperada`() {
        assertEquals("Sem conexão com o provedor.", ApiErrorMapper.message(IOException("offline")))
        assertEquals("Falha de segurança na conexão (certificado).", ApiErrorMapper.message(SSLException("bad cert")))
        assertEquals("O provedor respondeu num formato inesperado.", ApiErrorMapper.message(IllegalStateException("invalid JSON")))
    }

    @Test fun `resposta fora do formato nao aparece como falha de conexao`() {
        assertEquals("O provedor respondeu num formato inesperado.", ApiErrorMapper.message(LlmResponseFormatException("Formato inesperado do provedor")))
    }
}
