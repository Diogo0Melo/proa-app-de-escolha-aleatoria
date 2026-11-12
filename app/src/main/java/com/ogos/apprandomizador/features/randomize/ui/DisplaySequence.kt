package com.ogos.apprandomizador.features.randomize.ui

import com.ogos.apprandomizador.core.entropy.cryptoShuffled
import com.ogos.apprandomizador.core.model.RaffleItem
import com.ogos.apprandomizador.features.randomize.ui.component.MAX_DISPLAY_SLOTS

private const val MIN_SEQUENCE_SLOTS = 4

/**
 * Builds the per-spin display sequence handed to the wheel.
 *
 * - <= maxDisplaySlots items: every real item once; lists smaller than
 *   MIN_SEQUENCE_SLOTS are cycled up to avoid degenerate cylinder math.
 * - > maxDisplaySlots items: a subset of maxDisplaySlots unique indices that
 *   ALWAYS includes [target] (the winner picked by the ViewModel), so the spin
 *   can land on the exact winning item.
 *
 * Ordering comes from [randomBound] when supplied — a uniform integer generator
 * backed by the spin's cryptographic stream, so the card order is part of the
 * same chance as the winner. With no generator (idle state, unit tests) it falls
 * back to the platform shuffle.
 */
internal fun buildDisplaySequence(
    items: List<RaffleItem>,
    target: RaffleItem?,
    maxDisplaySlots: Int = MAX_DISPLAY_SLOTS,
    randomBound: ((Int) -> Int)? = null
): List<RaffleItem> {
    if (items.isEmpty()) return emptyList()

    if (items.size <= maxDisplaySlots) {
        val base = shuffleOf(items, randomBound)
        if (base.size >= MIN_SEQUENCE_SLOTS) return base
        return buildList {
            while (size < MIN_SEQUENCE_SLOTS) {
                addAll(base)
            }
        }
    }

    if (target == null) {
        return shuffleOf(items, randomBound).take(maxDisplaySlots)
    }

    val targetIndex = items.indexOf(target)
    if (targetIndex < 0) {
        return shuffleOf(items, randomBound).take(maxDisplaySlots)
    }

    val otherIndices = shuffleOf(
        items.indices.filter { it != targetIndex },
        randomBound
    ).take(maxDisplaySlots - 1)

    return shuffleOf(listOf(targetIndex) + otherIndices, randomBound).map { items[it] }
}

/** Embaralha por [randomBound] quando houver fluxo criptográfico; senão, o padrão. */
private fun <T> shuffleOf(source: List<T>, randomBound: ((Int) -> Int)?): List<T> =
    if (randomBound != null) cryptoShuffled(source, randomBound) else source.shuffled()
