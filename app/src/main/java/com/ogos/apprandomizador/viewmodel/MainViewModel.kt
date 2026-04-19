package com.ogos.apprandomizador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ogos.apprandomizador.data.database.DataStoreManager
import com.ogos.apprandomizador.data.repository.IDefaultInitilization
import com.ogos.apprandomizador.data.repository.IRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: IRepository,
    private val dataStoreManager: DataStoreManager,
    defaultInitilization: IDefaultInitilization
) : ViewModel() {
    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    init {
        viewModelScope.launch {
            val isFirstTime = dataStoreManager.isFirstTime.first()
            defaultInitilization.initializeDatabase(isFirstTime, dataStoreManager, repository)
            _isReady.value = true
        }
    }
}
