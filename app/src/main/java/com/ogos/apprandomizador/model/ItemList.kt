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
    var lastUsedAt: Instant? = null,
    val createdAt: Instant = Instant.now(),
) {
    fun recordDraw(drawItem: RaffleItem): ItemList {
        return copy(
            uses = uses + 1,
            lastUsedAt = Instant.now()
        )
    }
}
