package com.ogos.apprandomizador.modes.domain

/** Leitura da IA sobre uma resposta já sorteada. Comenta a resposta e nunca a substitui. */
data class OracleReading(
    val title: String?,
    val essence: String?,
    val reading: String,
    val affirmation: String?
)
