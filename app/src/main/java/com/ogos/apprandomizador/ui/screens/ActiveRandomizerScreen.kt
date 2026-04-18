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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ogos.apprandomizador.database.ItemDao
import com.ogos.apprandomizador.model.ItemList
import com.ogos.apprandomizador.repository.ItemListRepository
import com.ogos.apprandomizador.viewmodel.ChoiceViewModel
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDateTime

@Composable
fun ActiveRandomizerScreen(
    onBack: () -> Unit,
    itemIndex: Int,
    viewModel: ChoiceViewModel = viewModel(),
) {
    val item by viewModel.currentItem.collectAsState()
    val currentNumber by viewModel.randomNumber.collectAsState()
    LaunchedEffect(item) {
        viewModel.setCurrentItem(itemIndex)
    }
    Scaffold(
        topBar = { ActiveRandomizerTopBar(onBack = onBack) },
        bottomBar = {
            ActiveRandomizerBottomBar(item = item, viewModel = viewModel)
        }
    ) { paddingValues ->
        ActiveRandomizerContent(
            modifier = Modifier.padding(paddingValues),
            item = item,
            currentNumber = currentNumber
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveRandomizerTopBar(onBack: () -> Unit) {

    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(
                onClick =
                    {
                        onBack()
                    }
            ) {
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
fun ActiveRandomizerContent(
    modifier: Modifier = Modifier,
    item: ItemList,
    currentNumber: Int
) {

    val response = when {
        currentNumber in 0..item.items.size -> {
            val response = item.items[currentNumber]
            item.uses++
            item.updateHistory(response.keys.first(), LocalDateTime.now())
            response
        }

        else -> mapOf("USE RODAR PARA SORTEAR" to MaterialTheme.colorScheme.primary.toArgb().toLong())
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
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp,
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
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp,
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
                lineHeight = 40.sp,
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(response.values.first())
                )
            )
        }
    }
}

@Composable
fun ActiveRandomizerBottomBar(
    modifier: Modifier = Modifier,
    viewModel: ChoiceViewModel,
    item: ItemList
) {
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
                onClick = {
                    viewModel.generateRandomNumber(item.items.size)
                    viewModel.updateItem(item)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "RODAR")
            }
            Row {
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
    val fakeDao = object : ItemDao {
        override fun insertItem(item: ItemList) {}
        override fun updateItem(item: ItemList) {}
        override fun readAllItems() = flowOf(emptyList<ItemList>())
    }
    ItemListRepository(fakeDao).apply {
       createDefaultList()
    }
    ActiveRandomizerScreen(
        onBack = {},
        itemIndex = 0,
    )
}
