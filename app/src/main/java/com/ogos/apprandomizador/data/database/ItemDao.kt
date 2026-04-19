package com.ogos.apprandomizador.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ogos.apprandomizador.model.ItemList
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemList)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateItem(item: ItemList)

    @Query("SELECT * FROM item_list")
    fun readAllItems(): Flow<List<ItemList>>

    @Query("SELECT * FROM item_list WHERE id = :id")
    suspend fun getItem(id: Long): ItemList
}
