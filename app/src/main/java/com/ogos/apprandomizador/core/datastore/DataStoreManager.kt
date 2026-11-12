package com.ogos.apprandomizador.core.datastore

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

    /** Marca o conteúdo inicial como semeado. Depois disso a inicialização não roda de novo. */
    suspend fun markSeeded() {
        dataStore.edit {
            it[IS_FIRST_TIME] = false
        }
    }

    /** true enquanto o conteúdo inicial não foi semeado. A chave gravada mantém o nome antigo para não re-semear quem já usa o app. */
    val needsSeed: Flow<Boolean> = dataStore.data.map {
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