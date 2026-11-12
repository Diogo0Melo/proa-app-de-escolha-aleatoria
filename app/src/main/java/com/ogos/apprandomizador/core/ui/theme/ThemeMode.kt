package com.ogos.apprandomizador.core.ui.theme

import androidx.annotation.StringRes
import com.ogos.apprandomizador.R

enum class ThemeMode(val key: String, @StringRes val labelRes: Int) {
    SYSTEM("system", R.string.theme_system),
    LIGHT("light", R.string.theme_light),
    DARK("dark", R.string.theme_dark),
    AMOLED("amoled", R.string.theme_amoled);

    companion object {
        fun fromKey(key: String?): ThemeMode =
            entries.firstOrNull { it.key == key } ?: SYSTEM
    }
}
