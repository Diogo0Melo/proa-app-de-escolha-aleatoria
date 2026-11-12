package com.ogos.apprandomizador.fingers.domain

import com.ogos.apprandomizador.core.entropy.EntropyStream
import com.ogos.apprandomizador.core.entropy.uniformInt

data class FingersPick(val winnerIndex: Int, val direction: FingersDirection)

/**
 * Sorteio puro: sem Compose e sem Android, testável na JVM.
 *
 * A ordem de consumo do fluxo faz parte do contrato, como em `SpinPlanner`:
 * 1) índice do vencedor (uniforme por rejeição);
 * 2) sentido (uniforme em 2).
 * Novos sorteios cosméticos devem ser anexados ao fim, nunca intercalados.
 */
object FingersPicker {
    internal fun pick(fingerCount: Int, stream: EntropyStream): FingersPick {
        require(fingerCount >= MIN_FINGERS) {
            "São necessários pelo menos $MIN_FINGERS dedos, recebidos $fingerCount"
        }
        val winnerIndex = stream.uniformInt(fingerCount)
        val direction = if (stream.uniformInt(2) == 0) {
            FingersDirection.CLOCKWISE
        } else {
            FingersDirection.COUNTER_CLOCKWISE
        }
        return FingersPick(winnerIndex, direction)
    }

    const val MIN_FINGERS = 2
}
