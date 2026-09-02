package com.ogos.apprandomizador.data.repository

import com.ogos.apprandomizador.model.ItemList
import com.ogos.apprandomizador.data.database.DataStoreManager
import com.ogos.apprandomizador.model.RaffleItem

class DefaultInitialize : IDefaultInitialization {

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
            dataStoreManager.saveFirstAccess(false)
        }
    }

    override fun createDefaultList(): List<ItemList> {
        val allItems = mutableListOf<ItemList>()
        allItems.add(
            ItemList(
                topic = "Tema A",
                items = mutableListOf(
                    RaffleItem("DEFINITIVAMENTE SIM", 0xFF00FF00L),
                    RaffleItem("SIM" , 0xFF009600L),
                    RaffleItem("TALVEZ SIM" , 0xFF005500L),
                    RaffleItem("TALVEZ NÃO" , 0xFF550000L),
                    RaffleItem("NÃO" , 0xFF960000L),
                    RaffleItem("ABSOLUTAMENTE NÃO" , 0xFFFF0000L),
                ),
                uses = 0,
            ),
        )
        allItems.add(
            ItemList(
                topic = "Tema B",
                items = mutableListOf(
                    RaffleItem("LIXO" , 0xFF9E9E9EL),
                    RaffleItem("COMUM" , 0xFFBDBDBDL),
                    RaffleItem("BÁSICO" , 0xFFF5F5F5L),
                    RaffleItem("INCOMUM" , 0xFF4CAF50L),
                    RaffleItem("REFINADO" , 0xFF81C784L),
                    RaffleItem("RARO" , 0xFF2196F3L),
                    RaffleItem("SUPER RARO" , 0xFF64B5F6L),
                    RaffleItem("PRECIOSO" , 0xFF00BCD4L),
                    RaffleItem("ÉPICO" , 0xFF9C27B0L),
                    RaffleItem("MÍSTICO" , 0xFFBA68C8L),
                    RaffleItem("LENDÁRIO" , 0xFFFF9800L),
                    RaffleItem("ANCESTRAL" , 0xFFFB8C00L),
                    RaffleItem("DIVINO" , 0xFFFFD700L),
                    RaffleItem("IMORTAL" , 0xFFE91E63L),
                    RaffleItem("AMALDIÇOADO" , 0xFF311B92L),
                    RaffleItem("CORROMPIDO" , 0xFF212121L),
                    RaffleItem("CELESTIAL" , 0xFFB2EBF2L),
                    RaffleItem("VÉRTICE" , 0xFF00E5FFL),
                    RaffleItem("ARCANO" , 0xFF6200EAL),
                    RaffleItem("PROIBIDO" , 0xFFD50000L)
                ),
                uses = 0,
            )
        )
        return allItems
    }
}