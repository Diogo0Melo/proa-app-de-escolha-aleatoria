package com.ogos.apprandomizador.modes.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface StoredOutcome {
    /** Formato antigo do Sim ou Não. Mantido para decodificar o histórico já salvo. */
    @Serializable @SerialName("yes_no")
    data class YesNo(val answer: String) : StoredOutcome

    /** Resposta do oráculo. [sourceTopic] é nulo no oráculo clássico. A pergunta nunca é salva. */
    @Serializable @SerialName("oracle")
    data class Oracle(val answer: String, val sourceTopic: String? = null) : StoredOutcome

    @Serializable @SerialName("tarot")
    data class Tarot(val spread: String, val cards: List<StoredTarotCard>) : StoredOutcome

    /**
     * Sorteio por dedos. [fingerIndex] é a posição do vencedor na lista de dedos no
     * momento da revelação (0-based). [direction] é o `name` de FingersDirection.
     * Nenhuma coordenada é salva: só o necessário para estatística.
     */
    @Serializable @SerialName("fingers")
    data class Fingers(
        val fingerIndex: Int,
        val direction: String,
        val fingerCount: Int
    ) : StoredOutcome
}

@Serializable
data class StoredTarotCard(val cardId: String, val reversed: Boolean)
