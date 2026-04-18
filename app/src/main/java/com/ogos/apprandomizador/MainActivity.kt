package com.ogos.apprandomizador

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.ogos.apprandomizador.viewmodel.ChoiceViewModel
import com.ogos.apprandomizador.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    @SuppressLint("ViewModelConstructorInComposable")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = application as RandomApplication
            val repository = app.repository
            val choiceViewModel = ChoiceViewModel(repository)
            val mainViewModel = MainViewModel(repository, DataStoreManager(this))
            val isReady by mainViewModel.isReady.collectAsState()
            AppRandomizadorTheme {
                if (isReady) {
                    AppNavigation(viewModel = choiceViewModel)
                }
                println("Aguardando inicialização...")
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: ChoiceViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "preset_selection") {
        composable("preset_selection") {
            PresetSelectionScreen(
                viewModel = viewModel,
                onNavigateToActive = { itemIndex ->
                    navController.navigate("active_randomizer/$itemIndex")
                }
            )
        }
        composable(
            route = "active_randomizer/{itemIndex}", arguments = listOf(
                navArgument("itemIndex") { type = NavType.IntType },
            )
        ) { backStackEntry ->
            val itemIndex = backStackEntry.arguments?.getInt("itemIndex") ?: -1
            ActiveRandomizerScreen(
                onBack = {
                    navController.popBackStack()
                },
                viewModel = viewModel,
                itemIndex = itemIndex
            )
        }
    }
}
