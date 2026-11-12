package com.ogos.apprandomizador.core.model

import kotlinx.serialization.Serializable

@Serializable
data class RaffleItem(
    val name: String,
    val color: Long
) {
    fun isBlank(): Boolean {
        return name.isBlank() || color == 0L
    }
}

