package com.ogos.apprandomizador

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ogos.apprandomizador.core.di.ViewModelFactory
import com.ogos.apprandomizador.core.ui.MainViewModel
import com.ogos.apprandomizador.core.ui.navigation.AppNavigation
import com.ogos.apprandomizador.core.ui.theme.AppRandomizadorTheme
import com.ogos.apprandomizador.core.ui.theme.ThemeMode
import com.ogos.apprandomizador.features.randomize.data.DefaultInitialize

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
