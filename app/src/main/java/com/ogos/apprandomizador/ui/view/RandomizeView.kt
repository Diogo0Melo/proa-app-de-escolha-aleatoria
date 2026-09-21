package com.ogos.apprandomizador.ui.view

import android.annotation.SuppressLint
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.ogos.apprandomizador.model.RaffleItem
import com.ogos.apprandomizador.ui.sound.TickPlayer
import com.ogos.apprandomizador.ui.view.component.MAX_DISPLAY_SLOTS
import com.ogos.apprandomizador.ui.view.component.RouletteWheel
import com.ogos.apprandomizador.viewmodel.RandomizeViewModel
import com.ogos.apprandomizador.viewmodel.SpinTarget


private const val MIN_SEQUENCE_SLOTS = 4

/**
 * Builds the per-spin display sequence handed to the wheel.
 *
 * - <= maxDisplaySlots items: every real item once; lists smaller than
 *   MIN_SEQUENCE_SLOTS are cycled up to avoid degenerate cylinder math.
 * - > maxDisplaySlots items: a random subset of maxDisplaySlots unique
 *   indices that ALWAYS includes [target] (the winner picked by the
 *   ViewModel), so the spin can land on the exact winning item.
 */
internal fun buildDisplaySequence(
    items: List<RaffleItem>,
    target: RaffleItem?,
    maxDisplaySlots: Int = MAX_DISPLAY_SLOTS
): List<RaffleItem> {
    if (items.isEmpty()) return emptyList()

    if (items.size <= maxDisplaySlots) {
        val base = items.shuffled()
        if (base.size >= MIN_SEQUENCE_SLOTS) return base
        return buildList {
            while (size < MIN_SEQUENCE_SLOTS) {
                addAll(base)
            }
        }
    }

    if (target == null) {
        return items.shuffled().take(maxDisplaySlots)
    }

    val targetIndex = items.indexOf(target)
    if (targetIndex < 0) {
        return items.shuffled().take(maxDisplaySlots)
    }

    val otherIndices = items.indices
        .filter { it != targetIndex }
        .shuffled()
        .take(maxDisplaySlots - 1)

    return (listOf(targetIndex) + otherIndices)
        .shuffled()
        .map { items[it] }
}

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
    val onSlotCrossed: (() -> Unit)? = if (isSpinning && soundEnabled) {
        { tickPlayer.play(volume = 1f) }
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
    onSlotCrossed: (() -> Unit)?,
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
    onSlotCrossed: (() -> Unit)?,
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
                buildDisplaySequence(items, target.targetItem)
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
        onSlotCrossed = null,
        isSpinning = false,
        modifier = Modifier
    )
}
