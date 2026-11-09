package com.ogos.apprandomizador.ui.sound

import com.ogos.apprandomizador.modes.domain.Reading

/** Limite seguro por chamada do TextToSpeech (o limite do Android é 4000 caracteres). */
const val MAX_SPEECH_CHUNK = 3_000

/**
 * Divide [text] em partes de no máximo [maxLength] caracteres, preferindo quebrar em espaços.
 * Função pura, sem dependência do Android.
 */
fun splitForSpeech(text: String, maxLength: Int = MAX_SPEECH_CHUNK): List<String> {
    require(maxLength > 0) { "maxLength deve ser positivo" }
    val chunks = mutableListOf<String>()
    var rest = text.trim()
    while (rest.length > maxLength) {
        val cut = rest.lastIndexOf(' ', maxLength).takeIf { it > 0 } ?: maxLength
        chunks += rest.substring(0, cut).trim()
        rest = rest.substring(cut).trim()
    }
    if (rest.isNotEmpty()) chunks += rest
    return chunks.filter(String::isNotEmpty)
}

/** Texto narrado para uma leitura, na mesma ordem em que a tela a exibe. */
fun Reading.toSpeechText(): String = buildList {
    title?.let { add(it) }
    opening?.let { add(it) }
    add(summary)
    sections.forEach { add("${it.title}. ${it.text}") }
    guidance?.let { add(it) }
    synthesis?.let { add(it) }
}.joinToString(" ")
