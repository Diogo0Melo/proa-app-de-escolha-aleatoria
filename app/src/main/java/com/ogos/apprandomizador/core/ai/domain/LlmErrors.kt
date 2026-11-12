package com.ogos.apprandomizador.core.ai.domain

import java.io.IOException

class LlmHttpException(val statusCode: Int, message: String) : IOException("HTTP $statusCode: $message")

/** Resposta recebida do provedor, porém fora do formato esperado. Não é retentada. */
class LlmResponseFormatException(message: String) : IOException(message)
