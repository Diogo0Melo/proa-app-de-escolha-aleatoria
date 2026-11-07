package com.ogos.apprandomizador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ogos.apprandomizador.data.database.DataStoreManager
import com.ogos.apprandomizador.data.repository.IDefaultInitialization
import com.ogos.apprandomizador.data.repository.IRepository
import com.ogos.apprandomizador.modes.ui.yesno.YesNoViewModel

class ViewModelFactory(
    private val repository: IRepository,
    private val dataStoreManager: DataStoreManager,
    private val defaultInitialization: IDefaultInitialization,
    private val entropySource: EntropySource
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) ->
                MainViewModel(repository, dataStoreManager, defaultInitialization) as T
            modelClass.isAssignableFrom(RandomizeViewModel::class.java) ->
                RandomizeViewModel(repository, entropySource) as T
            modelClass.isAssignableFrom(YesNoViewModel::class.java) ->
                YesNoViewModel(entropySource) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
