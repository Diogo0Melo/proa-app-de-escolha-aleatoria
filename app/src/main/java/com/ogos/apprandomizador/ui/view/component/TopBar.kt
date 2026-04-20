package com.ogos.apprandomizador.ui.view.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ogos.apprandomizador.model.TopBarState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    state: TopBarState
) {
    TopAppBar(
        title = {
            Text(text = stringResource(state.titleRes))
        },
        navigationIcon = {
            if (state.showBackButton) {
                IconButton(onClick = state.onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }
            }
        },
        actions = {
            state.actions.forEach { action ->
                IconButton(onClick = action.onClick) {
                    Icon(
                        imageVector = action.iconImageVector,
                        contentDescription = action.contentDescription
                    )
                }
            }
        }
    )
}