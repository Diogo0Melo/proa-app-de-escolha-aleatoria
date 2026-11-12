package com.ogos.apprandomizador.core.ui.component

import androidx.annotation.StringRes
import com.ogos.apprandomizador.R

data class TopBarState(
    @StringRes val titleRes: Int = R.string.app_name,
    val title: String? = null,
    val showBackButton: Boolean = false,
    val onBackClick: () -> Unit = {},
    val actions: List<TopBarAction> = emptyList()
)
