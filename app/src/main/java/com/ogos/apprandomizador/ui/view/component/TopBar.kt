package com.ogos.apprandomizador.ui.view.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.ogos.apprandomizador.R
import com.ogos.apprandomizador.model.TopBarState
import com.ogos.apprandomizador.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    state: TopBarState,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    soundEnabled: Boolean = true,
    onSoundToggle: (Boolean) -> Unit = {}
) {
    var themeMenuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(text = stringResource(state.titleRes))
        },
        navigationIcon = {
            if (state.showBackButton) {
                IconButton(onClick = state.onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }
        },
        actions = {
            state.actions.forEach { action ->
                IconButton(onClick = action.onClick) {
                    Icon(
                        imageVector = action.iconImageVector,
                        contentDescription = stringResource(action.contentDescriptionRes)
                    )
                }
            }

            IconButton(onClick = { themeMenuExpanded = true }) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.cd_theme)
                )
            }

            DropdownMenu(
                expanded = themeMenuExpanded,
                onDismissRequest = { themeMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(if (soundEnabled) R.string.sound_on else R.string.sound_off)) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (soundEnabled) Icons.Outlined.VolumeUp else Icons.Outlined.VolumeOff,
                            contentDescription = null
                        )
                    },
                    onClick = { onSoundToggle(!soundEnabled) }
                )
                ThemeMode.entries.forEach { mode ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(mode.labelRes),
                                fontWeight = if (mode == themeMode) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        trailingIcon = {
                            if (mode == themeMode) {
                                Icon(Icons.Filled.Check, contentDescription = null)
                            }
                        },
                        onClick = {
                            onThemeModeChange(mode)
                            themeMenuExpanded = false
                        }
                    )
                }
            }
        }
    )
}
