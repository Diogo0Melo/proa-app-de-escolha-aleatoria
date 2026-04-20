package com.ogos.apprandomizador.model

import androidx.annotation.StringRes
import com.ogos.apprandomizador.R

data class TopBarState(
    @StringRes val titleRes: Int = R.string.app_name,
    val showBackButton: Boolean = false,
    val onBackClick: () -> Unit = {},
    val actions: List<TopBarAction> = emptyList()
)
