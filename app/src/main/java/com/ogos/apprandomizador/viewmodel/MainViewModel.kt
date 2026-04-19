package com.ogos.apprandomizador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ogos.apprandomizador.model.database.DataStoreManager
import com.ogos.apprandomizador.model.repository.ItemListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: ItemListRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {
    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    init {
        viewModelScope.launch {
            val isFirstTime = dataStoreManager.isFirstTime.first()
            repository.initializeDatabase(isFirstTime, dataStoreManager)
            _isReady.value = true
        }
    }
}
