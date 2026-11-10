package com.ogos.apprandomizador.ui.view.component

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Stream
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ogos.apprandomizador.R
import com.ogos.apprandomizador.ui.navigation.Routes

@Composable
fun BottomBar(
    navController: NavController,
    currentRoute: String?
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Routes.HOME_RANDOMIZE,
            onClick = {
                navController.navigate(Routes.HOME_RANDOMIZE) {
                    popUpTo(Routes.HOME_RANDOMIZE) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.dice_3_svgrepo_com),
                    contentDescription = stringResource(R.string.draw),
                    modifier = Modifier.size(32.dp)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.draw),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.HOME_SHUFFLE,
            onClick = {
                navController.navigate(Routes.HOME_SHUFFLE) {
                    popUpTo(Routes.HOME_RANDOMIZE) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Shuffle,
                    contentDescription = stringResource(R.string.shuffle),
                    modifier = Modifier.size(32.dp)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.shuffle),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.HOME_FINGERS,
            onClick = {
                navController.navigate(Routes.HOME_FINGERS) {
                    popUpTo(Routes.HOME_RANDOMIZE) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Fingerprint,
                    contentDescription = stringResource(R.string.fingers),
                    modifier = Modifier.size(32.dp)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.fingers),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.HOME_MODES,
            onClick = {
                navController.navigate(Routes.HOME_MODES) {
                    popUpTo(Routes.HOME_RANDOMIZE) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Stream,
                    contentDescription = stringResource(R.string.modes),
                    modifier = Modifier.size(32.dp)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.modes),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )
    }
}
