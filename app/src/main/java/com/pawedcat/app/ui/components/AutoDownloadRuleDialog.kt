package com.pawedcat.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pawedcat.app.data.local.entity.AutoDownloadRuleEntity

@Composable
fun AutoDownloadRuleDialog(
    podcastTitle: String,
    initialRule: AutoDownloadRuleEntity?,
    podcastId: Long,
    onSaveRule: (AutoDownloadRuleEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var isEnabled by remember { mutableStateOf(initialRule?.isEnabled ?: true) }
    var regexText by remember { mutableStateOf(initialRule?.positiveRegex ?: ".*") }
    var maxCount by remember { mutableStateOf(initialRule?.maxRecentCount ?: 1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Auto-Download Rule", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Configure background downloads for \"$podcastTitle\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enable Auto-Download", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it }
                    )
                }

                if (isEnabled) {
                    OutlinedTextField(
                        value = regexText,
                        onValueChange = { regexText = it },
                        label = { Text("Positive Regex Pattern") },
                        placeholder = { Text("e.g. .* or Bonus|Interview") },
                        supportingText = { Text("Matches episode title (e.g. .* for all)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text(
                        text = "Max recent matching episodes to download: $maxCount",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1, 2, 3, 5).forEach { count ->
                            FilterChip(
                                selected = maxCount == count,
                                onClick = { maxCount = count },
                                label = { Text("$count") }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalRule = AutoDownloadRuleEntity(
                        podcastId = podcastId,
                        positiveRegex = regexText.ifBlank { ".*" },
                        maxRecentCount = maxCount,
                        isEnabled = isEnabled,
                        updatedAt = System.currentTimeMillis()
                    )
                    onSaveRule(finalRule)
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
