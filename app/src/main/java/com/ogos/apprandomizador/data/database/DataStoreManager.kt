package com.ogos.apprandomizador.data.database

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val IS_FIRST_TIME = booleanPreferencesKey("is_first_time")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val IS_SOUND_ON = booleanPreferencesKey("is_sound_on")
    }

    suspend fun saveFirstAccess(isFirstTime: Boolean) {
        dataStore.edit {
            it[IS_FIRST_TIME] = isFirstTime
        }
    }

    val isFirstTime: Flow<Boolean> = dataStore.data.map {
        it[IS_FIRST_TIME] ?: true
    }

    val themeMode: Flow<String> = dataStore.data.map {
        it[THEME_MODE] ?: "system"
    }

    suspend fun saveThemeMode(mode: String) {
        dataStore.edit {
            it[THEME_MODE] = mode
        }
    }

    val isSoundOn: Flow<Boolean> = dataStore.data.map {
        it[IS_SOUND_ON] ?: true
    }

    suspend fun saveSoundOn(enabled: Boolean) {
        dataStore.edit {
            it[IS_SOUND_ON] = enabled
        }
    }

}