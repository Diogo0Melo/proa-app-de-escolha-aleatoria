package com.ogos.apprandomizador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ogos.apprandomizador.data.database.DataStoreManager
import com.ogos.apprandomizador.data.repository.IDefaultInitialization
import com.ogos.apprandomizador.data.repository.IRepository
import com.ogos.apprandomizador.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: IRepository,
    private val dataStoreManager: DataStoreManager,
    defaultInitilization: IDefaultInitialization
) : ViewModel() {
    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    val themeMode: StateFlow<ThemeMode?> = dataStoreManager.themeMode
        .map<String, ThemeMode?> { ThemeMode.fromKey(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            dataStoreManager.saveThemeMode(mode.key)
        }
    }

    val soundEnabled: StateFlow<Boolean> = dataStoreManager.isSoundOn
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true
        )

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.saveSoundOn(enabled)
        }
    }

    init {
        viewModelScope.launch {
            val isFirstTime = dataStoreManager.isFirstTime.first()
            defaultInitilization.initializeDatabase(isFirstTime, dataStoreManager, repository)
            _isReady.value = true
        }
    }
}
