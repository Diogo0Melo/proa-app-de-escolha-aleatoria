package com.ogos.apprandomizador.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ogos.apprandomizador.R


@Composable
fun PresetSelectionScreen(modifier: Modifier = Modifier) {
    Scaffold(
        topBar = { RandomizeTopBar() },
        bottomBar = { RandomizeBottomBar() },
        modifier = Modifier.fillMaxSize(),
        content = { padding ->
            PresetCollectionContent(modifier = Modifier.padding(padding))
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
fun PresetCollectionContent(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.padding(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row() {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_background),
                    contentDescription = "Itens da coleção",
                )
                Column(
                    modifier = Modifier.padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Tema A Ser Roletado",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "Items: 5")
                    Text(text = "Usos: 5")
                    Text(text = "Ultimo uso: há´22min")
                    Text(text = "Criado Em: 22/02/2000")
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

@Preview(showSystemUi = true)
@Composable
fun PresetSelectionScreenPreview() {
    PresetSelectionScreen()
}