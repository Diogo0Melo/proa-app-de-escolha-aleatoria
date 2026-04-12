package com.ogos.apprandomizador.repository

import androidx.compose.runtime.mutableStateListOf
import com.ogos.apprandomizador.database.ItemDao
import com.ogos.apprandomizador.model.ItemList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ItemListRepository(private val itemDao: ItemDao) {

    val items = mutableStateListOf<ItemList>()

    suspend fun saveInDatabase() {
        val copiaDaLista = items.toList()
        withContext(Dispatchers.IO) {
            copiaDaLista.forEach { item ->
                itemDao.insertOrUpdateItem(item)
            }
        }
    }

    suspend fun readFromDatabase() {
        val listaDoBanco = withContext(Dispatchers.IO) {
            itemDao.readAllItems().first()
        }
        items.clear()
        items.addAll(listaDoBanco)
    }

    fun getAllItems(): Flow<List<ItemList>> = itemDao.readAllItems()

    fun createDefault() {
        items.add(
            ItemList(
                topic = "Tema A",
                items = mutableListOf(
                    mapOf("DEFINITIVAMENTE SIM" to 0xFF00FF00),
                    mapOf("SIM" to 0xFF009600),
                    mapOf("TALVEZ SIM" to 0xFF005500),
                    mapOf("TALVEZ NÃO" to 0xFF550000),
                    mapOf("NÃO" to 0xFF960000),
                    mapOf("ABSOLUTAMENTE NÃO" to 0xFFFF0000),
                ),
                uses = 0,
            ),
        )
        items.add(
            ItemList(
                topic = "Tema B",
                items = mutableListOf(
                    mapOf("LIXO" to 0xFF9E9E9E),
                    mapOf("COMUM" to 0xFFBDBDBD),
                    mapOf("BÁSICO" to 0xFFF5F5F5),
                    mapOf("INCOMUM" to 0xFF4CAF50),
                    mapOf("REFINADO" to 0xFF81C784),
                    mapOf("RARO" to 0xFF2196F3),
                    mapOf("SUPER RARO" to 0xFF64B5F6),
                    mapOf("PRECIOSO" to 0xFF00BCD4),
                    mapOf("ÉPICO" to 0xFF9C27B0),
                    mapOf("MÍSTICO" to 0xFFBA68C8),
                    mapOf("LENDÁRIO" to 0xFFFF9800),
                    mapOf("ANCESTRAL" to 0xFFFB8C00),
                    mapOf("DIVINO" to 0xFFFFD700),
                    mapOf("IMORTAL" to 0xFFE91E63),
                    mapOf("AMALDIÇOADO" to 0xFF311B92),
                    mapOf("CORROMPIDO" to 0xFF212121),
                    mapOf("CELESTIAL" to 0xFFB2EBF2),
                    mapOf("VÉRTICE" to 0xFF00E5FF),
                    mapOf("ARCANO" to 0xFF6200EA),
                    mapOf("PROIBIDO" to 0xFFD50000)
                ),
                uses = 0,
            )
        )
    }
}