package com.ogos.apprandomizador.core.ui.component

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class TopBarAction(
    val iconImageVector: ImageVector,
    @StringRes val contentDescriptionRes: Int,
    val onClick: () -> Unit,
)
