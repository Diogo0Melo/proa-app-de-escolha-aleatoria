package com.ogos.apprandomizador.core.data

import com.ogos.apprandomizador.core.model.DrawHistoryEntry
import com.ogos.apprandomizador.core.model.ItemList
import kotlinx.coroutines.flow.Flow

interface IRepository : IRouletteSource {
    override val allItems: Flow<List<ItemList>>
    suspend fun save(item: ItemList)
    suspend fun update(item: ItemList)
    suspend fun delete(item: ItemList)
    suspend fun deleteById(id: Long)
    override fun getItemFlow(id: Long): Flow<ItemList?>
    suspend fun recordDraw(listId: Long, itemName: String)
    fun getDrawHistory(itemListId: Long): Flow<List<DrawHistoryEntry>>
}