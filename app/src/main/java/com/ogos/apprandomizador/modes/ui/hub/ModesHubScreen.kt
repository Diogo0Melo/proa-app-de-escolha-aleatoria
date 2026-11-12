package com.ogos.apprandomizador.modes.ui.hub

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ogos.apprandomizador.R
import com.ogos.apprandomizador.core.database.OracleModeStats
import com.ogos.apprandomizador.core.ui.format.TimeAgoFormatter
import com.ogos.apprandomizador.core.ui.theme.AppRandomizadorTheme

@Composable
fun ModesHubRoute(
    onModeSelected: (String) -> Unit,
    stats: Map<String, OracleModeStats>,
    modifier: Modifier = Modifier
) {
    ModesHubScreen(
        modes = ModeCatalog.entries,
        stats = stats,
        onModeSelected = onModeSelected,
        modifier = modifier
    )
}

@Composable
fun ModesHubScreen(
    modes: List<ModeEntry>,
    stats: Map<String, OracleModeStats>,
    onModeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.modes_hub_title), style = MaterialTheme.typography.headlineSmall)
                }
                Text(
                    stringResource(R.string.modes_hub_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(modes, key = ModeEntry::id) { mode ->
            val usage = stats[mode.id]
            val accessibilityLabel = stringResource(mode.titleRes) + ". " +
                stringResource(mode.descriptionRes) + ". " +
                stringResource(R.string.uses_count, usage?.uses ?: 0) + ". " +
                stringResource(R.string.last_use, TimeAgoFormatter.updateLastUse(usage?.lastUsedAt))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onModeSelected(mode.route) }
                    .testTag("mode-${mode.id}")
                    .semantics { contentDescription = accessibilityLabel },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = mode.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(stringResource(mode.titleRes), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(mode.descriptionRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            stringResource(R.string.uses_count, usage?.uses ?: 0),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            stringResource(R.string.last_use, TimeAgoFormatter.updateLastUse(usage?.lastUsedAt)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Hub de modos")
@Composable
private fun ModesHubPreview() {
    AppRandomizadorTheme {
        ModesHubScreen(
            modes = ModeCatalog.entries,
            stats = emptyMap(),
            onModeSelected = {}
        )
    }
}
