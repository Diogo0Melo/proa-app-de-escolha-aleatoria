package com.ogos.apprandomizador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ogos.apprandomizador.database.DataStoreManager
import com.ogos.apprandomizador.repository.ItemListRepository

class ViewModelFactory(
    private val repository: ItemListRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) ->
                MainViewModel(repository, dataStoreManager) as T
            modelClass.isAssignableFrom(ChoiceViewModel::class.java) ->
                ChoiceViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
