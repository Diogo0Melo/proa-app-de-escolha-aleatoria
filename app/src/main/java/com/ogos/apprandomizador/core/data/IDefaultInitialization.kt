package com.ogos.apprandomizador.core.data

import com.ogos.apprandomizador.core.datastore.DataStoreManager
import com.ogos.apprandomizador.core.model.ItemList

interface IDefaultInitialization {
    suspend fun initializeDatabase(
        needsSeed: Boolean,
        dataStoreManager: DataStoreManager,
        repository: IRepository
    )

    fun createDefaultList(): List<ItemList>
}