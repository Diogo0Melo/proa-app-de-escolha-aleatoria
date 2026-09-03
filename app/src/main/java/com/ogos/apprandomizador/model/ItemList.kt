package com.ogos.apprandomizador.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDateTime

@Entity(tableName = "item_list")
data class ItemList(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var topic: String = "",
    var items: List<RaffleItem> = listOf(),
    var uses: Int = 0,
    var resultHistory: List<String> = listOf(),
    var dateTimeHistory: List<Instant> = listOf(),
    val createdAtNotFormatted: String = LocalDateTime.now().toString(),
)
