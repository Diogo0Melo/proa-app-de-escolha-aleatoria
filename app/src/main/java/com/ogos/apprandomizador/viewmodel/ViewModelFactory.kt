package com.ogos.apprandomizador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ogos.apprandomizador.data.database.DataStoreManager
import com.ogos.apprandomizador.data.repository.IDefaultInitialization
import com.ogos.apprandomizador.data.repository.ItemListRepository

class ViewModelFactory(
    private val repository: ItemListRepository,
    private val dataStoreManager: DataStoreManager,
    private val defaultInitilization: IDefaultInitialization
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) ->
                MainViewModel(repository, dataStoreManager, defaultInitilization) as T
            modelClass.isAssignableFrom(RandomizeViewModel::class.java) ->
                RandomizeViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
