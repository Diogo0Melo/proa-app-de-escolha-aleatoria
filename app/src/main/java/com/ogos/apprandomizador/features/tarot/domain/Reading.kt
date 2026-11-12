package com.ogos.apprandomizador.features.tarot.domain

data class ReadingSection(val position: SpreadPosition, val title: String, val text: String)

/**
 * Leitura interpretada. Os campos opcionais só são preenchidos pela leitura da IA;
 * a leitura offline usa apenas [summary] e [sections].
 */
data class Reading(
    val summary: String,
    val sections: List<ReadingSection>,
    val title: String? = null,
    val opening: String? = null,
    val guidance: String? = null,
    val synthesis: String? = null
)

fun interface ReadingInterpreter {
    suspend fun interpret(question: String?, draw: List<DrawnCard>): Reading
}
