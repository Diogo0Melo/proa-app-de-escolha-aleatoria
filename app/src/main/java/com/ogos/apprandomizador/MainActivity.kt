package com.ogos.apprandomizador

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ogos.apprandomizador.database.AppDatabase
import com.ogos.apprandomizador.database.DataStoreManager
import com.ogos.apprandomizador.model.ItemList
import com.ogos.apprandomizador.repository.ItemListRepository
import com.ogos.apprandomizador.ui.theme.AppRandomizadorTheme
import com.ogos.apprandomizador.ui.screens.PresetSelectionScreen
import com.ogos.apprandomizador.ui.screens.ActiveRandomizerScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val dataStoreManager = DataStoreManager(this)
            val isFirstTime = dataStoreManager.isFirstTime.collectAsState(null)
            val appDatabase = AppDatabase.getDatabase(this)
            val itemDao = appDatabase.itemDao()
            ItemListRepository.itemDao = itemDao
            AppRandomizadorTheme {
                when (isFirstTime.value) {
                    null -> {}
                    true -> {
                        LaunchedEffect(isFirstTime) {
                            dataStoreManager.saveFirstAcess(false)
                            ItemListRepository.createDefault()
                            ItemListRepository.saveInDatabase()
                        }
                    }
                    false -> {
                        LaunchedEffect(isFirstTime) {
                            ItemListRepository.readFromDatabase()
                        }
                    }
                }
                AppNavigation()
            }
        }
    }

}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "preset_selection") {
        composable("preset_selection") {
            PresetSelectionScreen(
                onNavigateToActive = { itemIndex ->
                    navController.navigate("active_randomizer/$itemIndex")
                }
            )
        }
        composable(
            route = "active_randomizer/{itemIndex}", arguments = listOf(
                navArgument("itemIndex") { type = NavType.IntType }
            )) { backStackEntry ->
            val itemIndex = backStackEntry.arguments?.getInt("itemIndex") ?: -1
            ActiveRandomizerScreen(
                onBack = {
                    navController.popBackStack()
                },
                itemIndex = itemIndex
            )
        }
    }
}
