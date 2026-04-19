package com.ogos.apprandomizador

import android.app.Application
import com.ogos.apprandomizador.data.database.AppDatabase
import com.ogos.apprandomizador.data.repository.ItemListRepository

class RandomApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { ItemListRepository(database.itemDao()) }
}