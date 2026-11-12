package com.ogos.apprandomizador.core.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "draw_history",
    foreignKeys = [
        ForeignKey(
            entity = ItemList::class,
            parentColumns = ["id"],
            childColumns = ["itemListId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("itemListId")]
)
data class DrawHistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemListId: Long,
    val resultName: String,
    val drawnAt: Instant = Instant.now()
)
