package com.ogos.apprandomizador

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ogos.apprandomizador.database.DataStoreManager
import com.ogos.apprandomizador.repository.ItemListRepository
import com.ogos.apprandomizador.ui.theme.AppRandomizadorTheme
import com.ogos.apprandomizador.ui.screens.PresetSelectionScreen
import com.ogos.apprandomizador.ui.screens.ActiveRandomizerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = application as RandomApplication
            val repository = app.repository
            val dataStoreManager = DataStoreManager(this)
            val isFirstTime = dataStoreManager.isFirstTime.collectAsState(null)
            AppRandomizadorTheme {
                LaunchedEffect(isFirstTime.value) {
                    when (isFirstTime.value) {
                        true -> {
                            dataStoreManager.saveFirstAcess(false)
                            repository.createDefault()
                            repository.saveInDatabase()
                        }

                        false -> {
                            repository.readFromDatabase()
                        }

                        null -> {}
                    }
                }
                AppNavigation(repository = repository)
            }
        }
    }
}

@Composable
fun AppNavigation(repository: ItemListRepository) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "preset_selection") {
        composable("preset_selection") {
            PresetSelectionScreen(
                repository = repository,
                onNavigateToActive = { itemIndex ->
                    navController.navigate("active_randomizer/$itemIndex")
                }
            )
        }
        composable(
            route = "active_randomizer/{itemIndex}", arguments = listOf(
                navArgument("itemIndex") { type = NavType.IntType },
            )) { backStackEntry ->
            val itemIndex = backStackEntry.arguments?.getInt("itemIndex") ?: -1
            ActiveRandomizerScreen(
                repository = repository,
                onBack = {
                    navController.popBackStack()
                },
                itemIndex = itemIndex
            )
        }
    }
}
