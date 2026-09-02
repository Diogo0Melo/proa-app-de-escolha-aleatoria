package com.ogos.apprandomizador.data.database

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val IS_FIRST_TIME = booleanPreferencesKey("is_first_time")
    }

    suspend fun saveFirstAccess(isFirstTime: Boolean) {
        dataStore.edit {
            it[IS_FIRST_TIME] = isFirstTime
        }
    }

    val isFirstTime: Flow<Boolean> = context.dataStore.data.map {
        it[IS_FIRST_TIME] ?: true
    }

}