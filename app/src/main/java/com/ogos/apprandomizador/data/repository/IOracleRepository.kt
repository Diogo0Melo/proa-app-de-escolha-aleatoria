package com.ogos.apprandomizador.data.repository

import com.ogos.apprandomizador.data.database.OracleModeStats
import com.ogos.apprandomizador.modes.domain.StoredOutcome
import kotlinx.coroutines.flow.Flow

interface IOracleRepository {
    val stats: Flow<List<OracleModeStats>>
    suspend fun record(modeId: String, outcome: StoredOutcome)
}
