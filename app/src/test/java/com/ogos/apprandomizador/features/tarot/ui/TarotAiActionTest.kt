package com.ogos.apprandomizador.features.tarot.ui

import com.ogos.apprandomizador.core.ui.ai.AiAction
import com.ogos.apprandomizador.features.tarot.domain.Reading
import org.junit.Assert.assertEquals
import org.junit.Test

class TarotAiActionTest {
    private val revealed = TarotUiState(phase = TarotPhase.Revealed, aiEnabled = true)

    @Test fun `deepen is offered once the reading is revealed and AI is enabled`() {
        assertEquals(AiAction.Deepen, revealed.aiAction)
    }

    @Test fun `no AI action while AI is disabled`() {
        assertEquals(AiAction.None, revealed.copy(aiEnabled = false).aiAction)
    }

    @Test fun `no AI action before the reading is revealed`() {
        assertEquals(AiAction.None, revealed.copy(phase = TarotPhase.Interpreting).aiAction)
    }

    @Test fun `loading replaces the deepen button`() {
        assertEquals(AiAction.Loading, revealed.copy(aiLoading = true, aiAttempt = 1).aiAction)
    }

    @Test fun `a generation error offers retry`() {
        assertEquals(AiAction.Retry, revealed.copy(aiError = "falhou").aiAction)
    }

    @Test fun `a cancelled generation offers deepen again`() {
        assertEquals(AiAction.Retry, revealed.copy(aiCancelled = true).aiAction)
    }

    @Test fun `a successful AI reading removes every AI action for this reading`() {
        val done = revealed.copy(aiReading = Reading(summary = "ok", sections = emptyList()))
        assertEquals(AiAction.None, done.aiAction)
    }
}
