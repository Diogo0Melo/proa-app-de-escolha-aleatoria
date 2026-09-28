package com.ogos.apprandomizador.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ogos.apprandomizador.ui.view.component.ListCollection
import com.ogos.apprandomizador.viewmodel.RandomizeViewModel

@Composable
fun HomeShuffleViewRoute(
    viewModel: RandomizeViewModel,
    modifier: Modifier = Modifier
) {

    val allItemsList = viewModel.getAllItems().collectAsStateWithLifecycle(initialValue = emptyList()).value

    ListCollection(
        modifier = modifier,
        allItemsList = allItemsList,
        onNavigateToActive = {}
    )
}

