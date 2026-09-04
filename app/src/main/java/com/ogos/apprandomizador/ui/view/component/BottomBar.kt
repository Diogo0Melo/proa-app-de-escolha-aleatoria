package com.ogos.apprandomizador.ui.view.component

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Stream
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ogos.apprandomizador.R

@Composable
fun BottomBar(
    navController: NavController,
    currentRoute: String?
) {

    val icon1Color = if (currentRoute == "home_randomize") Color.Magenta else Color.White
    val icon2Color = if (currentRoute == "home_shuffle") Color.Magenta else Color.White
    val icon3Color = if (currentRoute == "home_fingers") Color.Magenta else Color.White
    val icon4Color = if (currentRoute == "home_more") Color.Magenta else Color.White

    BottomAppBar {
        NavigationBar {
            NavigationBarItem(
                selected = currentRoute == "home_randomize",
                onClick = {
                    navController.navigate(route = "home_randomize")
                },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.dice_3_svgrepo_com),
                        contentDescription = stringResource(R.string.draw),
                        tint = icon1Color,
                        modifier = Modifier.size(32.dp)
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.draw),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = icon1Color

                    )
                }
            )
            NavigationBarItem(
                selected = currentRoute == "home_shuffle",
                onClick = {
                    navController.navigate(route = "home_shuffle")
                },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Shuffle,
                        contentDescription = stringResource(R.string.shuffle),
                        modifier = Modifier.size(32.dp),
                        tint = icon2Color
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.shuffle),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = icon2Color
                    )
                }
            )
            NavigationBarItem(
                selected = false,
                onClick = {},
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Fingerprint,
                        contentDescription = stringResource(R.string.fingers),
                        modifier = Modifier.size(32.dp),
                        tint = icon3Color
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.fingers),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = icon3Color
                    )
                }
            )
            NavigationBarItem(
                selected = false,
                onClick = {},
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Stream,
                        contentDescription = stringResource(R.string.modes),
                        modifier = Modifier.size(32.dp),
                        tint = icon4Color
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.modes),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = icon4Color
                    )
                }
            )
        }
    }
}