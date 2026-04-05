package com.ogos.apprandomizador.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ogos.apprandomizador.model.ItemList
import com.ogos.apprandomizador.repository.ItemListRepository

@Composable
fun ActiveRandomizerScreen(onBack: () -> Unit, itemIndex: Int) {
    val item = ItemListRepository.items[itemIndex]
    var currentNumber by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = { ActiveRandomizerTopBar(onBack = onBack) },
        bottomBar = { ActiveRandomizerBottomBar(onClick = { currentNumber = (1..item.items.size).random() }) }
    ) { paddingValues ->
        ActiveRandomizerContent(
            response = currentNumber,
            modifier = Modifier.padding(paddingValues),
            item = item
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveRandomizerTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = onBack) {
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
fun ActiveRandomizerContent(response: Int, modifier: Modifier = Modifier, item: ItemList) {

    val response = when {
        response in 1..item.items.size -> item.items[response - 1]
        else -> mapOf("USE RODAR PARA SORTEAR" to MaterialTheme.colorScheme.primary)
    }



    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = modifier
                .padding(16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = response.keys.first(),
                style = TextStyle(
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 42.sp,
                    drawStyle = Stroke(
                        miter = 10f,
                        width = 16f,
                        join = StrokeJoin.Round
                    ),
                    color = Color.White,
                )
            )
            Text(
                text = response.keys.first(),
                style = TextStyle(
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 42.sp,
                    drawStyle = Stroke(
                        miter = 10f,
                        width = 8f,
                        join = StrokeJoin.Round
                    ),
                    color = Color.Black,
                )
            )
            Text(
                text = response.keys.first(),
                lineHeight = 42.sp,
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = response.values.first()
                )
            )
        }
    }
}

@Composable
fun ActiveRandomizerBottomBar(onClick: () -> Unit, modifier: Modifier = Modifier) {
    BottomAppBar(
        modifier = modifier.height(160.dp),
        containerColor = MaterialTheme.colorScheme.surface,

        ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { onClick() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "RODAR")
            }
            Row() {
                OutlinedButton(
                    onClick = { /*TODO*/ },
                    modifier = Modifier.weight(1f),

                    ) {
                    Text(text = "DESATIVAR")
                }
                Spacer(Modifier.padding(4.dp))
                OutlinedButton(
                    onClick = { /*TODO*/ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "RESETAR")
                }
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun ActiveRandomizerScreenPreview() {
    ActiveRandomizerScreen(onBack = {}, itemIndex = -1)
}
