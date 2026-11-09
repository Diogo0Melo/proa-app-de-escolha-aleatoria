package com.ogos.apprandomizador.modes.ui.hub

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Style
import androidx.compose.ui.graphics.vector.ImageVector
import com.ogos.apprandomizador.R
import com.ogos.apprandomizador.ui.navigation.Routes

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
            id = "yes_no",
            route = Routes.MODE_YES_NO,
            titleRes = R.string.mode_yes_no,
            descriptionRes = R.string.mode_yes_no_description,
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
