package com.ogos.apprandomizador.features.tarot.domain

class OfflineReadingInterpreter : ReadingInterpreter {
    override suspend fun interpret(question: String?, draw: List<DrawnCard>): Reading {
        require(draw.isNotEmpty()) { "A reading requires at least one drawn card" }
        val sections = draw.map { drawn ->
            val title = when (drawn.position) {
                SpreadPosition.SINGLE -> "Sua carta"
                SpreadPosition.PAST -> "Passado"
                SpreadPosition.PRESENT -> "Presente"
                SpreadPosition.FUTURE -> "Futuro"
            }
            val orientation = if (drawn.reversed) "Invertida" else "Em pé"
            ReadingSection(
                position = drawn.position,
                title = "$title · ${drawn.card.name}",
                text = "${drawn.meaning.essence} Palavras-chave: ${drawn.meaning.keywords.joinToString(", ")}. (Carta $orientation.)"
            )
        }
        val reversedCount = draw.count(DrawnCard::reversed)
        val dominant = draw.flatMap { it.meaning.keywords }.groupingBy { it }.eachCount()
            .maxByOrNull { it.value }?.key
        val balance = when {
            reversedCount == 0 -> "A tiragem aponta para temas que podem ser explorados com abertura e iniciativa."
            reversedCount == draw.size -> "A tiragem convida a olhar para dentro, rever padrões e respeitar o tempo de cada mudança."
            else -> "A combinação sugere equilibrar movimento e revisão, sem tratar os desafios como conclusões definitivas."
        }
        val questionContext = question?.trim()?.takeIf(String::isNotEmpty)
            ?.let { " Considerando sua pergunta: “${it.take(140)}”." }.orEmpty()
        val keywordContext = dominant?.let { " Um tema recorrente é $it." }.orEmpty()
        return Reading("$balance$keywordContext$questionContext", sections)
    }
}
