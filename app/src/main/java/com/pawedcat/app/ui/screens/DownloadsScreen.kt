package com.pawedcat.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pawedcat.app.ServiceLocator
import com.pawedcat.app.data.local.entity.EpisodeEntity
import kotlinx.coroutines.launch
import java.io.File

import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
import com.pawedcat.app.ui.util.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    serviceLocator: ServiceLocator
) {
    val episodeRepo = serviceLocator.episodeRepository
    val podcastRepo = serviceLocator.podcastRepository
    val downloadManager = serviceLocator.downloadManager
    val playbackManager = serviceLocator.playbackManager
    val context = LocalContext.current

    val downloadedEpisodes by episodeRepo.getDownloadedEpisodesFlow().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Downloads", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        if (downloadedEpisodes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.DownloadDone, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Text("No downloaded episodes", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Downloaded episodes will appear here for 100% offline listening.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(downloadedEpisodes, key = { it.id }) { episode ->
                    DownloadedEpisodeCard(
                        episode = episode,
                        onPlay = { playbackManager.playNow(episode.id) },
                        onShare = {
                            coroutineScope.launch {
                                val podcast = podcastRepo.getPodcastById(episode.podcastId)
                                ShareUtils.shareEpisode(
                                    context = context,
                                    podcastTitle = podcast?.title ?: "",
                                    episodeTitle = episode.title,
                                    enclosureUrl = episode.enclosureUrl
                                )
                            }
                        },
                        onDelete = {
                            coroutineScope.launch {
                                downloadManager.deleteDownload(episode.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadedEpisodeCard(
    episode: EpisodeEntity,
    onPlay: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val fileSizeMb = remember(episode.localFilePath) {
        episode.localFilePath?.let { path ->
            val f = File(path)
            if (f.exists()) "%.1f MB".format(f.length() / (1024f * 1024f)) else null
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = episode.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (fileSizeMb != null) {
                    Text(
                        text = "Downloaded ($fileSizeMb)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPlay) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete File", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
