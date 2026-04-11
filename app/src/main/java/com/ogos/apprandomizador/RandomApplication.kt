package com.ogos.apprandomizador

import android.app.Application
import com.ogos.apprandomizador.database.AppDatabase
import com.ogos.apprandomizador.repository.ItemListRepository

class RandomApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { ItemListRepository(database.itemDao()) }
}