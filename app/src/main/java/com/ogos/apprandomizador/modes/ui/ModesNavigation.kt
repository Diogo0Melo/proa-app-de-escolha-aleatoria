package com.ogos.apprandomizador.modes.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ogos.apprandomizador.modes.ui.hub.ModesHubRoute
import com.ogos.apprandomizador.modes.ui.yesno.YesNoRoute
import com.ogos.apprandomizador.modes.ui.yesno.YesNoViewModel
import com.ogos.apprandomizador.ui.navigation.Routes

fun NavGraphBuilder.modesGraph(
    navController: NavController,
    factory: ViewModelProvider.Factory,
    modifier: Modifier = Modifier
) {
    composable(Routes.HOME_MODES) {
        ModesHubRoute(
            onModeSelected = { route -> navController.navigate(route) },
            modifier = modifier
        )
    }
    composable(
        route = Routes.MODE_YES_NO,
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
        val viewModel: YesNoViewModel = viewModel(factory = factory)
        YesNoRoute(viewModel = viewModel, modifier = modifier)
    }
}
