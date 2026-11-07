package com.ogos.apprandomizador

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ogos.apprandomizador.data.repository.DefaultInitialize
import com.ogos.apprandomizador.model.TopBarAction
import com.ogos.apprandomizador.model.TopBarState
import com.ogos.apprandomizador.modes.ui.modesGraph
import com.ogos.apprandomizador.ui.navigation.Routes
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

private val BOTTOM_BAR_ROUTES = setOf(Routes.HOME_RANDOMIZE, Routes.HOME_SHUFFLE, Routes.HOME_MODES)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = application as RandomApplication
            val factory = remember {
                ViewModelFactory(app.repository, app.dataStoreManager, DefaultInitialize(), app.entropySource)
            }
            val mainViewModel: MainViewModel = viewModel(factory = factory)
            val isReady by mainViewModel.isReady.collectAsStateWithLifecycle()
            val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()
            val soundEnabled by mainViewModel.soundEnabled.collectAsStateWithLifecycle()
            val resolvedThemeMode = themeMode ?: ThemeMode.SYSTEM

            AppRandomizadorTheme(themeMode = resolvedThemeMode) {
                if (isReady && themeMode != null) {
                    AppNavigation(
                        factory = factory,
                        themeMode = resolvedThemeMode,
                        onThemeModeChange = mainViewModel::setThemeMode,
                        soundEnabled = soundEnabled,
                        onSoundToggle = mainViewModel::setSoundEnabled
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
    onThemeModeChange: (ThemeMode) -> Unit,
    soundEnabled: Boolean = true,
    onSoundToggle: (Boolean) -> Unit = {}
) {
    val navController = rememberNavController()
    var lastRandomizeNavTime by remember { mutableLongStateOf(0L) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val topBarState = remember(currentRoute, navController) {
        when {
            currentRoute == Routes.HOME_RANDOMIZE || currentRoute == null -> TopBarState(
                titleRes = R.string.draw_item_from_list,
                showBackButton = false,
                actions = listOf(
                    TopBarAction(Icons.Filled.Add, R.string.add_new_item) { }
                )
            )

            currentRoute == Routes.HOME_SHUFFLE -> TopBarState(
                titleRes = R.string.shuffle_item_from_list,
                showBackButton = false,
                actions = listOf(
                    TopBarAction(Icons.Filled.Add, R.string.add_new_item) { }
                )
            )

            currentRoute == Routes.HOME_MODES -> TopBarState(
                titleRes = R.string.modes,
                showBackButton = false
            )

            currentRoute == Routes.MODE_YES_NO -> TopBarState(
                titleRes = R.string.mode_yes_no,
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )

            currentRoute == Routes.MODE_TAROT -> TopBarState(
                titleRes = R.string.mode_tarot,
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )

            currentRoute.startsWith("${Routes.RANDOMIZE_ROUTE}/") -> TopBarState(
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
                onThemeModeChange = onThemeModeChange,
                soundEnabled = soundEnabled,
                onSoundToggle = onSoundToggle
            )
        },
        bottomBar = {
            if (currentRoute in BOTTOM_BAR_ROUTES) BottomBar(
                navController = navController,
                currentRoute = currentRoute
            )
        },
    ) { paddingValues ->
        NavHost(navController = navController, startDestination = Routes.HOME_RANDOMIZE) {
            composable(Routes.HOME_RANDOMIZE) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.HOME_RANDOMIZE)
                }
                val viewModel: RandomizeViewModel =
                    viewModel(viewModelStoreOwner = parentEntry, factory = factory)
                HomeRandomizeViewRoute(
                    viewModel = viewModel,
                    onNavigateToActive = { itemID ->
                        val now = SystemClock.uptimeMillis()
                        if (now - lastRandomizeNavTime >= 800L) {
                            lastRandomizeNavTime = now
                            navController.navigate(Routes.randomizeWithId(itemID)) {
                                launchSingleTop = true
                            }
                        }
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            composable(
                route = Routes.RANDOMIZE_FULL, arguments = listOf(
                    navArgument("itemID") { type = NavType.LongType },
                ),
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { it / 4 }) + fadeIn(tween(200))
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { it / 4 }) + fadeOut(tween(150))
                },
                popEnterTransition = {
                    slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn(tween(200))
                },
                popExitTransition = {
                    slideOutHorizontally(targetOffsetX = { -it / 4 }) + fadeOut(tween(150))
                }
            ) { backStackEntry ->
                val itemID = backStackEntry.arguments?.getLong("itemID") ?: -1
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.RANDOMIZE_FULL)
                }
                val viewModel: RandomizeViewModel =
                    viewModel(viewModelStoreOwner = parentEntry, factory = factory)
                RandomizeViewRoute(
                    itemID = itemID,
                    viewModel = viewModel,
                    soundEnabled = soundEnabled,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            composable(Routes.HOME_SHUFFLE) {
                val viewModel: RandomizeViewModel =
                    viewModel(factory = factory)
                HomeShuffleViewRoute(
                    viewModel = viewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            modesGraph(
                navController = navController,
                factory = factory,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
