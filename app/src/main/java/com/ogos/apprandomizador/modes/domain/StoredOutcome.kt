package com.ogos.apprandomizador.modes.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface StoredOutcome {
    @Serializable @SerialName("yes_no")
    data class YesNo(val answer: String) : StoredOutcome

    @Serializable @SerialName("tarot")
    data class Tarot(val spread: String, val cards: List<StoredTarotCard>) : StoredOutcome
}

@Serializable
data class StoredTarotCard(val cardId: String, val reversed: Boolean)
