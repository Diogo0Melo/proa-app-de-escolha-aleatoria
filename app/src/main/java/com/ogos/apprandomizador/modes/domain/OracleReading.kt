package com.ogos.apprandomizador.modes.domain

/** Profundidade pedida à IA: a curta é automática; a estendida só existe se o usuário pedir. */
enum class ReadingDepth { Short, Extended }

/** Leitura da IA sobre uma resposta já sorteada. Comenta a resposta e nunca a substitui. */
data class OracleReading(
    val title: String?,
    val essence: String?,
    val reading: String,
    val affirmation: String?,
    val depth: ReadingDepth
)
