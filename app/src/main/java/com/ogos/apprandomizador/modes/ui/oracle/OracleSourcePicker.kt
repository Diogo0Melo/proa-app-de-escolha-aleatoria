package com.ogos.apprandomizador.modes.ui.oracle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ogos.apprandomizador.R
import com.ogos.apprandomizador.core.model.ItemList

/** Escolha da fonte: o Sim ou Não clássico ou uma roleta do usuário. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OracleSourcePicker(
    sources: List<ItemList>,
    selectedRouletteId: Long?,
    onSelectClassic: () -> Unit,
    onSelectRoulette: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(R.string.oracle_source_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            SourceOption(
                title = stringResource(R.string.oracle_source_classic),
                subtitle = stringResource(R.string.oracle_source_classic_hint),
                selected = selectedRouletteId == null,
                onClick = {
                    onSelectClassic()
                    onDismiss()
                }
            )
            sources.forEach { list ->
                SourceOption(
                    title = list.topic.ifBlank { "—" },
                    subtitle = stringResource(R.string.oracle_source_items, list.items.size),
                    selected = list.id == selectedRouletteId,
                    onClick = {
                        onSelectRoulette(list.id)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun SourceOption(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
