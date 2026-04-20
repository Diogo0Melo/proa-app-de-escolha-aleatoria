package com.ogos.apprandomizador.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.ogos.apprandomizador.ui.view.component.ListCollection
import com.ogos.apprandomizador.viewmodel.RandomizeViewModel

@Composable
fun HomeShuffleViewRoute(
    viewModel: RandomizeViewModel,
    modifier: Modifier = Modifier
) {

    val allItemsList = viewModel.getAllItems().collectAsState(initial = emptyList()).value

    ListCollection(
        modifier = modifier,
        allItemsList = allItemsList,
        onNavigateToActive = {}
    )
}

