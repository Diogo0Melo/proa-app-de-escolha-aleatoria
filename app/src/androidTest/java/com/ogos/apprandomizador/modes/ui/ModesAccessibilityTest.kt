package com.ogos.apprandomizador.modes.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.ogos.apprandomizador.modes.domain.YesNoAnswer
import com.ogos.apprandomizador.modes.ui.hub.ModeCatalog
import com.ogos.apprandomizador.modes.ui.hub.ModesHubScreen
import com.ogos.apprandomizador.modes.ui.yesno.YesNoPhase
import com.ogos.apprandomizador.modes.ui.yesno.YesNoScreen
import com.ogos.apprandomizador.modes.ui.yesno.YesNoUiState
import com.ogos.apprandomizador.ui.theme.AppRandomizadorTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ModesAccessibilityTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun hub_abre_modo_sim_ou_nao() {
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
        compose.runOnIdle { assertEquals(com.ogos.apprandomizador.ui.navigation.Routes.MODE_YES_NO, selectedRoute) }
    }

    @Test
    fun resultado_sim_fica_visivel() {
        compose.setContent {
            AppRandomizadorTheme {
                YesNoScreen(
                    state = YesNoUiState(phase = YesNoPhase.Revealed, answer = YesNoAnswer.YES),
                    question = "",
                    onQuestionChange = {},
                    onAsk = {},
                    onReset = {}
                )
            }
        }
        compose.onNodeWithText("SIM").assertIsDisplayed()
    }
}
