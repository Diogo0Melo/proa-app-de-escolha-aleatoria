package com.ogos.apprandomizador

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ogos.apprandomizador.ui.theme.AppRandomizadorTheme
import com.ogos.apprandomizador.view.PresetSelectionScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContent {
            AppRandomizadorTheme {
                PresetSelectionScreen()
            }
        }
    }
}
