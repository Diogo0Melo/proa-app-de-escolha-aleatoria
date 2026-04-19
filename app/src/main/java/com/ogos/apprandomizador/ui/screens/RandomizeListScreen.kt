package com.ogos.apprandomizador.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Stream
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ogos.apprandomizador.R
import com.ogos.apprandomizador.database.ItemDao
import com.ogos.apprandomizador.model.ItemList
import com.ogos.apprandomizador.repository.ItemListRepository
import com.ogos.apprandomizador.viewmodel.ChoiceViewModel
import kotlinx.coroutines.flow.flowOf

@Composable
fun PresetSelectionScreen(
    onNavigateToActive: (index: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChoiceViewModel,
) {
    Scaffold(
        topBar = { RandomizeTopBar() },
        bottomBar = { RandomizeBottomBar() },
        modifier = modifier.fillMaxSize(),
        content = { padding ->
            PresetCollectionContent(
                onNavigateToActive = onNavigateToActive,
                modifier = Modifier.padding(padding),
                viewModel = viewModel,
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomizeTopBar(modifier: Modifier = Modifier) {
    TopAppBar(
        title = {
            Text(text = "Sortear 1 item de uma lista")
        },
        modifier = modifier,
        actions = {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Adicionar novo item",
                modifier = Modifier.padding(end = 16.dp)
            )
        }
    )
}

@Composable
fun RandomizeBottomBar(modifier: Modifier = Modifier) {
    BottomAppBar(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    painter = painterResource(R.drawable.dice_3_svgrepo_com),
                    contentDescription = "Adicionar novo item",
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "Sortear",
                    fontSize = 22.sp,
                )
            }
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Filled.Shuffle,
                    contentDescription = "Adicionar novo item",
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "Embaralhar",
                    fontSize = 22.sp,
                )
            }
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Filled.Fingerprint,
                    contentDescription = "Adicionar novo item",
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "Dedos",
                    fontSize = 22.sp,
                )
            }
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Filled.Stream,
                    contentDescription = "Adicionar novo item",
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "Modos",
                    fontSize = 22.sp,
                )
            }
        }
    }
}

@Composable
fun PresetCollectionContent(
    onNavigateToActive: (index: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChoiceViewModel
) {
    val itemList = viewModel.getAllItems().collectAsState(initial = emptyList()).value

    LazyColumn(
        modifier = modifier
    ) {
        items(count = itemList.size) { index ->
            val item = itemList[index]
            val scrollState = rememberScrollState()
            Card(
                onClick = { onNavigateToActive(item.id) },
                modifier = Modifier
                    .padding(12.dp, 8.dp)
                    .height(136.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            val times = if (item.items.size >= 7) item.items.size else 7
                            repeat(
                                times = times,
                            ) { index ->
                                var index = index
                                if (item.items.size < 7 && index >= item.items.size) {
                                    index -= item.items.size
                                }

                                val item = item.items[index]
                                Box(
                                    modifier = Modifier.width(108.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(16.dp),
                                        color = Color(item.values.first())
                                    ) {
                                        Text(
                                            text = item.keys.first(),
                                            modifier = Modifier.wrapContentHeight(Alignment.CenterVertically),
                                            style = TextStyle(
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black,
                                                textAlign = TextAlign.Center,
                                                drawStyle = Stroke(
                                                    miter = 10f,
                                                    width = 8f,
                                                    join = StrokeJoin.Round
                                                ),
                                                color = Color.Black
                                            )
                                        )
                                        Text(
                                            text = item.keys.first(),
                                            modifier = Modifier.wrapContentHeight(Alignment.CenterVertically),
                                            style = TextStyle(
                                                textAlign = TextAlign.Center,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        Column(
                            modifier = Modifier.padding(4.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = item.topic,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = "Items: ${item.items.size}")
                            Text(text = "Usos: ${item.uses}")
                            Text(text = "Ultimo uso: ${item.lastUse}")
                            Text(text = "Criado Em: ${item.createdAt}")
                        }
                    }
                    Box() {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Editar esse item",
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}


@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun PresetSelectionScreenPreview() {
    val fakeDao = object : ItemDao {
        override suspend fun insertItem(item: ItemList) {}
        override suspend fun updateItem(item: ItemList) {}
        override fun readAllItems() = flowOf(emptyList<ItemList>())
        override suspend fun getItem(id: Long): ItemList {
            return ItemList()
        }
    }
    ItemListRepository(fakeDao).apply {
        createDefaultList()
    }
    PresetSelectionScreen(
        onNavigateToActive = {},
        viewModel = TODO()
    )
}
