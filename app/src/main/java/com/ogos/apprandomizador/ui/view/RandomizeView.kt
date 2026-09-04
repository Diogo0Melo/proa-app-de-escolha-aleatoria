package com.ogos.apprandomizador.ui.view

import android.media.MediaPlayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ogos.apprandomizador.R
import com.ogos.apprandomizador.model.RaffleItem
import com.ogos.apprandomizador.viewmodel.RandomizeViewModel


@Composable
fun RandomizeViewRoute(
    itemID: Long,
    viewModel: RandomizeViewModel,
    modifier: Modifier
) {
    val context = LocalContext.current
    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.roleta_audio)
    }
    val isSpinning by viewModel.isSpinning.collectAsState()
    val result by viewModel.currentRandomItem.collectAsState()
    val color = MaterialTheme.colorScheme.primary.toArgb().toLong()
    val onPerformRoll = { viewModel.performRoll() }
    LaunchedEffect(itemID) {
        viewModel.setCurrentItem(itemID)
        viewModel.defaultText(defaultString = context.getString(R.string.default_shuffle_text), color = color)
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
    if (result.isBlank()) {
        println(stringResource(R.string.waiting_raffle))
    } else {
        RandomizeViewMain(
            result = result,
            onPerformRoll = onPerformRoll,
            isSpinning = isSpinning,
            modifier = modifier
        )
    }
}

@Composable
fun RandomizeViewMain(
    result: RaffleItem,
    onPerformRoll: () -> Unit,
    isSpinning: Boolean,
    modifier: Modifier
) {
    Scaffold(
        topBar = { },
        bottomBar = {
            RandomizeViewBottomBar(onPerformRoll = onPerformRoll, isSpinning = isSpinning)
        }
    ) { paddingValues ->
        RandomizeViewContent(
            modifier = modifier.padding(paddingValues),
            result = result
        )
    }
}

@Composable
fun RandomizeViewContent(
    modifier: Modifier = Modifier,
    result: RaffleItem
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
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
                text = result.name,
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
                text = result.name,
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
                text = result.name,
                lineHeight = 40.sp,
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(result.color)
                )
            )
        }
    }
}

@Composable
fun RandomizeViewBottomBar(onPerformRoll: () -> Unit, isSpinning: Boolean) {
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
                    onPerformRoll()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.roll))
            }
            Row {
                OutlinedButton(
                    onClick = { /*TODO*/ },
                    modifier = Modifier.weight(1f),

                    ) {
                    Text(text = stringResource(R.string.disable))
                }
                Spacer(Modifier.padding(4.dp))
                OutlinedButton(
                    onClick = { /*TODO*/ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(R.string.reset))
                }
            }
        }
    }
}


@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun RandomizeViewPreview() {
    RandomizeViewMain(
        result = RaffleItem(stringResource(R.string.use_roll_to_draw), 0xFF000000),
        onPerformRoll = { },
        isSpinning = false,
        modifier = Modifier
    )
}
