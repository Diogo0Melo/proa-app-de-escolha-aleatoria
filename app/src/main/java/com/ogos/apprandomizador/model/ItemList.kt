package com.ogos.apprandomizador.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "item_list")
data class ItemList(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var topic: String = "",
    var items: List<RaffleItem> = listOf(),
    var uses: Int = 0,
    var resultHistory: List<String> = listOf(),
    var dateTimeHistory: List<Instant> = listOf(),
    val createdAt: Instant = Instant.now(),
) {
    fun recordDraw(drawItem: RaffleItem): ItemList {
        val resultKey = drawItem.name
        val newHistory = resultHistory + resultKey
        val newTimeHistory = dateTimeHistory + Instant.now()

        return copy(
            uses = uses + 1,
            resultHistory = newHistory,
            dateTimeHistory = newTimeHistory
        )
    }
}
