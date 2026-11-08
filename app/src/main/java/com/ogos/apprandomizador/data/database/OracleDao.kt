package com.ogos.apprandomizador.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OracleDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(reading: OracleReadingEntity)

    @Query("SELECT modeId, COUNT(*) AS uses, MAX(drawnAt) AS lastUsedAt FROM oracle_readings GROUP BY modeId")
    fun observeStats(): Flow<List<OracleModeStats>>
}
