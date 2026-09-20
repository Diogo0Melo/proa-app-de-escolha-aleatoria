package com.ogos.apprandomizador.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ogos.apprandomizador.model.DrawHistoryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface DrawHistoryDao {
    @Insert
    suspend fun insert(entry: DrawHistoryEntry)

    @Query("SELECT * FROM draw_history WHERE itemListId = :itemListId ORDER BY drawnAt DESC")
    fun getHistoryForItem(itemListId: Long): Flow<List<DrawHistoryEntry>>

    @Query("SELECT resultName, COUNT(*) as count FROM draw_history WHERE itemListId = :itemListId GROUP BY resultName ORDER BY count DESC")
    fun getResultFrequency(itemListId: Long): Flow<List<ResultFrequency>>

    @Query("DELETE FROM draw_history WHERE itemListId = :itemListId")
    suspend fun clearHistoryForItem(itemListId: Long)
}

data class ResultFrequency(
    val resultName: String,
    val count: Int
)
