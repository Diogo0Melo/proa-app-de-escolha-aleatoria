package com.ogos.apprandomizador.model

data class RaffleItem(
    val name: String,
    val color: Long
) {
    fun isBlank(): Boolean {
        return name.isBlank() || color == 0L
    }
}

