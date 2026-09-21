package com.ogos.apprandomizador.data.repository

import com.ogos.apprandomizador.model.DrawHistoryEntry
import com.ogos.apprandomizador.model.ItemList
import kotlinx.coroutines.flow.Flow

interface IRepository {
    val allItems: Flow<List<ItemList>>
    suspend fun save(item: ItemList)
    suspend fun update(item: ItemList)
    suspend fun delete(item: ItemList)
    suspend fun deleteById(id: Long)
    fun getItemFlow(id: Long): Flow<ItemList?>
    suspend fun recordDraw(listId: Long, itemName: String)
    fun getDrawHistory(itemListId: Long): Flow<List<DrawHistoryEntry>>
}