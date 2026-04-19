package com.ogos.apprandomizador

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ogos.apprandomizador.model.database.DataStoreManager
import com.ogos.apprandomizador.ui.theme.AppRandomizadorTheme
import com.ogos.apprandomizador.ui.view.HomeRandomizeViewRoute
import com.ogos.apprandomizador.ui.view.RandomizeViewRoute
import com.ogos.apprandomizador.viewmodel.RandomizeViewModel
import com.ogos.apprandomizador.viewmodel.MainViewModel
import com.ogos.apprandomizador.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = application as RandomApplication
            val factory = ViewModelFactory(app.repository, DataStoreManager(this))
            val mainViewModel: MainViewModel = viewModel(factory = factory)
            val isReady by mainViewModel.isReady.collectAsState()

            AppRandomizadorTheme {
                if (isReady) {
                    AppNavigation(factory)
                }
                println("Aguardando inicialização...")
            }
        }
    }
}

@Composable
fun AppNavigation(factory: ViewModelProvider.Factory) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "preset_selection") {
        composable("preset_selection") { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("preset_selection")
            }
            val viewModel: RandomizeViewModel =
                viewModel(viewModelStoreOwner = parentEntry, factory = factory)
            HomeRandomizeViewRoute(
                viewModel = viewModel,
                onNavigateToActive = { itemID ->
                    navController.navigate("randomize_route/$itemID")
                }
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
                onBack = {
                    navController.popBackStack()
                },
                itemID = itemID,
                viewModel = viewModel,
            )
        }
    }
}
