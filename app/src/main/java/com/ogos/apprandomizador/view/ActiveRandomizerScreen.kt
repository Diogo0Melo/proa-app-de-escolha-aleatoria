package com.ogos.apprandomizador.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ActiveRandomizerScreen() {
    Scaffold(
        topBar = { ActiveRandomizerTopBar() }
    ) { padding ->
        ActiveRandomizerContent(padding)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveRandomizerTopBar() {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.ArrowBackIosNew,
                    contentDescription = "Voltar"
                )
            }
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = "Estatisticas"
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar"
                )
            }
        }
    )
}

@Composable
fun ActiveRandomizerContent(modifier: Modifier = Modifier) {

}


@Preview(showSystemUi = true)
@Composable
fun ActiveRandomizerScreenPreview() {
    ActiveRandomizerScreen()
}