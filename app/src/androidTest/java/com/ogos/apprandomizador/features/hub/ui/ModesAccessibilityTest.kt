package com.ogos.apprandomizador.features.hub.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.ogos.apprandomizador.core.ui.navigation.Routes
import com.ogos.apprandomizador.core.ui.theme.AppRandomizadorTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ModesAccessibilityTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun hub_abre_oraculo() {
        var selectedRoute: String? = null
        compose.setContent {
            AppRandomizadorTheme {
                ModesHubScreen(
                    modes = ModeCatalog.entries,
                    stats = emptyMap(),
                    onModeSelected = { selectedRoute = it }
                )
            }
        }

        compose.onNodeWithTag("mode-yes_no").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(Routes.MODE_ORACLE, selectedRoute) }
    }
}
