package com.ogos.apprandomizador.features.tarot.ui

/**
 * Mapeia o ID de uma carta para o arquivo de imagem em `assets/tarot/`.
 * Os Maiores usam "00".."21" (arquivos m00..m21); os Menores usam o próprio ID (w01..p14).
 * Retorna null para qualquer ID fora desse formato, para nunca carregar a imagem errada.
 */
internal object TarotCardImages {
    private val majorId = Regex("^(0[0-9]|1[0-9]|2[01])$")
    private val minorId = Regex("^[wcsp](0[1-9]|1[0-4])$")

    const val CARD_BACK_PATH = "tarot/card-back.webp"

    fun assetPathFor(cardId: String): String? = when {
        majorId.matches(cardId) -> "tarot/m$cardId.webp"
        minorId.matches(cardId) -> "tarot/$cardId.webp"
        else -> null
    }

    fun assetUri(path: String): String = "file:///android_asset/$path"
}
