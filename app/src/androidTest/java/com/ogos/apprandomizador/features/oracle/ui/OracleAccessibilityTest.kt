package com.ogos.apprandomizador.features.oracle.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.ogos.apprandomizador.core.ui.theme.AppRandomizadorTheme
import com.ogos.apprandomizador.features.oracle.domain.DefaultOracleAnswers
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OracleAccessibilityTest {
    @get:Rule
    val compose = createComposeRule()

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
