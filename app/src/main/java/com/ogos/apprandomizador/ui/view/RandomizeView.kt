package com.ogos.apprandomizador.ui.view

import android.media.MediaPlayer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import com.ogos.apprandomizador.R
import com.ogos.apprandomizador.viewmodel.RandomizeViewModel


@Composable
fun RandomizeViewRoute(
    onBack: () -> Unit,
    itemID: Long,
    viewModel: RandomizeViewModel,
) {
    val context = LocalContext.current
    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.roleta_audio)
    }
    val isSpinning by viewModel.isSpinning.collectAsState()
    val result by viewModel.currentRandomItem.collectAsState()
    val color = MaterialTheme.colorScheme.primary.toArgb().toLong()
    val onPerfomRoll = { viewModel.performRoll() }
    LaunchedEffect(itemID) {
        viewModel.setCurrentItem(itemID)
        viewModel.defaultText(color = color)
    }
    LaunchedEffect(isSpinning) {
        if (isSpinning) {
            mediaPlayer.isLooping = true
            mediaPlayer.start()
        } else {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                mediaPlayer.seekTo(0)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer.release() }
    }
    if (result.isEmpty()) {
        println("Aguardando sorteio...")
    } else {
        RandomizeViewMain(
            result = result,
            onPerfomRoll = onPerfomRoll,
            onBack = onBack,
            isSpinning = isSpinning
        )
    }
}

@Composable
fun RandomizeViewMain(result: Map<String, Long>, onPerfomRoll: () -> Unit, onBack: () -> Unit, isSpinning: Boolean) {
    Scaffold(
        topBar = { RandomizeViewTopBar(onBack = onBack) },
        bottomBar = {
            RandomizeViewBottomBar(onPerfomRoll = onPerfomRoll, isSpinning = isSpinning)
        }
    ) { paddingValues ->
        RandomizeViewContent(
            modifier = Modifier.padding(paddingValues),
            result = result
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomizeViewTopBar(onBack: () -> Unit) {

    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(
                onClick = onBack
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
fun RandomizeViewContent(
    modifier: Modifier = Modifier,
    result: Map<String, Long>
) {
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
                text = result.keys.first(),
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
                text = result.keys.first(),
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
                text = result.keys.first(),
                lineHeight = 40.sp,
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(result.values.first())
                )
            )
        }
    }
}

@Composable
fun RandomizeViewBottomBar(onPerfomRoll: () -> Unit, isSpinning: Boolean) {
    BottomAppBar(
        modifier = Modifier.height(160.dp),
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
                    if (isSpinning) return@Button
                    onPerfomRoll()
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
private fun RandomizeViewPreview() {
    RandomizeViewMain(
        onBack = {},
        result = mapOf("USE RODAR PARA SORTEAR" to 0xFF000000),
        onPerfomRoll = { },
        isSpinning = false
    )
}
