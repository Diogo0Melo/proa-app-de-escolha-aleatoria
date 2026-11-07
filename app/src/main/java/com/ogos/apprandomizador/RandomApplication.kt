package com.ogos.apprandomizador

import android.app.Application
import com.ogos.apprandomizador.data.database.AppDatabase
import com.ogos.apprandomizador.data.database.DataStoreManager
import com.ogos.apprandomizador.data.repository.ItemListRepository
import com.ogos.apprandomizador.viewmodel.EntropySource
import com.ogos.apprandomizador.viewmodel.SecureEntropySource

class RandomApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        ItemListRepository(
            database,
            database.itemDao(),
            database.drawHistoryDao()
        )
    }
    val dataStoreManager by lazy { DataStoreManager(this) }
    val entropySource: EntropySource by lazy { SecureEntropySource() }
}