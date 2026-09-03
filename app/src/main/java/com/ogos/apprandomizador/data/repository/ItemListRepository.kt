package com.ogos.apprandomizador.data.repository

import com.ogos.apprandomizador.data.database.ItemDao
import com.ogos.apprandomizador.model.ItemList
import kotlinx.coroutines.flow.Flow

class ItemListRepository(private val itemDao: ItemDao) : IRepository {

    override val allItems: Flow<List<ItemList>> = itemDao.readAllItems()

    override suspend fun save(item: ItemList) {
        itemDao.insertItem(item)
    }

    override suspend fun update(item: ItemList) {
        itemDao.updateItem(item)
    }

    override suspend fun getItem(id: Long): ItemList = itemDao.getItem(id)

}
