package com.ogos.apprandomizador.data.repository

import androidx.room.withTransaction
import com.ogos.apprandomizador.data.database.AppDatabase
import com.ogos.apprandomizador.data.database.DrawHistoryDao
import com.ogos.apprandomizador.data.database.ItemDao
import com.ogos.apprandomizador.model.DrawHistoryEntry
import com.ogos.apprandomizador.model.ItemList
import kotlinx.coroutines.flow.Flow
import java.time.Instant

class ItemListRepository(
    private val database: AppDatabase,
    private val itemDao: ItemDao,
    private val drawHistoryDao: DrawHistoryDao
) : IRepository {

    override val allItems: Flow<List<ItemList>> = itemDao.readAllItems()

    override suspend fun save(item: ItemList) {
        itemDao.insertItem(item)
    }

    override suspend fun update(item: ItemList) {
        itemDao.updateItem(item)
    }

    override suspend fun delete(item: ItemList) {
        itemDao.deleteItem(item)
    }

    override suspend fun deleteById(id: Long) {
        itemDao.deleteItemById(id)
    }


    override fun getItemFlow(id: Long): Flow<ItemList?> = itemDao.getItemFlow(id)

    override suspend fun recordDraw(listId: Long, itemName: String) {
        database.withTransaction {
            itemDao.incrementUses(listId, Instant.now())
            drawHistoryDao.insert(
                DrawHistoryEntry(itemListId = listId, resultName = itemName)
            )
        }
    }

    override fun getDrawHistory(itemListId: Long): Flow<List<DrawHistoryEntry>> =
        drawHistoryDao.getHistoryForItem(itemListId)
}
