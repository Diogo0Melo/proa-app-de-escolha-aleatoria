package com.ogos.apprandomizador.modes.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ogos.apprandomizador.modes.ui.hub.ModesHubRoute
import com.ogos.apprandomizador.modes.ui.hub.ModesHubViewModel
import com.ogos.apprandomizador.modes.ui.ai.AiSettingsRoute
import com.ogos.apprandomizador.modes.ui.ai.AiSettingsViewModel
import com.ogos.apprandomizador.modes.ui.oracle.OracleRoute
import com.ogos.apprandomizador.modes.ui.oracle.OracleViewModel
import com.ogos.apprandomizador.modes.ui.tarot.TarotRoute
import com.ogos.apprandomizador.modes.ui.tarot.TarotViewModel
import com.ogos.apprandomizador.ui.navigation.Routes

fun NavGraphBuilder.modesGraph(
    navController: NavController,
    factory: ViewModelProvider.Factory,
    soundEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    composable(Routes.HOME_MODES) {
        val viewModel: ModesHubViewModel = viewModel(factory = factory)
        val stats by viewModel.stats.collectAsStateWithLifecycle()
        ModesHubRoute(
            stats = stats,
            onModeSelected = { route -> navController.navigate(route) },
            modifier = modifier
        )
    }
    composable(
        route = Routes.MODE_AI_SETTINGS,
        enterTransition = { fadeIn(tween(200)) },
        exitTransition = { fadeOut(tween(150)) }
    ) {
        val viewModel: AiSettingsViewModel = viewModel(factory = factory)
        AiSettingsRoute(viewModel = viewModel, modifier = modifier)
    }
    composable(
        route = Routes.MODE_ORACLE,
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
    ) {
        val viewModel: OracleViewModel = viewModel(factory = factory)
        OracleRoute(viewModel = viewModel, modifier = modifier)
    }
    composable(
        route = Routes.MODE_TAROT,
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
    ) {
        val viewModel: TarotViewModel = viewModel(factory = factory)
        TarotRoute(viewModel = viewModel, soundEnabled = soundEnabled, modifier = modifier)
    }
}

