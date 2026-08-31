package com.pawedcat.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pawedcat.app.data.local.entity.AutoDownloadRuleEntity
import kotlin.math.roundToInt

@Composable
fun AutoDownloadRuleDialog(
    podcastTitle: String,
    initialRule: AutoDownloadRuleEntity?,
    initialVolumeBoostDb: Int,
    podcastId: Long,
    onSave: (rule: AutoDownloadRuleEntity, volumeBoostDb: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var isEnabled by remember { mutableStateOf(initialRule?.isEnabled ?: true) }
    var regexText by remember { mutableStateOf(initialRule?.positiveRegex ?: ".*") }
    var maxCount by remember { mutableStateOf(initialRule?.maxRecentCount ?: 1) }
    var volumeBoostDb by remember { mutableStateOf(initialVolumeBoostDb) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Podcast Settings", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Settings for \"$podcastTitle\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Auto-Download Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Auto-Download", style = MaterialTheme.typography.titleMedium)
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

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Volume Boost Section (Below Regex)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = if (volumeBoostDb > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Text("Volume Boost", style = MaterialTheme.typography.titleMedium)
                        }
                        Text(
                            text = if (volumeBoostDb == 0) "Off (0 dB)" else "+$volumeBoostDb dB",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (volumeBoostDb > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "Amplify quieter shows to level with GPS navigation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Slider(
                        value = volumeBoostDb.toFloat(),
                        onValueChange = { volumeBoostDb = it.roundToInt() },
                        valueRange = 0f..10f,
                        steps = 9,
                        modifier = Modifier.fillMaxWidth()
                    )
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
                    onSave(finalRule, volumeBoostDb)
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

