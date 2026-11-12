package com.ogos.apprandomizador.viewmodel

import com.ogos.apprandomizador.core.model.RaffleItem

/**
 * Encenação de um sorteio: cinco números tirados do fluxo criptográfico, ainda
 * sem forma. Cada campo é uma unidade em `[0, 1)` que a View converte em
 * parâmetro concreto (percurso, excesso, vacilo, duração) usando o tamanho real
 * da sequência exibida — informação que só existe lá embaixo.
 */
data class SpinCosmetics(
    val travelUnit: Float,
    val overshootUnit: Float,
    val hesitationAmplitudeUnit: Float,
    val hesitationCyclesUnit: Float,
    val durationUnit: Float
)

/**
 * One pending draw. The ViewModel picks the winning [targetItem] FIRST; the 3D wheel
 * in the View then locates that item in the displayed (shuffled) list, animates the
 * ring until its slot sits at the center and calls back via finishSpin(). [listId]
 * ties the target to the list it was drawn from, guarding against list changes
 * during a spin. [requestId] makes every roll a distinct key even when the same
 * item is drawn twice in a row.
 *
 * [cosmetics] and [shuffleSeed] come from the same entropy cocktail as the
 * winner, so the whole performance — ordem dos cartões, distância percorrida,
 * se a roda passa do ponto, quão rápido o som bate — é um só acaso, não vários
 * ruídos independentes.
 */
data class SpinTarget(
    val targetItem: RaffleItem,
    val listId: Long,
    val requestId: Long,
    val cosmetics: SpinCosmetics,
    val shuffleSeed: Long
)

