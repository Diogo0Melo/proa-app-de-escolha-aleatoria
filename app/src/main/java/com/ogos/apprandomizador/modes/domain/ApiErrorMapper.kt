package com.ogos.apprandomizador.modes.domain

import java.io.IOException
import javax.net.ssl.SSLException

object ApiErrorMapper {
    fun message(error: Throwable): String {
        val detail = error.message.orEmpty()
        return when {
            error is SSLException -> "Falha de segurança na conexão (certificado)."
            detail.contains("401") || detail.contains("403") -> "A chave foi recusada pelo provedor."
            detail.contains("429") -> "Limite do provedor atingido. Tente mais tarde."
            detail.contains("404") -> "Endereço ou modelo não encontrado. Confira a URL e o model ID."
            detail.contains("HTTPS") -> "Só endereços HTTPS são aceitos."
            detail.contains("chave", ignoreCase = true) -> "Informe a chave da API."
            detail.contains("model ID", ignoreCase = true) -> "Informe o model ID para testar este provedor."
            detail.startsWith("HTTP") -> "O provedor respondeu num formato inesperado."
            error is LlmResponseFormatException -> "O provedor respondeu num formato inesperado."
            error is IOException -> "Sem conexão com o provedor."
            else -> "O provedor respondeu num formato inesperado."
        }
    }
}
