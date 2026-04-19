package com.ogos.apprandomizador.data.repository

import com.ogos.apprandomizador.model.ItemList
import com.ogos.apprandomizador.data.database.DataStoreManager

interface IDefaultInitilization {
    suspend fun initializeDatabase(
        isFirstTime: Boolean,
        dataStoreManager: DataStoreManager,
        repository: IRepository
    )

    fun createDefaultList(): List<ItemList>
}