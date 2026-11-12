package com.ogos.apprandomizador.core.outcome

import com.ogos.apprandomizador.core.database.OracleModeStats
import com.ogos.apprandomizador.core.outcome.StoredOutcome
import kotlinx.coroutines.flow.Flow

interface IOracleRepository {
    val stats: Flow<List<OracleModeStats>>
    suspend fun record(modeId: String, outcome: StoredOutcome)
}
