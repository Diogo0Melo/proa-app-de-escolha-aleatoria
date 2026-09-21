package com.ogos.apprandomizador.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ogos.apprandomizador.model.ItemList
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface ItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemList)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateItem(item: ItemList)

    @Delete
    suspend fun deleteItem(item: ItemList)

    @Query("DELETE FROM item_list WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("SELECT * FROM item_list")
    fun readAllItems(): Flow<List<ItemList>>


    @Query("SELECT * FROM item_list WHERE id = :id")
    fun getItemFlow(id: Long): Flow<ItemList?>

    @Query("UPDATE item_list SET uses = uses + 1, lastUsedAt = :now WHERE id = :id")
    suspend fun incrementUses(id: Long, now: Instant)
}
