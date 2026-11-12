package com.ogos.apprandomizador.features.randomize.data

import com.ogos.apprandomizador.core.data.IDefaultInitialization
import com.ogos.apprandomizador.core.data.IRepository
import com.ogos.apprandomizador.core.datastore.DataStoreManager
import com.ogos.apprandomizador.core.model.ItemList
import com.ogos.apprandomizador.core.model.RaffleItem
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DefaultInitialize : IDefaultInitialization {

    override suspend fun initializeDatabase(
        needsSeed: Boolean,
        dataStoreManager: DataStoreManager,
        repository: IRepository
    ) {
        if (!needsSeed) return
        // Idempotency: never re-seed if lists already exist (e.g. a previous run
        // was killed mid-seeding before the seeded flag was set).
        if (repository.allItems.first().isNotEmpty()) {
            dataStoreManager.markSeeded()
            return
        }
        val defaultItems = createDefaultList()
        // Insert concurrently instead of awaiting each save sequentially.
        // coroutineScope awaits all children before setting the seeded flag,
        // so the seed-then-mark ordering keeps initialization idempotent.
        coroutineScope {
            defaultItems.forEach { item ->
                launch { repository.save(item) }
            }
        }
        dataStoreManager.markSeeded()
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
        allItems.add(createNumberedList("Teste 2 itens", 2))
        allItems.add(createNumberedList("Teste 13 itens", 13))
        allItems.add(createNumberedList("Teste 24 itens", 24))
        allItems.add(createNumberedList("Teste 50 itens", 50))
        allItems.add(createNumberedList("Teste 100 itens", 100))
        return allItems
    }

    private fun createNumberedList(topic: String, count: Int): ItemList {
        val colors = listOf(
            0xFF9D4EDDL, 0xFF4CAF50L, 0xFFD6E024L, 0xFFE5484DL,
            0xFF2BD4C8L, 0xFF8FA3B0L, 0xFFB07A50L, 0xFF2196F3L,
            0xFFFF9800L, 0xFFE91E63L
        )
        return ItemList(
            topic = topic,
            items = (1..count).map { index ->
                RaffleItem("Item $index", colors[(index - 1) % colors.size])
            }.toMutableList(),
            uses = 0
        )
    }
}