package com.ogos.apprandomizador

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ogos.apprandomizador.data.database.DataStoreManager
import com.ogos.apprandomizador.data.repository.DefaultInitialize
import com.ogos.apprandomizador.model.TopBarAction
import com.ogos.apprandomizador.model.TopBarState
import com.ogos.apprandomizador.ui.theme.AppRandomizadorTheme
import com.ogos.apprandomizador.ui.view.HomeRandomizeViewRoute
import com.ogos.apprandomizador.ui.view.HomeShuffleViewRoute
import com.ogos.apprandomizador.ui.view.RandomizeViewRoute
import com.ogos.apprandomizador.ui.view.component.BottomBar
import com.ogos.apprandomizador.ui.view.component.TopBar
import com.ogos.apprandomizador.viewmodel.MainViewModel
import com.ogos.apprandomizador.viewmodel.RandomizeViewModel
import com.ogos.apprandomizador.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = application as RandomApplication
            val factory = ViewModelFactory(
                app.repository, DataStoreManager(this),
                DefaultInitialize()
            )
            val mainViewModel: MainViewModel = viewModel(factory = factory)
            val isReady by mainViewModel.isReady.collectAsState()

            AppRandomizadorTheme {
                if (isReady) {
                    AppNavigation(factory)
                } else {
                    println(stringResource(R.string.waiting_initialization))
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppNavigation(factory: ViewModelProvider.Factory) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val topBarState = remember { mutableStateOf(TopBarState()) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar(state = topBarState.value) },
        bottomBar = {
            if (currentRoute in listOf("home_randomize", "home_shuffle")) BottomBar(
                navController = navController,
                currentRoute = currentRoute
            )
        },
    ) { paddingValues ->
        NavHost(navController = navController, startDestination = "home_randomize") {
            composable("home_randomize") { backStackEntry ->
                topBarState.value = TopBarState(
                    titleRes = R.string.draw_item_from_list,
                    showBackButton = false,
                    actions = listOf(
                        TopBarAction(
                            iconImageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.add_new_item),
                            onClick = { }
                        )
                    )
                )
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("home_randomize")
                }
                val viewModel: RandomizeViewModel =
                    viewModel(viewModelStoreOwner = parentEntry, factory = factory)
                HomeRandomizeViewRoute(
                    viewModel = viewModel,
                    onNavigateToActive = { itemID ->
                        navController.navigate("randomize_route/$itemID")
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            composable(
                route = "randomize_route/{itemID}", arguments = listOf(
                    navArgument("itemID") { type = NavType.LongType },
                )
            ) { backStackEntry ->
                topBarState.value = TopBarState(
                    titleRes = R.string.blank_text,
                    showBackButton = true,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    actions = listOf(
                        TopBarAction(
                            iconImageVector = Icons.Default.Analytics,
                            contentDescription = stringResource(R.string.statistics),
                            onClick = {}
                        ),
                        TopBarAction(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit),
                            onClick = {}
                        )
                    )
                )
                val itemID = backStackEntry.arguments?.getLong("itemID") ?: -1
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("randomize_route/{itemID}")
                }
                val viewModel: RandomizeViewModel =
                    viewModel(viewModelStoreOwner = parentEntry, factory = factory)
                RandomizeViewRoute(
                    itemID = itemID,
                    viewModel = viewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            composable("home_shuffle") { backStackEntry ->
                topBarState.value = TopBarState(
                    titleRes = R.string.shuffle_item_from_list,
                    showBackButton = false,
                    actions = listOf(
                        TopBarAction(
                            iconImageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.add_new_item),
                            onClick = { }
                        )
                    )
                )
                val viewModel: RandomizeViewModel =
                    viewModel(factory = factory)
                HomeShuffleViewRoute(
                    viewModel = viewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}
