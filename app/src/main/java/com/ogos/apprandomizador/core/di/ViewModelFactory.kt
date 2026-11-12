package com.ogos.apprandomizador.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ogos.apprandomizador.core.ai.domain.AiSettingsRepository
import com.ogos.apprandomizador.core.ai.domain.LlmClient
import com.ogos.apprandomizador.core.data.IDefaultInitialization
import com.ogos.apprandomizador.core.data.IRepository
import com.ogos.apprandomizador.core.datastore.DataStoreManager
import com.ogos.apprandomizador.core.entropy.EntropySource
import com.ogos.apprandomizador.core.outcome.IOracleRepository
import com.ogos.apprandomizador.core.ui.MainViewModel
import com.ogos.apprandomizador.fingers.ui.FingersViewModel
import com.ogos.apprandomizador.features.tarot.data.FullTarotDeck
import com.ogos.apprandomizador.features.tarot.domain.AiReadingService
import com.ogos.apprandomizador.features.tarot.domain.DeckProvider
import com.ogos.apprandomizador.features.tarot.domain.OfflineReadingInterpreter
import com.ogos.apprandomizador.features.oracle.domain.OracleAiService
import com.ogos.apprandomizador.features.tarot.domain.ReadingInterpreter
import com.ogos.apprandomizador.modes.ui.ai.AiSettingsViewModel
import com.ogos.apprandomizador.modes.ui.hub.ModesHubViewModel
import com.ogos.apprandomizador.features.oracle.ui.OracleViewModel
import com.ogos.apprandomizador.features.tarot.ui.TarotViewModel
import com.ogos.apprandomizador.features.randomize.ui.RandomizeViewModel

class ViewModelFactory(
    private val repository: IRepository,
    private val dataStoreManager: DataStoreManager,
    private val defaultInitialization: IDefaultInitialization,
    private val entropySource: EntropySource,
    private val deckProvider: DeckProvider = FullTarotDeck,
    private val readingInterpreter: ReadingInterpreter = OfflineReadingInterpreter(),
    private val oracleRepository: IOracleRepository,
    private val aiSettingsRepository: com.ogos.apprandomizador.core.ai.domain.AiSettingsRepository,
    private val aiReadingService: com.ogos.apprandomizador.features.tarot.domain.AiReadingService,
    private val aiClient: com.ogos.apprandomizador.core.ai.domain.LlmClient,
    private val oracleAiService: OracleAiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) ->
                MainViewModel(repository, dataStoreManager, defaultInitialization) as T
            modelClass.isAssignableFrom(RandomizeViewModel::class.java) ->
                RandomizeViewModel(repository, entropySource) as T
            modelClass.isAssignableFrom(FingersViewModel::class.java) ->
                FingersViewModel(
                    entropySource = entropySource,
                    oracleRepository = oracleRepository
                ) as T
            modelClass.isAssignableFrom(ModesHubViewModel::class.java) ->
                ModesHubViewModel(oracleRepository) as T
            modelClass.isAssignableFrom(OracleViewModel::class.java) ->
                OracleViewModel(
                    entropySource = entropySource,
                    rouletteSource = repository,
                    oracleRepository = oracleRepository,
                    aiSettingsRepository = aiSettingsRepository,
                    aiService = oracleAiService
                ) as T
            modelClass.isAssignableFrom(TarotViewModel::class.java) ->
                TarotViewModel(entropySource, deckProvider, readingInterpreter, oracleRepository = oracleRepository, aiReadingService = aiReadingService, aiSettingsRepository = aiSettingsRepository) as T
            modelClass.isAssignableFrom(com.ogos.apprandomizador.modes.ui.ai.AiSettingsViewModel::class.java) ->
                com.ogos.apprandomizador.modes.ui.ai.AiSettingsViewModel(aiSettingsRepository, aiClient) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
