package com.ogos.apprandomizador.modes.domain

data class ReadingSection(val position: SpreadPosition, val title: String, val text: String)
data class Reading(val summary: String, val sections: List<ReadingSection>)

fun interface ReadingInterpreter {
    suspend fun interpret(question: String?, draw: List<DrawnCard>): Reading
}
