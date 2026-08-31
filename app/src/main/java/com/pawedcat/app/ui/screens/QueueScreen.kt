package com.pawedcat.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pawedcat.app.ServiceLocator
import com.pawedcat.app.data.local.entity.EpisodeEntity
import com.pawedcat.app.data.local.entity.QueueItemEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    serviceLocator: ServiceLocator
) {
    val queueRepo = serviceLocator.queueRepository
    val episodeRepo = serviceLocator.episodeRepository
    val playbackManager = serviceLocator.playbackManager

    val queueItems by queueRepo.getQueueItemsFlow().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Play Queue", style = MaterialTheme.typography.headlineMedium) },
                actions = {
                    if (queueItems.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    queueRepo.clearQueue()
                                }
                            }
                        ) {
                            Icon(Icons.Default.ClearAll, contentDescription = "Clear Queue")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        if (queueItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.QueueMusic, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Text("Queue is empty", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Add episodes to queue using \"Play Next\" or \"Add to Queue\".",
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
                itemsIndexed(queueItems, key = { _, item -> item.id }) { index, item ->
                    QueueItemRow(
                        queueItem = item,
                        index = index,
                        episodeRepo = episodeRepo,
                        onPlay = { playbackManager.playNow(item.episodeId) },
                        onRemove = {
                            coroutineScope.launch {
                                queueRepo.removeAndCompact(item.episodeId)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun QueueItemRow(
    queueItem: QueueItemEntity,
    index: Int,
    episodeRepo: com.pawedcat.app.data.repository.EpisodeRepository,
    onPlay: () -> Unit,
    onRemove: () -> Unit
) {
    val episode by episodeRepo.getEpisodeByIdFlow(queueItem.episodeId).collectAsState(initial = null)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay),
        colors = CardDefaults.cardColors(
            containerColor = if (index == 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Column {
                    Text(
                        text = episode?.title ?: "Loading…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (index == 0) {
                        Text(
                            text = "Up Next",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPlay) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play Now")
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Close, contentDescription = "Remove from Queue")
                }
            }
        }
    }
}
