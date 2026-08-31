package com.pawedcat.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pawedcat.app.ServiceLocator
import com.pawedcat.app.playback.AudioPlaybackManager
import com.pawedcat.app.playback.model.SleepTimerMode
import com.pawedcat.app.ui.util.ShareUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingBottomBar(
    playbackManager: AudioPlaybackManager,
    modifier: Modifier = Modifier
) {
    val playbackState by playbackManager.playbackState.collectAsState()
    val episode = playbackState.currentEpisode ?: return
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showFullPlayer by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }

    val currentPos = playbackState.currentPositionMs
    val duration = playbackState.durationMs
    val progress = if (duration > 0) (currentPos.toFloat() / duration).coerceIn(0f, 1f) else 0f
    val speedSteps = listOf(0.5f, 0.8f, 1.0f, 1.2f, 1.5f, 1.75f, 2.0f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showFullPlayer = true },
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = episode.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "%s / %s".format(formatTime(currentPos), formatTime(duration)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = { playbackManager.skipBackward(15) }) {
                        Icon(Icons.Default.Replay10, contentDescription = "Back 15s")
                    }

                    FilledIconButton(
                        onClick = { playbackManager.togglePlayPause() },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    IconButton(onClick = { playbackManager.skipForward(30) }) {
                        Icon(Icons.Default.Forward30, contentDescription = "Forward 30s")
                    }
                }
            }
        }
    }

    if (showFullPlayer) {
        ModalBottomSheet(
            onDismissRequest = { showFullPlayer = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = episode.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                if (episode.description.isNotBlank()) {
                    Text(
                        text = episode.description.take(200) + if (episode.description.length > 200) "…" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Seekbar
                var sliderPosition by remember(currentPos) { mutableStateOf(currentPos.toFloat()) }
                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
                    onValueChangeFinished = { playbackManager.seekTo(sliderPosition.toLong()) },
                    valueRange = 0f..(if (duration > 0) duration.toFloat() else 1f),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = formatTime(sliderPosition.toLong()), style = MaterialTheme.typography.bodySmall)
                    Text(text = formatTime(duration), style = MaterialTheme.typography.bodySmall)
                }

                // Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                val podcastRepo = ServiceLocator.getInstance(context).podcastRepository
                                val podcast = podcastRepo.getPodcastById(episode.podcastId)
                                ShareUtils.shareEpisode(
                                    context = context,
                                    podcastTitle = podcast?.title ?: "",
                                    episodeTitle = episode.title,
                                    enclosureUrl = episode.enclosureUrl
                                )
                            }
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(28.dp))
                    }

                    IconButton(onClick = { playbackManager.skipBackward(15) }) {
                        Icon(Icons.Default.Replay10, contentDescription = "Back 15s", modifier = Modifier.size(32.dp))
                    }

                    FilledIconButton(
                        onClick = { playbackManager.togglePlayPause() },
                        modifier = Modifier.size(64.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    IconButton(onClick = { playbackManager.skipForward(30) }) {
                        Icon(Icons.Default.Forward30, contentDescription = "Forward 30s", modifier = Modifier.size(32.dp))
                    }

                    IconButton(onClick = { playbackManager.nextInQueue() }) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next in queue", modifier = Modifier.size(32.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Playback Speed Buttons
                val currentSpeed = playbackState.playbackSpeed
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Speed",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        speedSteps.forEach { speed ->
                            val isSelected = currentSpeed == speed
                            FilterChip(
                                selected = isSelected,
                                onClick = { playbackManager.setPlaybackSpeed(speed) },
                                label = {
                                    val label = if (speed == speed.toLong().toFloat()) {
                                        "${speed.toInt()}x"
                                    } else {
                                        "${speed}x"
                                    }
                                    Text(label, style = MaterialTheme.typography.labelMedium)
                                }
                            )
                        }
                    }
                }

                // Sleep Timer button
                OutlinedButton(
                    onClick = { showSleepTimerDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val timerLabel = when (val mode = playbackState.sleepTimerMode) {
                        is SleepTimerMode.Off -> "Sleep Timer: Off"
                        is SleepTimerMode.EndOfEpisode -> "Sleep Timer: End of Episode"
                        is SleepTimerMode.Minutes -> "Sleep Timer: %02d:%02d".format(mode.remainingSeconds / 60, mode.remainingSeconds % 60)
                    }
                    Icon(Icons.Default.Bedtime, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(timerLabel)
                }
            }
        }
    }

    if (showSleepTimerDialog) {
        SleepTimerDialog(
            currentMode = playbackState.sleepTimerMode,
            onSetMinutes = { playbackManager.setSleepTimerMinutes(it) },
            onSetEndOfEpisode = { playbackManager.setSleepTimerEndOfEpisode() },
            onCancelTimer = { playbackManager.cancelSleepTimer() },
            onDismiss = { showSleepTimerDialog = false }
        )
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
