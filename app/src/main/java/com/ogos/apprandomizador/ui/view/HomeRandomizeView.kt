package com.ogos.apprandomizador.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ogos.apprandomizador.ui.view.component.ListCollection
import com.ogos.apprandomizador.viewmodel.RandomizeViewModel

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
