package com.ogos.apprandomizador.features.hub.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Style
import androidx.compose.ui.graphics.vector.ImageVector
import com.ogos.apprandomizador.R
import com.ogos.apprandomizador.core.ui.navigation.Routes

data class ModeEntry(
    val id: String,
    val route: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val icon: ImageVector
)

object ModeCatalog {
    val entries = listOf(
        ModeEntry(
            // O id continua "yes_no": as estatísticas do hub já salvas usam esse identificador.
            id = "yes_no",
            route = Routes.MODE_ORACLE,
            titleRes = R.string.mode_oracle,
            descriptionRes = R.string.mode_oracle_description,
            icon = Icons.Filled.AutoAwesome
        ),
        ModeEntry(
            id = "tarot",
            route = Routes.MODE_TAROT,
            titleRes = R.string.mode_tarot,
            descriptionRes = R.string.mode_tarot_description,
            icon = Icons.Filled.Style
        )
    )
}
