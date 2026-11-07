package com.ogos.apprandomizador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ogos.apprandomizador.data.database.DataStoreManager
import com.ogos.apprandomizador.data.repository.IDefaultInitialization
import com.ogos.apprandomizador.data.repository.IRepository
import com.ogos.apprandomizador.modes.data.MajorArcanaDeck
import com.ogos.apprandomizador.modes.domain.DeckProvider
import com.ogos.apprandomizador.modes.domain.OfflineReadingInterpreter
import com.ogos.apprandomizador.modes.domain.ReadingInterpreter
import com.ogos.apprandomizador.modes.ui.tarot.TarotViewModel
import com.ogos.apprandomizador.modes.ui.yesno.YesNoViewModel

class ViewModelFactory(
    private val repository: IRepository,
    private val dataStoreManager: DataStoreManager,
    private val defaultInitialization: IDefaultInitialization,
    private val entropySource: EntropySource,
    private val deckProvider: DeckProvider = MajorArcanaDeck,
    private val readingInterpreter: ReadingInterpreter = OfflineReadingInterpreter()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) ->
                MainViewModel(repository, dataStoreManager, defaultInitialization) as T
            modelClass.isAssignableFrom(RandomizeViewModel::class.java) ->
                RandomizeViewModel(repository, entropySource) as T
            modelClass.isAssignableFrom(YesNoViewModel::class.java) ->
                YesNoViewModel(entropySource) as T
            modelClass.isAssignableFrom(TarotViewModel::class.java) ->
                TarotViewModel(entropySource, deckProvider, readingInterpreter) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
