package com.ogos.apprandomizador.features.hub.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ogos.apprandomizador.core.database.OracleModeStats
import com.ogos.apprandomizador.core.outcome.IOracleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ModesHubViewModel(repository: IOracleRepository) : ViewModel() {
    val stats: StateFlow<Map<String, OracleModeStats>> = repository.stats
        .map { values -> values.associateBy(OracleModeStats::modeId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())
}
