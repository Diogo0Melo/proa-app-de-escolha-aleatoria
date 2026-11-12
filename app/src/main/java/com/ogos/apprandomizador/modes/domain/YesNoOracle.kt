package com.ogos.apprandomizador.modes.domain

import com.ogos.apprandomizador.core.entropy.EntropyStream
import com.ogos.apprandomizador.core.entropy.uniformInt

enum class YesNoAnswer { YES, NO }

/** Regra de domínio pura: cada resposta tem probabilidade uniforme. */
object YesNoOracle {
    internal fun ask(stream: EntropyStream): YesNoAnswer =
        if (stream.uniformInt(2) == 0) YesNoAnswer.YES else YesNoAnswer.NO
}
