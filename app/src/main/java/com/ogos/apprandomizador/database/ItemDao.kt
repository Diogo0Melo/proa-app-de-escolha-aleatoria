package com.ogos.apprandomizador.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ogos.apprandomizador.model.ItemList
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateItem(item: ItemList)

    @Query("SELECT * FROM item_list")
    fun readAllItems(): Flow<List<ItemList>>

}