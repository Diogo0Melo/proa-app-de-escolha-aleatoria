package com.ogos.apprandomizador.data.repository

import com.ogos.apprandomizador.data.database.OracleDao
import com.ogos.apprandomizador.data.database.OracleReadingEntity
import com.ogos.apprandomizador.modes.domain.StoredOutcome
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class OracleRepository(private val dao: OracleDao) : IOracleRepository {
    private val json = Json { classDiscriminator = "type" }
    override val stats = dao.observeStats()

    override suspend fun record(modeId: String, outcome: StoredOutcome) {
        require(modeId.isNotBlank())
        dao.insert(OracleReadingEntity(modeId = modeId, outcomeJson = json.encodeToString(outcome)))
    }
}
