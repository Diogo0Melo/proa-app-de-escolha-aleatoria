package com.ogos.apprandomizador.modes.domain

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object ReadingJsonParser {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun parse(raw: String, draw: List<DrawnCard>): Reading {
        val cleaned = raw.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val start = cleaned.indexOf('{')
        val end = cleaned.lastIndexOf('}')
        val candidate = if (start >= 0 && end > start) cleaned.substring(start, end + 1) else cleaned
        return runCatching {
            val root = json.parseToJsonElement(candidate).jsonObject
            val summary = root["summary"]?.jsonPrimitive?.content?.takeIf(String::isNotBlank)
                ?: "Uma nova perspectiva para refletir sobre sua tiragem."
            val sections = root["sections"]?.jsonArray.orEmpty().mapIndexedNotNull { index, element ->
                val item = element.jsonObject
                val text = item["text"]?.jsonPrimitive?.content?.takeIf(String::isNotBlank) ?: return@mapIndexedNotNull null
                val position = item["position"]?.jsonPrimitive?.content?.let { value ->
                    runCatching { SpreadPosition.valueOf(value.uppercase()) }.getOrNull()
                } ?: draw.getOrNull(index)?.position ?: SpreadPosition.SINGLE
                val title = draw.firstOrNull { it.position == position }?.card?.name ?: positionLabel(position)
                ReadingSection(position, title, text)
            }
            Reading(summary, sections.ifEmpty { fallback(raw) .sections })
        }.getOrElse { Reading("A resposta do provedor foi recebida em formato livre.", listOf(ReadingSection(SpreadPosition.SINGLE, "Leitura", raw.trim().take(8_000)))) }
    }

    private fun fallback(raw: String) = Reading("Leitura complementar recebida.", listOf(ReadingSection(SpreadPosition.SINGLE, "Leitura", raw.trim().take(8_000))))
    private fun positionLabel(position: SpreadPosition) = when (position) {
        SpreadPosition.SINGLE -> "Carta"; SpreadPosition.PAST -> "Passado"; SpreadPosition.PRESENT -> "Presente"; SpreadPosition.FUTURE -> "Futuro"
    }
}
