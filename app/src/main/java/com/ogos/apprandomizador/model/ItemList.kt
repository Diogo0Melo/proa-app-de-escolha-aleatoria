package com.ogos.apprandomizador.model

import androidx.compose.ui.graphics.Color

data class ItemList(
    var topic: String = "",
    var items: MutableList<Map<String, Color>> = mutableListOf(),
    var uses: Int = 0,
    var lastUse: String = "Nunca",
    var createdAt: String = "",
    var history: MutableList<String>? = null
)
