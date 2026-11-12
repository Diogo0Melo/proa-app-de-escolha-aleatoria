package com.ogos.apprandomizador.features.randomize.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ogos.apprandomizador.features.randomize.ui.RandomizeViewModel
import com.ogos.apprandomizador.features.randomize.ui.component.ListCollection

@Composable
fun HomeRandomizeViewRoute(
    onNavigateToActive: (index: Long) -> Unit,
    viewModel: RandomizeViewModel,
    modifier: Modifier = Modifier
) {
    val allItemsList = viewModel.getAllItems().collectAsStateWithLifecycle(initialValue = emptyList()).value
    ListCollection(
        onNavigateToActive = onNavigateToActive,
        modifier = modifier,
        allItemsList = allItemsList
    )
}
