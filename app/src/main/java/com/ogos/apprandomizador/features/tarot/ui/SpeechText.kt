package com.ogos.apprandomizador.features.tarot.ui

import com.ogos.apprandomizador.features.tarot.domain.Reading

/** Texto narrado para uma leitura, na mesma ordem em que a tela a exibe. */
fun Reading.toSpeechText(): String = buildList {
    title?.let { add(it) }
    opening?.let { add(it) }
    add(summary)
    sections.forEach { add("${it.title}. ${it.text}") }
    guidance?.let { add(it) }
    synthesis?.let { add(it) }
}.joinToString(" ")
