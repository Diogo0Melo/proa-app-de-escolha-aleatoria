package com.ogos.apprandomizador.features.fingers.domain

/**
 * Paleta dos dedos, em ARGB. Cores saturadas o bastante para contrastar com
 * fundo escuro (AMOLED) e claro. O teal do mockup vem primeiro.
 *
 * A cor depende da ordem de entrada e não do que está na tela: cada toque novo
 * avança uma posição. Assim, um dedo que sai e outro que entra em seguida nunca
 * dividem a cor. Com 10 cores, os 10 primeiros toques são sempre distintos.
 */
object FingersPalette {
    private val colors: List<Long> = listOf(
        0xFF1DE9B6L, // teal, destaque do mockup
        0xFFFF5252L, // coral
        0xFFFFD740L, // amber
        0xFF448AFFL, // azul
        0xFFE040FBL, // magenta
        0xFF69F0AEL, // verde
        0xFFFF6E40L, // laranja profundo
        0xFFB388FFL, // violeta
        0xFF40C4FFL, // azul claro
        0xFFF48FB1L  // rosa
    )

    val size: Int get() = colors.size

    /** Cor do [entryIndex]-ésimo toque da sessão (0-based). Cicla após [size] toques. */
    fun colorAt(entryIndex: Int): Long {
        require(entryIndex >= 0) { "entryIndex não pode ser negativo: $entryIndex" }
        return colors[entryIndex % colors.size]
    }
}
