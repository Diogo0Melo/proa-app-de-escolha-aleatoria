package com.ogos.apprandomizador.modes.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.ogos.apprandomizador.core.ui.navigation.Routes
import com.ogos.apprandomizador.core.ui.theme.AppRandomizadorTheme
import com.ogos.apprandomizador.features.oracle.domain.DefaultOracleAnswers
import com.ogos.apprandomizador.modes.ui.hub.ModeCatalog
import com.ogos.apprandomizador.modes.ui.hub.ModesHubScreen
import com.ogos.apprandomizador.features.oracle.ui.YesNoOracleActions
import com.ogos.apprandomizador.features.oracle.ui.OraclePhase
import com.ogos.apprandomizador.features.oracle.ui.OracleUiState
import com.ogos.apprandomizador.features.oracle.ui.YesNoOracleScreen
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

    @Test
    fun sugestao_preenche_a_pergunta_sem_disparar_consulta() {
        var question = ""
        var askCalled = false
        compose.setContent {
            AppRandomizadorTheme {
                YesNoOracleScreen(
                    state = OracleUiState(),
                    actions = YesNoOracleActions(
                        onQuestionChange = { question = it },
                        onAsk = { askCalled = true }
                    )
                )
            }
        }

        compose.onNodeWithText("Devo seguir em frente?").performClick()
        compose.runOnIdle {
            assertEquals("Devo seguir em frente com essa ideia?", question)
            assertEquals(false, askCalled)
        }
    }

    @Test
    fun resposta_revelada_fica_anunciada() {
        val answer = DefaultOracleAnswers.items.first()
        compose.setContent {
            AppRandomizadorTheme {
                YesNoOracleScreen(
                    state = OracleUiState(phase = OraclePhase.Revealed, answer = answer),
                    actions = YesNoOracleActions()
                )
            }
        }
        compose.onNodeWithContentDescription("O oráculo respondeu: ${answer.name}").assertIsDisplayed()
    }
}
