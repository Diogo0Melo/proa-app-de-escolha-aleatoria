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
import androidx.compose.material.icons.filled.Settings
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
import com.ogos.apprandomizador.core.di.ViewModelFactory
import com.ogos.apprandomizador.core.ui.MainViewModel
import com.ogos.apprandomizador.core.ui.component.BottomBar
import com.ogos.apprandomizador.core.ui.component.TopBar
import com.ogos.apprandomizador.core.ui.component.TopBarAction
import com.ogos.apprandomizador.core.ui.component.TopBarState
import com.ogos.apprandomizador.core.ui.navigation.AppNavigation
import com.ogos.apprandomizador.core.ui.navigation.Routes
import com.ogos.apprandomizador.core.ui.theme.AppRandomizadorTheme
import com.ogos.apprandomizador.core.ui.theme.ThemeMode
import com.ogos.apprandomizador.features.randomize.data.DefaultInitialize
import com.ogos.apprandomizador.fingers.ui.FingersRoute
import com.ogos.apprandomizador.fingers.ui.FingersViewModel
import com.ogos.apprandomizador.modes.ui.modesGraph
import com.ogos.apprandomizador.features.randomize.ui.HomeRandomizeViewRoute
import com.ogos.apprandomizador.features.randomize.ui.HomeShuffleViewRoute
import com.ogos.apprandomizador.features.randomize.ui.RandomizeViewRoute
import com.ogos.apprandomizador.features.randomize.ui.RandomizeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = application as RandomApplication
            val factory = remember {
                ViewModelFactory(app.container.repository, app.container.dataStoreManager, DefaultInitialize(), app.container.entropySource, oracleRepository = app.container.oracleRepository, aiSettingsRepository = app.container.aiSettingsRepository, aiReadingService = app.container.aiReadingService, aiClient = app.container.llmClient, oracleAiService = app.container.oracleAiService)
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
