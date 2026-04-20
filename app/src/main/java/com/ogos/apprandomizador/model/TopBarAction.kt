package com.ogos.apprandomizador.model

import androidx.compose.ui.graphics.vector.ImageVector

data class TopBarAction(
    val iconImageVector: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit,
)
