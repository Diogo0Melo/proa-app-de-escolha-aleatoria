package com.ogos.apprandomizador.modes.domain

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

/**
 * Converte a resposta bruta da IA em [OracleReading]. O formato não tem campo de resposta:
 * a IA não tem como trocar o resultado sorteado. Retorna null sem um texto de leitura aproveitável.
 */
object OracleJsonParser {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private const val MAX_READING = 600
    private const val MAX_TITLE = 60
    private const val MAX_ESSENCE = 40
    private const val MAX_AFFIRMATION = 100

    fun parse(raw: String): OracleReading? {
        val candidate = extractJsonObject(raw) ?: return null
        val root = runCatching { json.parseToJsonElement(candidate).jsonObject }.getOrNull() ?: return null
        val reading = text(root["reading"], MAX_READING) ?: return null
        return OracleReading(
            title = text(root["title"], MAX_TITLE),
            essence = text(root["essence"], MAX_ESSENCE),
            reading = reading,
            affirmation = text(root["affirmation"], MAX_AFFIRMATION)
        )
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
