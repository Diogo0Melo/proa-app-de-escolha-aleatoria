package com.ogos.apprandomizador

import android.app.Application
import com.ogos.apprandomizador.data.database.AppDatabase
import com.ogos.apprandomizador.data.database.DataStoreManager
import com.ogos.apprandomizador.data.repository.ItemListRepository
import com.ogos.apprandomizador.data.repository.OracleRepository
import com.ogos.apprandomizador.modes.data.AiSettingsDataStore
import com.ogos.apprandomizador.modes.data.KeystoreSecretStore
import com.ogos.apprandomizador.modes.data.OpenAiCompatibleClient
import com.ogos.apprandomizador.modes.domain.AiReadingService
import com.ogos.apprandomizador.modes.domain.OracleAiService
import com.ogos.apprandomizador.modes.data.AiSettingsRepositoryImpl
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
    val oracleRepository by lazy { OracleRepository(database.oracleDao()) }
    val aiSettingsRepository by lazy {
        AiSettingsRepositoryImpl(AiSettingsDataStore(this), KeystoreSecretStore())
    }
    val llmClient by lazy { OpenAiCompatibleClient() }
    val aiReadingService by lazy { AiReadingService(llmClient, aiSettingsRepository) }
    val oracleAiService by lazy { OracleAiService(llmClient, aiSettingsRepository) }
    val entropySource: EntropySource by lazy { SecureEntropySource() }
}