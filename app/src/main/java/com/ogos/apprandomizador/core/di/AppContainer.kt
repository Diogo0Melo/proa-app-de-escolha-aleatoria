package com.ogos.apprandomizador.core.di

import android.content.Context
import com.ogos.apprandomizador.core.ai.data.AiSettingsDataStore
import com.ogos.apprandomizador.core.ai.data.AiSettingsRepositoryImpl
import com.ogos.apprandomizador.core.ai.data.KeystoreSecretStore
import com.ogos.apprandomizador.core.ai.data.OpenAiCompatibleClient
import com.ogos.apprandomizador.core.database.AppDatabase
import com.ogos.apprandomizador.core.datastore.DataStoreManager
import com.ogos.apprandomizador.core.entropy.EntropySource
import com.ogos.apprandomizador.core.entropy.SecureEntropySource
import com.ogos.apprandomizador.core.outcome.OracleRepository
import com.ogos.apprandomizador.features.randomize.data.ItemListRepository
import com.ogos.apprandomizador.modes.domain.AiReadingService
import com.ogos.apprandomizador.modes.domain.OracleAiService

/** Grafo de dependências da aplicação (composition root dos serviços). */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database by lazy { AppDatabase.getDatabase(appContext) }
    val repository by lazy {
        ItemListRepository(
            database,
            database.itemDao(),
            database.drawHistoryDao()
        )
    }
    val dataStoreManager by lazy { DataStoreManager(appContext) }
    val oracleRepository by lazy { OracleRepository(database.oracleDao()) }
    val aiSettingsRepository by lazy {
        AiSettingsRepositoryImpl(AiSettingsDataStore(appContext), KeystoreSecretStore())
    }
    val llmClient by lazy { OpenAiCompatibleClient() }
    val aiReadingService by lazy { AiReadingService(llmClient, aiSettingsRepository) }
    val oracleAiService by lazy { OracleAiService(llmClient, aiSettingsRepository) }
    val entropySource: EntropySource by lazy { SecureEntropySource() }
}
