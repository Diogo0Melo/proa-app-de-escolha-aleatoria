package com.ogos.apprandomizador.data.repository

import com.ogos.apprandomizador.model.ItemList
import kotlinx.coroutines.flow.Flow

interface IRepository {
    val allItems: Flow<List<ItemList>>
    suspend fun save(item: ItemList)
    suspend fun update(item: ItemList)
    suspend fun getItem(id: Long): ItemList
}