package com.ogos.apprandomizador.model.repository

import com.ogos.apprandomizador.model.ItemList
import com.ogos.apprandomizador.model.database.DataStoreManager

class DefaultInitilize : IDefaultInitilization {

    override suspend fun initializeDatabase(
        isFirstTime: Boolean,
        dataStoreManager: DataStoreManager,
        repository: IRepository
    ) {
        if (isFirstTime) {
            val defaultItems = createDefaultList()
            defaultItems.forEach { item ->
                repository.saveInDatabase(item)
            }
            dataStoreManager.saveFirstAcess(false)
        }
    }

    override fun createDefaultList(): List<ItemList> {
        val allItems = mutableListOf<ItemList>()
        allItems.add(
            ItemList(
                topic = "Tema A",
                items = mutableListOf(
                    mapOf("DEFINITIVAMENTE SIM" to 0xFF00FF00L),
                    mapOf("SIM" to 0xFF009600L),
                    mapOf("TALVEZ SIM" to 0xFF005500L),
                    mapOf("TALVEZ NÃO" to 0xFF550000L),
                    mapOf("NÃO" to 0xFF960000L),
                    mapOf("ABSOLUTAMENTE NÃO" to 0xFFFF0000L),
                ),
                uses = 0,
            ),
        )
        allItems.add(
            ItemList(
                topic = "Tema B",
                items = mutableListOf(
                    mapOf("LIXO" to 0xFF9E9E9EL),
                    mapOf("COMUM" to 0xFFBDBDBDL),
                    mapOf("BÁSICO" to 0xFFF5F5F5L),
                    mapOf("INCOMUM" to 0xFF4CAF50L),
                    mapOf("REFINADO" to 0xFF81C784L),
                    mapOf("RARO" to 0xFF2196F3L),
                    mapOf("SUPER RARO" to 0xFF64B5F6L),
                    mapOf("PRECIOSO" to 0xFF00BCD4L),
                    mapOf("ÉPICO" to 0xFF9C27B0L),
                    mapOf("MÍSTICO" to 0xFFBA68C8L),
                    mapOf("LENDÁRIO" to 0xFFFF9800L),
                    mapOf("ANCESTRAL" to 0xFFFB8C00L),
                    mapOf("DIVINO" to 0xFFFFD700L),
                    mapOf("IMORTAL" to 0xFFE91E63L),
                    mapOf("AMALDIÇOADO" to 0xFF311B92L),
                    mapOf("CORROMPIDO" to 0xFF212121L),
                    mapOf("CELESTIAL" to 0xFFB2EBF2L),
                    mapOf("VÉRTICE" to 0xFF00E5FFL),
                    mapOf("ARCANO" to 0xFF6200EAL),
                    mapOf("PROIBIDO" to 0xFFD50000L)
                ),
                uses = 0,
            )
        )
        return allItems
    }
}