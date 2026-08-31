package com.pawedcat.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pawedcat.app.data.feed.model.PodcastSearchResult
import kotlinx.coroutines.launch

@Composable
fun AddPodcastDialog(
    initialFeedUrl: String = "",
    onSearch: suspend (String) -> List<PodcastSearchResult>,
    onSubscribe: suspend (String) -> Result<Any>,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(if (initialFeedUrl.isNotBlank()) 1 else 0) }
    var searchQuery by remember { mutableStateOf("") }
    var customUrl by remember { mutableStateOf(initialFeedUrl) }
    var searchResults by remember { mutableStateOf<List<PodcastSearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var isSubscribing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Podcast", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Search") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("RSS URL") }
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (selectedTab == 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search by title, topic…") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                        )
                        Button(
                            onClick = {
                                if (searchQuery.isNotBlank()) {
                                    coroutineScope.launch {
                                        isSearching = true
                                        errorMessage = null
                                        searchResults = onSearch(searchQuery)
                                        isSearching = false
                                        if (searchResults.isEmpty()) {
                                            errorMessage = "No podcasts found"
                                        }
                                    }
                                }
                            },
                            enabled = !isSearching && searchQuery.isNotBlank()
                        ) {
                            if (isSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Find")
                            }
                        }
                    }

                    if (isSubscribing) {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(searchResults) { result ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            coroutineScope.launch {
                                                isSubscribing = true
                                                errorMessage = null
                                                val res = onSubscribe(result.feedUrl)
                                                isSubscribing = false
                                                if (res.isSuccess) {
                                                    onDismiss()
                                                } else {
                                                    errorMessage = res.exceptionOrNull()?.message ?: "Failed to subscribe"
                                                }
                                            }
                                        },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = result.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (result.author.isNotBlank()) {
                                                Text(
                                                    text = result.author,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Icon(Icons.Default.Add, contentDescription = "Subscribe", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = customUrl,
                        onValueChange = { customUrl = it },
                        label = { Text("Feed URL") },
                        placeholder = { Text("https://example.com/feed.xml") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 3
                    )

                    Button(
                        onClick = {
                            if (customUrl.isNotBlank()) {
                                coroutineScope.launch {
                                    isSubscribing = true
                                    errorMessage = null
                                    val res = onSubscribe(customUrl)
                                    isSubscribing = false
                                    if (res.isSuccess) {
                                        onDismiss()
                                    } else {
                                        errorMessage = res.exceptionOrNull()?.message ?: "Failed to subscribe"
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSubscribing && customUrl.isNotBlank()
                    ) {
                        if (isSubscribing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Subscribe")
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
