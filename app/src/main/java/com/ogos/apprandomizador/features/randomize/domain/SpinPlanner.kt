package com.ogos.apprandomizador.features.randomize.domain

import com.ogos.apprandomizador.core.entropy.deriveSpinStream
import com.ogos.apprandomizador.core.entropy.pickWinnerIndex
import com.ogos.apprandomizador.core.model.RaffleItem

/**
 * Planeja um giro sem efeitos colaterais. A ordem de consumo do fluxo é parte
 * do contrato: vencedor, cinco unidades cosméticas e, por fim, shuffleSeed.
 */
internal fun planSpin(
    items: List<RaffleItem>,
    listId: Long,
    seed: ByteArray,
    requestId: Long,
    fallback: (Int) -> Int
): SpinTarget {
    require(items.isNotEmpty()) { "items must not be empty" }
    val stream = deriveSpinStream(seed, requestId)
    val winnerIndex = pickWinnerIndex(stream, items.size, fallback)
    val winner = items[winnerIndex]
    val cosmetics = SpinCosmetics(
        travelUnit = stream.nextDouble().toFloat(),
        overshootUnit = stream.nextDouble().toFloat(),
        hesitationAmplitudeUnit = stream.nextDouble().toFloat(),
        hesitationCyclesUnit = stream.nextDouble().toFloat(),
        durationUnit = stream.nextDouble().toFloat()
    )
    val shuffleSeed = stream.nextIntNonNegative().toLong() and 0xFFFFFFFFL
    return SpinTarget(
        targetItem = winner,
        listId = listId,
        requestId = requestId,
        cosmetics = cosmetics,
        shuffleSeed = shuffleSeed
    )
}
