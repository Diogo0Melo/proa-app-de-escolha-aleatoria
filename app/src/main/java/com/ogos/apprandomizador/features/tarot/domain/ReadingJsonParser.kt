package com.ogos.apprandomizador.features.tarot.domain

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

/**
 * Converte a resposta bruta do provedor em [Reading] validada contra o sorteio real.
 *
 * Regras canônicas: posições inventadas pelo modelo são descartadas, títulos de seção
 * vêm sempre do nome da carta sorteada e texto livre sem JSON válido nunca é exibido.
 * Retorna null quando nenhuma seção é aproveitável.
 */
object ReadingJsonParser {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private const val DEFAULT_SUMMARY = "Uma nova perspectiva para refletir sobre sua tiragem."
    private const val MAX_SUMMARY = 1_000
    private const val MAX_TEXT = 2_000
    private const val MAX_TITLE = 120

    fun parse(raw: String, draw: List<DrawnCard>): Reading? {
        val candidate = extractJsonObject(raw) ?: return null
        val root = runCatching { json.parseToJsonElement(candidate).jsonObject }.getOrNull() ?: return null
        val sections = (root["sections"] as? JsonArray).orEmpty()
            .mapIndexedNotNull { index, element -> parseSection(element, index, draw) }
            .distinctBy { it.position }
            .sortedBy { section -> draw.indexOfFirst { it.position == section.position } }
        if (sections.isEmpty()) return null
        return Reading(
            summary = text(root["summary"], MAX_SUMMARY) ?: DEFAULT_SUMMARY,
            sections = sections,
            title = text(root["title"], MAX_TITLE),
            opening = text(root["opening"], MAX_TEXT),
            guidance = text(root["guidance"], MAX_TEXT),
            synthesis = text(root["synthesis"], MAX_TEXT)
        )
    }

    private fun parseSection(element: JsonElement, index: Int, draw: List<DrawnCard>): ReadingSection? {
        val item = element as? JsonObject ?: return null
        val body = text(item["text"], MAX_TEXT) ?: return null
        val position = resolvePosition(item["position"], index, draw) ?: return null
        val drawn = draw.firstOrNull { it.position == position } ?: return null
        return ReadingSection(position, drawn.card.name, body)
    }

    /** Posição ausente usa a ordem do sorteio; posição informada e inexistente no sorteio resulta em null. */
    private fun resolvePosition(element: JsonElement?, index: Int, draw: List<DrawnCard>): SpreadPosition? {
        if (element == null) return draw.getOrNull(index)?.position
        val value = text(element, 16)?.uppercase() ?: return null
        return runCatching { SpreadPosition.valueOf(value) }.getOrNull()
    }

    private fun text(element: JsonElement?, maxLength: Int): String? =
        (element as? JsonPrimitive)?.takeIf { it.isString }?.content
            ?.trim()?.takeIf(String::isNotEmpty)?.take(maxLength)

    private fun extractJsonObject(raw: String): String? {
        val cleaned = raw.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val start = cleaned.indexOf('{')
        val end = cleaned.lastIndexOf('}')
        return if (start >= 0 && end > start) cleaned.substring(start, end + 1) else null
    }
}
