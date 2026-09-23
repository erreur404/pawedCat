package com.pawedcat.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pawedcat.app.ServiceLocator
import com.pawedcat.app.data.local.entity.DownloadStatus
import com.pawedcat.app.data.local.entity.EpisodeEntity
import com.pawedcat.app.ui.components.AutoDownloadRuleDialog
import com.pawedcat.app.ui.util.ShareUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastDetailScreen(
    podcastId: Long,
    serviceLocator: ServiceLocator,
    onNavigateBack: () -> Unit
) {
    val podcastRepo = serviceLocator.podcastRepository
    val episodeRepo = serviceLocator.episodeRepository
    val feedManager = serviceLocator.feedManager
    val downloadManager = serviceLocator.downloadManager
    val playbackManager = serviceLocator.playbackManager

    val podcast by podcastRepo.getPodcastByIdFlow(podcastId).collectAsState(initial = null)
    val autoDownloadRule by podcastRepo.getAutoDownloadRuleFlow(podcastId).collectAsState(initial = null)
    val episodes by episodeRepo.getEpisodesForPodcastFlow(podcastId).collectAsState(initial = emptyList())

    var isRefreshing by remember { mutableStateOf(false) }
    var showRuleDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var filterOnlyDownloaded by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val filteredEpisodes = remember(episodes, searchQuery, filterOnlyDownloaded) {
        episodes.filter { episode ->
            val matchesQuery = searchQuery.isBlank() || episode.title.contains(searchQuery, ignoreCase = true)
            val matchesDownloaded = !filterOnlyDownloaded || episode.downloadStatus == DownloadStatus.DOWNLOADED
            matchesQuery && matchesDownloaded
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        podcast?.title ?: "Podcast",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }

                },
                actions = {
                    IconButton(
                        onClick = {
                            if (podcast != null) {
                                ShareUtils.sharePodcast(context, podcast!!.title, podcast!!.feedUrl)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share Podcast")
                    }
                    IconButton(onClick = { showRuleDialog = true }) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Podcast Settings",
                            tint = if (autoDownloadRule?.isEnabled == true || (podcast?.volumeBoostDb ?: 0) > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                isRefreshing = true
                                feedManager.refreshPodcast(podcastId)
                                isRefreshing = false
                            }
                        },
                        enabled = !isRefreshing
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Feed")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Description & Search Filter
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                if (podcast?.author?.isNotBlank() == true) {
                    Text(
                        text = podcast!!.author,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                val statusNotes = mutableListOf<String>()
                if (autoDownloadRule?.isEnabled == true) {
                    statusNotes.add("Auto-Download: regex \"${autoDownloadRule!!.positiveRegex}\" (${autoDownloadRule!!.maxRecentCount})")
                }
                if ((podcast?.volumeBoostDb ?: 0) > 0) {
                    statusNotes.add("Boost: +${podcast!!.volumeBoostDb} dB")
                }
                if (statusNotes.isNotEmpty()) {
                    Text(
                        text = statusNotes.joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Filter episodes…") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    FilledIconToggleButton(
                        checked = filterOnlyDownloaded,
                        onCheckedChange = { filterOnlyDownloaded = it },
                        colors = IconButtonDefaults.filledIconToggleButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            checkedContentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.DownloadDone,
                            contentDescription = if (filterOnlyDownloaded) "Showing downloaded only" else "Filter downloaded"
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(filteredEpisodes, key = { it.id }) { episode ->
                    EpisodeCard(
                        episode = episode,
                        onPlayNow = { playbackManager.playNow(episode.id) },
                        onPlayNext = { playbackManager.playNext(episode.id) },
                        onAddToQueue = { playbackManager.addToQueue(episode.id) },
                        onShare = {
                            ShareUtils.shareEpisode(
                                context = context,
                                podcastTitle = podcast?.title ?: "",
                                episodeTitle = episode.title,
                                enclosureUrl = episode.enclosureUrl
                            )
                        },
                        onDownload = {
                            coroutineScope.launch {
                                downloadManager.enqueueDownload(episode.id)
                            }
                        },
                        onDeleteDownload = {
                            coroutineScope.launch {
                                downloadManager.deleteDownload(episode.id)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showRuleDialog && podcast != null) {
        AutoDownloadRuleDialog(
            podcastTitle = podcast!!.title,
            initialRule = autoDownloadRule,
            initialVolumeBoostDb = podcast!!.volumeBoostDb,
            podcastId = podcastId,
            onSave = { rule, boostDb ->
                coroutineScope.launch {
                    podcastRepo.saveAutoDownloadRule(rule)
                    podcastRepo.updateVolumeBoost(podcastId, boostDb)
                    playbackManager.onPodcastVolumeBoostChanged(podcastId, boostDb)
                }
            },
            onDismiss = { showRuleDialog = false }
        )
    }
}

@Composable
private fun EpisodeCard(
    episode: EpisodeEntity,
    onPlayNow: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit,
    onDeleteDownload: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (episode.isPlayed) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDate(episode.pubDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (episode.durationMs > 0) {
                    Text(
                        text = formatDuration(episode.durationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = episode.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (episode.isPlayed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = onPlayNow,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Play")
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Queue Options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Play Next") },
                                onClick = {
                                    onPlayNext()
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Add to Queue") },
                                onClick = {
                                    onAddToQueue()
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Share") },
                                onClick = {
                                    onShare()
                                    showMenu = false
                                }
                            )
                        }
                    }
                }

                // Download Status / Action Button
                when (episode.downloadStatus) {
                    DownloadStatus.DOWNLOADED -> {
                        IconButton(onClick = onDeleteDownload) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Downloaded (tap to delete)", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    DownloadStatus.DOWNLOADING, DownloadStatus.QUEUED -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                progress = { (episode.downloadProgress / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${episode.downloadProgress}%", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    else -> {
                        IconButton(onClick = onDownload) {
                            Icon(Icons.Default.Download, contentDescription = "Download")
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(ms: Long): String {
    if (ms <= 0L) return ""
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(ms))
}

private fun formatDuration(ms: Long): String {
    val totalMins = ms / 60000
    val hours = totalMins / 60
    val mins = totalMins % 60
    return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
}
