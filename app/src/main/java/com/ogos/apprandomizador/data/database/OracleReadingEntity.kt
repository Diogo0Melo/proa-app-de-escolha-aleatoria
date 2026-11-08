package com.ogos.apprandomizador.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "oracle_readings", indices = [Index(value = ["modeId", "drawnAt"])])
data class OracleReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val modeId: String,
    val outcomeJson: String,
    val drawnAt: Instant = Instant.now()
)

data class OracleModeStats(val modeId: String, val uses: Int, val lastUsedAt: Instant?)
