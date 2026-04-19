package com.ogos.apprandomizador.data.repository

import com.ogos.apprandomizador.model.ItemList

interface IRepository {
    suspend fun saveInDatabase(item: ItemList)
    suspend fun updateInDatabase(item: ItemList)
    suspend fun getItem(id: Long): ItemList
}