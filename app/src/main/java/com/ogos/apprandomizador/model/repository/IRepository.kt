package com.ogos.apprandomizador.model.repository

import com.ogos.apprandomizador.model.ItemList

interface IRepository {
    suspend fun saveInDatabase(item: ItemList)
    suspend fun updateInDatabase(item: ItemList)
    suspend fun getItem(id: Long): ItemList
}