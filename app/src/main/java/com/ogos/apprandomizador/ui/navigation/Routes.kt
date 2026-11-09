package com.ogos.apprandomizador.ui.navigation

object Routes {
    const val HOME_RANDOMIZE = "home_randomize"
    const val HOME_SHUFFLE = "home_shuffle"
    const val HOME_MODES = "home_modes"
    const val MODE_YES_NO = "mode_yes_no"
    const val MODE_TAROT = "mode_tarot"
    const val MODE_AI_SETTINGS = "mode_ai_settings"
    const val RANDOMIZE_ROUTE = "randomize_route"
    const val RANDOMIZE_FULL = "randomize_route/{itemID}"

    fun randomizeWithId(id: Long) = "randomize_route/$id"
}
