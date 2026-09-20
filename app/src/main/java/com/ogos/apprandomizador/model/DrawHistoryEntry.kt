package com.ogos.apprandomizador.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "draw_history")
data class DrawHistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemListId: Long,
    val resultName: String,
    val drawnAt: Instant = Instant.now()
)
