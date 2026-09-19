package com.ogos.apprandomizador.ui.view

import android.annotation.SuppressLint
import android.media.MediaPlayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ogos.apprandomizador.R
import com.ogos.apprandomizador.model.RaffleItem
import com.ogos.apprandomizador.ui.view.component.RouletteWheel
import com.ogos.apprandomizador.viewmodel.RandomizeViewModel
import com.ogos.apprandomizador.viewmodel.SpinTarget


@SuppressLint("LocalContextGetResourceValueCall")
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
    val itemList by viewModel.currentItemList.collectAsState()
    val spinTarget by viewModel.spinTarget.collectAsState()
    val color = MaterialTheme.colorScheme.primary.toArgb().toLong()
    val onPerformRoll = { viewModel.performRoll() }
    LaunchedEffect(itemID) {
        viewModel.setCurrentItem(itemID)
    }
    LaunchedEffect(color) {
        if (viewModel.currentItemList.value.uses == 0) {
            viewModel.defaultText(defaultString = context.getString(R.string.default_shuffle_text), color = color)
        }
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
    if (!result.isBlank()) {
        RandomizeViewMain(
            result = result,
            items = itemList.items,
            spinTarget = spinTarget,
            uses = itemList.uses,
            onPerformRoll = onPerformRoll,
            onSpinFinished = { viewModel.finishSpin() },
            isSpinning = isSpinning,
            modifier = modifier
        )
    }
}

@Composable
fun RandomizeViewMain(
    result: RaffleItem,
    items: List<RaffleItem>,
    spinTarget: SpinTarget?,
    uses: Int,
    onPerformRoll: () -> Unit,
    onSpinFinished: () -> Unit,
    isSpinning: Boolean,
    modifier: Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {

        RandomizeViewContent(
            items = items,
            spinTarget = spinTarget,
            uses = uses,
            isSpinning = isSpinning,
            onSpinFinished = onSpinFinished,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        RandomizeViewBottomBar(
            onPerformRoll = onPerformRoll,
            isSpinning = isSpinning,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
fun RandomizeViewContent(
    items: List<RaffleItem>,
    spinTarget: SpinTarget?,
    uses: Int,
    isSpinning: Boolean,
    onSpinFinished: () -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val shuffledItems = remember(items) { items.shuffled() }
        RouletteWheel(
            items = shuffledItems,
            spinTarget = spinTarget,
            centerHighlighted = isSpinning || uses > 0,
            onSpinFinished = onSpinFinished,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}
@Composable
fun RandomizeViewBottomBar(
    onPerformRoll: () -> Unit,
    isSpinning: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = onPerformRoll,
                enabled = !isSpinning,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.roll),
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { /* TODO */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.disable))
                }

                OutlinedButton(
                    onClick = { /* TODO */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.reset))
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
        items = listOf(
            RaffleItem("Alpha", 0xFF9D4EDD),
            RaffleItem("Beta", 0xFF4CAF50),
            RaffleItem("Gamma", 0xFFD6E024),
            RaffleItem("Delta", 0xFFD27024),
        ),
        spinTarget = null,
        uses = 0,
        onPerformRoll = { },
        onSpinFinished = { },
        isSpinning = false,
        modifier = Modifier
    )
}
