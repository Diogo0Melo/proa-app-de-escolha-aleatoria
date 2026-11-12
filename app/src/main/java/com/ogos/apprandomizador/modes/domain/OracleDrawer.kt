package com.ogos.apprandomizador.modes.domain

import com.ogos.apprandomizador.core.entropy.EntropyStream
import com.ogos.apprandomizador.core.entropy.uniformInt
import com.ogos.apprandomizador.core.model.RaffleItem

/**
 * Sorteio da resposta. Cada item tem probabilidade uniforme e o resultado depende só do fluxo
 * determinístico da tiragem, nunca da IA.
 */
object OracleDrawer {
    internal fun draw(items: List<RaffleItem>, stream: EntropyStream): RaffleItem {
        require(items.isNotEmpty()) { "A fonte do oráculo precisa de pelo menos um item" }
        return items[stream.uniformInt(items.size)]
    }
}
