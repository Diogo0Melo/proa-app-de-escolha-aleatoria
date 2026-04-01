package com.ogos.apprandomizador

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ogos.apprandomizador.ui.theme.AppRandomizadorTheme
import com.ogos.apprandomizador.ui.screens.PresetSelectionScreen
import com.ogos.apprandomizador.ui.screens.ActiveRandomizerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppRandomizadorTheme {
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
                onNavigateToActive = { navController.navigate("active_randomizer") }
            )
        }
        composable("active_randomizer") {
            ActiveRandomizerScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
