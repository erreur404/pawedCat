package com.pawedcat.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Wifi

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pawedcat.app.ServiceLocator
import com.pawedcat.app.data.feed.OpmlExporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    serviceLocator: ServiceLocator
) {
    val settingsRepo = serviceLocator.settingsRepository
    val feedManager = serviceLocator.feedManager
    val podcastRepo = serviceLocator.podcastRepository
    val context = LocalContext.current

    val wifiOnly by settingsRepo.downloadOnWifiOnlyFlow.collectAsState(initial = true)
    val defaultSleepTimer by settingsRepo.defaultSleepTimerMinutesFlow.collectAsState(initial = 30)

    val coroutineScope = rememberCoroutineScope()
    var importStatusMessage by remember { mutableStateOf<String?>(null) }
    var isImporting by remember { mutableStateOf(false) }
    var exportStatusMessage by remember { mutableStateOf<String?>(null) }
    var isExporting by remember { mutableStateOf(false) }

    val opmlLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isImporting = true
                importStatusMessage = null
                try {
                    val stream = context.contentResolver.openInputStream(uri)
                    if (stream != null) {
                        val feeds = feedManager.importOpml(stream)
                        var importedCount = 0
                        for (f in feeds) {
                            val res = feedManager.subscribeToFeed(f.feedUrl)
                            if (res.isSuccess) importedCount++
                        }
                        importStatusMessage = "Successfully imported $importedCount podcasts!"
                    } else {
                        importStatusMessage = "Failed to read selected file"
                    }
                } catch (e: Exception) {
                    importStatusMessage = "Import failed: ${e.message}"
                } finally {
                    isImporting = false
                }
            }
        }
    }

    val opmlExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/x-opml")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isExporting = true
                exportStatusMessage = null
                try {
                    val podcasts = podcastRepo.getAllPodcasts()
                    val stream = context.contentResolver.openOutputStream(uri)
                    if (stream != null) {
                        OpmlExporter().export(podcasts, stream)
                        stream.close()
                        exportStatusMessage = "Exported ${podcasts.size} podcast${if (podcasts.size == 1) "" else "s"}"
                    } else {
                        exportStatusMessage = "Could not open file for writing"
                    }
                } catch (e: Exception) {
                    exportStatusMessage = "Export failed: ${e.message}"
                } finally {
                    isExporting = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Network Constraints
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Wifi, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text("Download on Wi-Fi only", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Prevent background downloads over cellular data",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = wifiOnly,
                        onCheckedChange = { checked ->
                            coroutineScope.launch {
                                settingsRepo.setDownloadOnWifiOnly(checked)
                            }
                        }
                    )
                }
            }

            // OPML Import
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text("Import Subscriptions (OPML)", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Import feeds from Castbox, Pocket Casts, AntennaPod, etc.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (importStatusMessage != null) {
                        Text(
                            text = importStatusMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = { opmlLauncher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isImporting
                    ) {
                        if (isImporting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Select OPML File")
                        }
                    }
                }
            }

            // OPML Export
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text("Export Subscriptions (OPML)", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Back up feeds — import on any app that supports OPML",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (exportStatusMessage != null) {
                        Text(
                            text = exportStatusMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedButton(
                        onClick = { opmlExportLauncher.launch("pawedcat-subscriptions.opml") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isExporting
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Export to OPML File")
                        }
                    }
                }
            }

            // Background Playback & Battery Optimization
            val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as? PowerManager }
            var isIgnoringBatteryOptimizations by remember {
                mutableStateOf(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && powerManager != null) {
                        powerManager.isIgnoringBatteryOptimizations(context.packageName)
                    } else true
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            if (isIgnoringBatteryOptimizations) Icons.Default.BatteryChargingFull else Icons.Default.BatteryAlert,
                            contentDescription = null,
                            tint = if (isIgnoringBatteryOptimizations) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        Column {
                            Text("Background Playback & Battery", style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (isIgnoringBatteryOptimizations)
                                    "Unrestricted: background playback protected during screen lock"
                                else
                                    "Optimized: Android may pause playback after 5 min when locked on battery",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!isIgnoringBatteryOptimizations && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    try {
                                        val altIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(altIntent)
                                    } catch (_: Exception) {}
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Allow Unrestricted Background Playback")
                        }
                    }
                }
            }

            // About PawedCat
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("About PawedCat", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "PawedCat is a pun on Podcast. Built for zero bloat, zero ads, atomic corruption-proof downloads, regex auto-download filters, and hard sleep timer auto-stops.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Version 1.0.0 (Native Kotlin + Jetpack Compose + Media3)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
