package com.ogos.apprandomizador.modes.domain

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class StoredOutcomeTest {
    @Test fun `round trip salva somente spread id da carta e orientacao`() {
        val original: StoredOutcome = StoredOutcome.Tarot("PAST_PRESENT_FUTURE", listOf(StoredTarotCard("tower", true)))
        val encoded = Json.encodeToString(original)
        assertEquals(original, Json.decodeFromString<StoredOutcome>(encoded))
        assertFalse(encoded.contains("question"))
        assertFalse(encoded.contains("essence"))
        assertFalse(encoded.contains("reading"))
    }

    @Test fun `sorteio dos dedos salva so indice, quantidade e sentido`() {
        val original: StoredOutcome = StoredOutcome.Fingers(fingerIndex = 2, direction = "CLOCKWISE", fingerCount = 4)
        val encoded = Json.encodeToString(original)
        assertEquals(original, Json.decodeFromString<StoredOutcome>(encoded))
        assertTrue(encoded.contains("fingers"))
        assertFalse(encoded.contains("position"))
        assertFalse(encoded.contains("color"))
    }

    @Test fun `resposta sim nao usa apenas o identificador da resposta`() {
        val encoded = Json.encodeToString<StoredOutcome>(StoredOutcome.YesNo("YES"))
        assertTrue(encoded.contains("YES"))
        assertFalse(encoded.contains("question"))
    }
}
