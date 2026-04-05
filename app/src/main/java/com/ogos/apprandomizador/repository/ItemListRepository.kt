package com.ogos.apprandomizador.repository

import androidx.compose.ui.graphics.Color
import com.ogos.apprandomizador.model.ItemList

object ItemListRepository {
    val items = mutableListOf(
        ItemList(
            topic = "Tema A",
            items = mutableListOf(
                mapOf("DEFINITIVAMENTE SIM" to Color(0xFF00FF00)),
                mapOf("SIM" to Color(0xFF009600)),
                mapOf("TALVEZ SIM" to Color(0xFF005500)),
                mapOf("TALVEZ NÃO" to Color(0xFF550000)),
                mapOf("NÃO" to Color(0xFF960000)),
                mapOf("ABSOLUTAMENTE NÃO" to Color(0xFFFF0000)),
            ),
            uses = 5,
            lastUse = "22/02/2000",
            createdAt = "12/02/2000",
        ),
        ItemList(
            topic = "Tema B",
            items = mutableListOf(
                mapOf("LIXO" to Color(0xFF9E9E9E)),
                mapOf("COMUM" to Color(0xFFBDBDBD)),
                mapOf("BÁSICO" to Color(0xFFF5F5F5)),
                mapOf("INCOMUM" to Color(0xFF4CAF50)),
                mapOf("REFINADO" to Color(0xFF81C784)),
                mapOf("RARO" to Color(0xFF2196F3)),
                mapOf("SUPER RARO" to Color(0xFF64B5F6)),
                mapOf("PRECIOSO" to Color(0xFF00BCD4)),
                mapOf("ÉPICO" to Color(0xFF9C27B0)),
                mapOf("MÍSTICO" to Color(0xFFBA68C8)),
                mapOf("LENDÁRIO" to Color(0xFFFF9800)),
                mapOf("ANCESTRAL" to Color(0xFFFB8C00)),
                mapOf("DIVINO" to Color(0xFFFFD700)),
                mapOf("IMORTAL" to Color(0xFFE91E63)),
                mapOf("AMALDIÇOADO" to Color(0xFF311B92)),
                mapOf("CORROMPIDO" to Color(0xFF212121)),
                mapOf("CELESTIAL" to Color(0xFFB2EBF2)),
                mapOf("VÉRTICE" to Color(0xFF00E5FF)),
                mapOf("ARCANO" to Color(0xFF6200EA)),
                mapOf("PROIBIDO" to Color(0xFFD50000))
            ),
            uses = 5,
            lastUse = "22/02/2000",
            createdAt = "12/02/2000",
        )
    )
}