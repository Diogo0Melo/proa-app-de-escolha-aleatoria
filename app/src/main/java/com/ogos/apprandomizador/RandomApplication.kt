package com.ogos.apprandomizador

import android.app.Application
import com.ogos.apprandomizador.model.database.AppDatabase
import com.ogos.apprandomizador.model.repository.ItemListRepository

class RandomApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { ItemListRepository(database.itemDao()) }
}