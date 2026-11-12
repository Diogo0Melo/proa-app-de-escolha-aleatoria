package com.ogos.apprandomizador.features.oracle.ui

import com.ogos.apprandomizador.features.oracle.domain.OracleReading

/** Texto narrado para a leitura da IA do oráculo, na ordem em que aparece na tela. */
fun OracleReading.toSpeechText(): String =
    listOfNotNull(title, essence?.let { "$it." }, reading, affirmation).joinToString(" ")
