package com.ogos.apprandomizador.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.ogos.apprandomizador.ui.view.component.ListCollection
import com.ogos.apprandomizador.viewmodel.RandomizeViewModel

@Composable
fun HomeRandomizeViewRoute(
    onNavigateToActive: (index: Long) -> Unit,
    viewModel: RandomizeViewModel,
    modifier: Modifier = Modifier
) {
    val allItemsList = viewModel.getAllItems().collectAsState(initial = emptyList()).value
    ListCollection(
        onNavigateToActive = onNavigateToActive,
        modifier = modifier,
        allItemsList = allItemsList
    )
}

/*
@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun HomeRandomizeViewMainPreview() {
    HomeRandomizeViewMain(
        onNavigateToActive = {},
        allItemsList = listOf(
            ItemList(
                topic = "O que jantar hoje?",
                items = listOf(
                    mapOf("Pizza" to 0xFFE91E63), // Rosa
                    mapOf("Sushi" to 0xFF2196F3), // Azul
                    mapOf("Hambúrguer" to 0xFF4CAF50), // Verde
                    mapOf("Salada" to 0xFFFFEB3B), // Amarelo
                    mapOf("Tacos" to 0xFFFF9800)  // Laranja
                ),
                uses = 12,
                resultHistory = listOf("Pizza", "Salada", "Pizza")
            ),
            ItemList(
                topic = "Foco de Estudos Mensal",
                items = listOf(
                    mapOf("Jetpack Compose Avançado" to 0xFF673AB7),
                    mapOf("Kotlin Multiplatform (KMP)" to 0xFF00BCD4),
                    mapOf("Arquitetura de Micro-serviços" to 0xFF795548)
                ),
                uses = 5,
                resultHistory = listOf("Jetpack Compose Avançado")
            ),
            ItemList(
                topic = "Cara ou Coroa",
                items = listOf(
                    mapOf("CARA" to 0xFFFFC107),
                    mapOf("COROA" to 0xFF9E9E9E),
                ),
                uses = 150,
                resultHistory = listOf("CARA", "CARA", "COROA", "CARA")
            )
        ),
        topBarState = TopBarState()
    )
}
 */
