package com.ogos.apprandomizador.modes.ui.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AiSettingsRoute(viewModel: AiSettingsViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showConsent by remember { mutableStateOf(false) }
    var showKey by remember { mutableStateOf(false) }
    var showModelMenu by remember { mutableStateOf(false) }
    var showRemoveConfirmation by remember { mutableStateOf(false) }

    if (showConsent) {
        AlertDialog(
            onDismissRequest = { showConsent = false },
            title = { Text("Privacidade da leitura por IA") },
            text = { Text("Ao testar, a chave e o model ID serão enviados para ${state.baseUrlAuthority ?: "o provedor informado"}. Ao aprofundar, também serão enviados a pergunta (se houver) e os nomes, posições e orientações das cartas. A chave é enviada diretamente por HTTPS; a resposta da IA não é salva no aparelho. O provedor pode aplicar sua própria política de retenção. Deseja permitir esses envios?") },
            confirmButton = { TextButton(onClick = { showConsent = false; viewModel.acceptConsentAndTest() }) { Text("Aceitar e testar") } },
            dismissButton = { TextButton(onClick = { showConsent = false }) { Text("Agora não") } }
        )
    }
    if (showRemoveConfirmation) {
        AlertDialog(
            onDismissRequest = { showRemoveConfirmation = false },
            title = { Text("Remover configuração?") },
            text = { Text("A URL, o modelo e a chave criptografada serão removidos deste aparelho.") },
            confirmButton = { TextButton(onClick = { showRemoveConfirmation = false; viewModel.removeCredentials() }) { Text("Remover") } },
            dismissButton = { TextButton(onClick = { showRemoveConfirmation = false }) { Text("Cancelar") } }
        )
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Leitura por IA · BYOK", style = MaterialTheme.typography.headlineSmall)
        Text("Opcional. A leitura offline continua disponível mesmo sem configurar um provedor.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Conexão", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = state.baseUrl,
                    onValueChange = viewModel::updateBaseUrl,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("URL base HTTPS") },
                    singleLine = true,
                    supportingText = { Text("Não acrescente /chat/completions. /v1 não é inserido automaticamente.") }
                )
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("https://api.openai.com/v1", "https://openrouter.ai/api/v1", "https://api.groq.com/openai/v1").forEach { example ->
                        OutlinedButton(onClick = { viewModel.updateBaseUrl(example) }) { Text(when { example.contains("openrouter") -> "OpenRouter"; example.contains("groq") -> "Groq"; else -> "OpenAI" }) }
                    }
                }
                OutlinedTextField(
                    value = state.apiKeyInput?.value.orEmpty(),
                    onValueChange = viewModel::updateApiKey,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (state.hasSavedKey) "Nova API key (vazio mantém a salva)" else "API key") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrect = false),
                    visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = { TextButton(onClick = { showKey = !showKey }) { Text(if (showKey) "Ocultar" else "Mostrar") } }
                )
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Modelo", style = MaterialTheme.typography.titleLarge)
                androidx.compose.foundation.layout.Box {
                    OutlinedTextField(
                        value = state.model,
                        onValueChange = viewModel::updateModel,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Model ID") },
                        singleLine = true,
                        trailingIcon = { TextButton(onClick = { showModelMenu = !showModelMenu }) { Text("Modelos") } }
                    )
                    DropdownMenu(expanded = showModelMenu, onDismissRequest = { showModelMenu = false }) {
                        val matchingModels = viewModel.visibleModels()
                            .filter { it.contains(state.model, ignoreCase = true) }
                            .take(30)
                        if (matchingModels.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Nenhum modelo disponível. Teste a conexão ou informe o ID manualmente.") },
                                enabled = false,
                                onClick = {}
                            )
                        } else {
                            matchingModels.forEach { model ->
                                DropdownMenuItem(
                                    text = { Text(model) },
                                    onClick = { viewModel.updateModel(model); showModelMenu = false }
                                )
                            }
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = state.showAllModels, onCheckedChange = viewModel::toggleShowAll)
                    Text("Mostrar modelos de embedding, áudio e imagem")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = state.enabled, onCheckedChange = viewModel::toggleEnabled)
                    Text("Ativar leitura por IA")
                }
                when (val connection = state.connection) {
                    ConnectionState.Idle -> Unit
                    ConnectionState.Testing -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { CircularProgressIndicator(); Text("Testando conexão…") }
                    is ConnectionState.Success -> Text("Conectado · ${connection.modelCount} modelos · ${connection.elapsedMillis} ms", color = MaterialTheme.colorScheme.primary)
                    is ConnectionState.Error -> Text(connection.message, color = MaterialTheme.colorScheme.error)
                }
                state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                Button(
                    onClick = { if (state.hasConsent) viewModel.testConnection() else showConsent = true },
                    modifier = Modifier.fillMaxWidth(), enabled = state.connection != ConnectionState.Testing
                ) { Text("Testar conexão") }
                Button(onClick = viewModel::save, modifier = Modifier.fillMaxWidth(), enabled = !state.isSaving) {
                    if (state.isSaving) CircularProgressIndicator() else Text("Salvar")
                }
                if (state.hasSavedKey) OutlinedButton(onClick = { showRemoveConfirmation = true }, modifier = Modifier.fillMaxWidth()) { Text("Remover chave e configuração") }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Privacidade", style = MaterialTheme.typography.titleLarge)
                Text("Somente HTTPS. Testar a conexão envia a chave e o model ID; aprofundar envia também a pergunta (se houver) e os nomes, posições e orientações das cartas. O app não inclui ID de instalação ou telemetria no payload. Consulte a política de retenção do provedor. A chave fica criptografada com AES-GCM no Android Keystore e o arquivo de configurações é excluído de backup. Sem configuração e consentimento para o host atual, o app permanece offline.")
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}
