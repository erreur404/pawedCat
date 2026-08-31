package com.pawedcat.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.pawedcat.app.ServiceLocator
import com.pawedcat.app.ui.components.AddPodcastDialog
import com.pawedcat.app.ui.components.NowPlayingBottomBar
import com.pawedcat.app.ui.screens.DownloadsScreen
import com.pawedcat.app.ui.screens.PodcastDetailScreen
import com.pawedcat.app.ui.screens.PodcastsScreen
import com.pawedcat.app.ui.screens.QueueScreen
import com.pawedcat.app.ui.screens.SettingsScreen

enum class MainTab(val title: String, val icon: ImageVector) {
    PODCASTS("Podcasts", Icons.Default.Podcasts),
    DOWNLOADS("Downloads", Icons.Default.DownloadDone),
    QUEUE("Queue", Icons.Default.QueueMusic),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun MainScreen(
    serviceLocator: ServiceLocator,
    initialFeedUrl: String? = null
) {
    var selectedTab by remember { mutableStateOf(MainTab.PODCASTS) }
    var selectedPodcastId by remember { mutableStateOf<Long?>(null) }
    var incomingFeedUrl by remember { mutableStateOf(initialFeedUrl) }

    val playbackManager = serviceLocator.playbackManager
    val feedManager = serviceLocator.feedManager

    Scaffold(
        bottomBar = {
            Column {
                NowPlayingBottomBar(playbackManager = playbackManager)
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = NavigationBarDefaults.Elevation
                ) {
                    MainTab.values().forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab && selectedPodcastId == null,
                            onClick = {
                                selectedTab = tab
                                selectedPodcastId = null
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            if (selectedPodcastId != null) {
                PodcastDetailScreen(
                    podcastId = selectedPodcastId!!,
                    serviceLocator = serviceLocator,
                    onNavigateBack = { selectedPodcastId = null }
                )
            } else {
                when (selectedTab) {
                    MainTab.PODCASTS -> PodcastsScreen(
                        serviceLocator = serviceLocator,
                        onNavigateToPodcast = { id -> selectedPodcastId = id }
                    )
                    MainTab.DOWNLOADS -> DownloadsScreen(serviceLocator = serviceLocator)
                    MainTab.QUEUE -> QueueScreen(serviceLocator = serviceLocator)
                    MainTab.SETTINGS -> SettingsScreen(serviceLocator = serviceLocator)
                }
            }
        }
    }

    if (!incomingFeedUrl.isNullOrBlank()) {
        AddPodcastDialog(
            initialFeedUrl = incomingFeedUrl!!,
            onSearch = { query -> feedManager.searchDirectory(query) },
            onSubscribe = { url -> feedManager.subscribeToFeed(url) },
            onDismiss = { incomingFeedUrl = null }
        )
    }
}
