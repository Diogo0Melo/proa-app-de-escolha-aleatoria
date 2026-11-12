package com.ogos.apprandomizador.features.randomize.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ogos.apprandomizador.R
import com.ogos.apprandomizador.core.entropy.cryptoShuffled
import com.ogos.apprandomizador.core.entropy.shuffleStreamFrom
import com.ogos.apprandomizador.core.entropy.uniformInt
import com.ogos.apprandomizador.core.model.RaffleItem
import com.ogos.apprandomizador.core.ui.voice.TickPlayer
import com.ogos.apprandomizador.features.randomize.domain.SpinTarget
import com.ogos.apprandomizador.features.randomize.ui.RandomizeViewModel
import com.ogos.apprandomizador.features.randomize.ui.component.MAX_DISPLAY_SLOTS
import com.ogos.apprandomizador.features.randomize.ui.component.RouletteWheel

/**
 * A display sequence frozen right after a spin landed, paired with the real
 * list it was built from — keeps the winner centered until the next spin or
 * a genuine list change.
 */
private data class LockedDisplaySequence(
    val sourceItems: List<RaffleItem>,
    val sequence: List<RaffleItem>,
)

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun RandomizeViewRoute(
    itemID: Long,
    viewModel: RandomizeViewModel,
    soundEnabled: Boolean,
    modifier: Modifier
) {
    val context = LocalContext.current
    val tickPlayer = remember { TickPlayer(context.applicationContext, R.raw.roleta_audio) }
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
    // Callback do tick: criado APENAS quando ha giro ativo e som ligado, entao a
    // roleta nao compone caminho morto com o som desligado. O pitch chega pela
    // roleta (velocidade real do giro naquele instante) e vai direto ao SoundPool.
    val onSlotCrossed: ((Float) -> Unit)? = if (isSpinning && soundEnabled) {
        { pitch -> tickPlayer.play(volume = 1f, pitch = pitch) }
    } else {
        null
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> tickPlayer.pause()
                Lifecycle.Event.ON_RESUME -> tickPlayer.resume()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            tickPlayer.release()
        }
    }
    if (!result.isBlank()) {
        RandomizeViewMain(
            result = result,
            items = itemList.items,
            spinTarget = spinTarget,
            uses = itemList.uses,
            onPerformRoll = onPerformRoll,
            onSpinFinished = { viewModel.finishSpin() },
            onSlotCrossed = onSlotCrossed,
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
    onSlotCrossed: ((Float) -> Unit)?,
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
            onSlotCrossed = onSlotCrossed,
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
    onSlotCrossed: ((Float) -> Unit)?,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Sequence for the active spin. Keyed only by requestId so items
        // changes during the animation do not rebuild the animated sequence.
        val spinSequence = remember(spinTarget?.requestId) {
            val target = spinTarget
            if (target != null) {
                val stream = shuffleStreamFrom(target.shuffleSeed)
                buildDisplaySequence(items, target.targetItem) { bound ->
                    stream.uniformInt(bound)
                }
            } else {
                emptyList()
            }
        }

        // Idle sequence rebuilt when the real list changes.
        val idleSequence = remember(items) {
            buildDisplaySequence(items, null)
        }

        // Lock the landed sequence after the spin finishes so the winner stays centered.
        var lockedSequence by remember { mutableStateOf<LockedDisplaySequence?>(null) }
        // Local snapshot — delegated vars cannot be smart-cast.
        val locked = lockedSequence

        val displayItems = when {
            spinTarget != null -> spinSequence.ifEmpty { idleSequence }
            locked != null && locked.sourceItems == items -> locked.sequence
            else -> idleSequence
        }

        val hasLandedResult = locked != null && locked.sourceItems == items

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            RouletteWheel(
                items = displayItems,
                spinTarget = spinTarget,
                centerHighlighted = isSpinning || uses > 0 || hasLandedResult,
                onSpinFinished = {
                    if (spinTarget != null && displayItems.isNotEmpty()) {
                        lockedSequence = LockedDisplaySequence(
                            sourceItems = items,
                            sequence = displayItems,
                        )
                    }
                    onSpinFinished()
                },
                onSlotCrossed = onSlotCrossed,
                modifier = Modifier.fillMaxSize()
            )

            if (displayItems.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        Modifier
                            .size(width = 12.dp, height = 2.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                    )
                    Box(
                        Modifier
                            .size(width = 12.dp, height = 2.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                    )
                }
            }
        }
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
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.roll),
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { /* TODO */ },
                    enabled = !isSpinning,
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.VisibilityOff, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(stringResource(R.string.disable), maxLines = 1)
                }

                OutlinedButton(
                    onClick = { /* TODO */ },
                    enabled = !isSpinning,
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(stringResource(R.string.reset), maxLines = 1)
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
        onSlotCrossed = null,
        isSpinning = false,
        modifier = Modifier
    )
}
