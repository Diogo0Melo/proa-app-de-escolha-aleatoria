package com.ogos.apprandomizador.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "item_list")
data class ItemList(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var topic: String = "",
    var items: MutableList<Map<String, Color>> = mutableListOf(),
    var uses: Int = 0,
    var lastUse: String = "Nunca",
    var createdAt: String = "",
    var history: MutableList<String>? = null
)
