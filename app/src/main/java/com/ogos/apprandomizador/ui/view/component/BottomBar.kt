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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    currentRoute: MutableState<String>
) {
    val icon1 = remember { mutableStateOf(Color.Magenta) }
    val icon2 = remember { mutableStateOf(Color.White) }
    val icon3 = remember { mutableStateOf(Color.White) }
    val icon4 = remember { mutableStateOf(Color.White) }
    val changeColors = {
        when (currentRoute.value) {
            "home_randomize" -> {
                icon1.value = Color.Magenta
                icon2.value = Color.White
                icon3.value = Color.White
                icon4.value = Color.White
            }

            "home_shuffle" -> {
                icon1.value = Color.White
                icon2.value = Color.Magenta
                icon3.value = Color.White
                icon4.value = Color.White
            }

            else -> throw IllegalArgumentException("Capotemo o Coursa")
        }
    }
    BottomAppBar {
        NavigationBar {
            NavigationBarItem(
                selected = currentRoute.value == "home_randomize",
                onClick = {
                    navController.navigate(route = "home_randomize")
                    currentRoute.value = "home_randomize"
                    changeColors()
                },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.dice_3_svgrepo_com),
                        contentDescription = stringResource(R.string.draw),
                        tint = icon1.value,
                        modifier = Modifier.size(32.dp)
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.draw),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = icon1.value

                    )
                }
            )
            NavigationBarItem(
                selected = currentRoute.value == "home_shuffle",
                onClick = {
                    navController.navigate(route = "home_shuffle")
                    currentRoute.value = "home_shuffle"
                    changeColors()
                },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Shuffle,
                        contentDescription = stringResource(R.string.shuffle),
                        modifier = Modifier.size(32.dp),
                        tint = icon2.value
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.shuffle),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = icon2.value
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
                        tint = icon3.value
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.fingers),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = icon3.value
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
                        tint = icon4.value
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.modes),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = icon4.value
                    )
                }
            )
        }
    }
}