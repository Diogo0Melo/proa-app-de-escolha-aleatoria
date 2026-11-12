package com.ogos.apprandomizador.features.randomize.data

import androidx.room.withTransaction
import com.ogos.apprandomizador.core.data.IRepository
import com.ogos.apprandomizador.core.database.AppDatabase
import com.ogos.apprandomizador.core.database.dao.DrawHistoryDao
import com.ogos.apprandomizador.core.database.dao.ItemDao
import com.ogos.apprandomizador.core.model.DrawHistoryEntry
import com.ogos.apprandomizador.core.model.ItemList
import java.time.Instant
import kotlinx.coroutines.flow.Flow

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
