package com.ogos.apprandomizador

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.ogos.apprandomizador.ui.theme.ThemeMode
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
            val factory = remember {
                ViewModelFactory(app.repository, DataStoreManager(this), DefaultInitialize())
            }
            val mainViewModel: MainViewModel = viewModel(factory = factory)
            val isReady by mainViewModel.isReady.collectAsState()
            val themeMode by mainViewModel.themeMode.collectAsState()
            val resolvedThemeMode = themeMode ?: ThemeMode.SYSTEM

            AppRandomizadorTheme(themeMode = resolvedThemeMode) {
                if (isReady && themeMode != null) {
                    AppNavigation(
                        factory = factory,
                        themeMode = resolvedThemeMode,
                        onThemeModeChange = mainViewModel::setThemeMode
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppNavigation(
    factory: ViewModelProvider.Factory,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val topBarState = remember(currentRoute, navController) {
        when {
            currentRoute == "home_randomize" || currentRoute == null -> TopBarState(
                titleRes = R.string.draw_item_from_list,
                showBackButton = false,
                actions = listOf(
                    TopBarAction(Icons.Filled.Add, R.string.add_new_item) { }
                )
            )
            currentRoute == "home_shuffle" -> TopBarState(
                titleRes = R.string.shuffle_item_from_list,
                showBackButton = false,
                actions = listOf(
                    TopBarAction(Icons.Filled.Add, R.string.add_new_item) { }
                )
            )
            currentRoute.startsWith("randomize_route/") -> TopBarState(
                titleRes = R.string.blank_text,
                showBackButton = true,
                onBackClick = { navController.popBackStack() },
                actions = listOf(
                    TopBarAction(Icons.Default.Analytics, R.string.statistics) { },
                    TopBarAction(Icons.Default.Edit, R.string.edit) { }
                )
            )
            else -> TopBarState()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                state = topBarState,
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange
            )
        },
        bottomBar = {
            if (currentRoute in listOf("home_randomize", "home_shuffle")) BottomBar(
                navController = navController,
                currentRoute = currentRoute
            )
        },
    ) { paddingValues ->
        NavHost(navController = navController, startDestination = "home_randomize") {
            composable("home_randomize") { backStackEntry ->
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
            composable("home_shuffle") {
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
