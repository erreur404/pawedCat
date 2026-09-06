package com.pawedcat.app.ui

import android.Manifest
import android.app.SearchManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.pawedcat.app.PawedCatApp
import com.pawedcat.app.ServiceLocator
import com.pawedcat.app.ui.theme.PawedCatTheme
import com.pawedcat.app.worker.AutoDownloadPeriodicWorker
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result handled (Media3 notification will be shown if granted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request runtime notification permission on Android 13+ (API 33+) for persistent media notification
        checkNotificationPermission()

        val app = application as PawedCatApp
        val serviceLocator = app.serviceLocator

        // Schedule periodic auto-download worker
        lifecycleScope.launch {
            AutoDownloadPeriodicWorker.schedule(applicationContext)
        }

        // Handle voice assistant playback commands (e.g. from Google Assistant / Gemini)
        handleVoiceSearchIntent(intent, serviceLocator)

        val incomingUrl = extractIncomingUrl(intent)

        setContent {
            PawedCatTheme(darkTheme = true) {
                MainScreen(
                    serviceLocator = serviceLocator,
                    initialFeedUrl = incomingUrl
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val app = application as PawedCatApp
        handleVoiceSearchIntent(intent, app.serviceLocator)
    }

    private fun handleVoiceSearchIntent(intent: Intent?, serviceLocator: ServiceLocator) {
        if (intent == null) return
        if (MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH == intent.action ||
            "android.media.action.MEDIA_PLAY_FROM_SEARCH" == intent.action
        ) {
            val query = intent.getStringExtra(SearchManager.QUERY)
                ?: intent.getStringExtra(MediaStore.EXTRA_MEDIA_TITLE)
                ?: intent.getStringExtra(MediaStore.EXTRA_MEDIA_ARTIST)
                ?: intent.getStringExtra(MediaStore.EXTRA_MEDIA_ALBUM)
                ?: intent.getStringExtra("query")
            lifecycleScope.launch {
                serviceLocator.playbackManager.playFromVoiceQuery(query)
            }
        }
    }

    private fun extractIncomingUrl(intent: Intent?): String? {
        if (intent == null) return null

        if (Intent.ACTION_VIEW == intent.action) {
            val data: Uri? = intent.data
            if (data != null) {
                var urlString = data.toString()
                if (urlString.startsWith("feed://", ignoreCase = true)) {
                    urlString = "https://" + urlString.substring(7)
                } else if (urlString.startsWith("pcast://", ignoreCase = true) || urlString.startsWith("itpc://", ignoreCase = true)) {
                    urlString = "https://" + urlString.substring(8)
                }
                return urlString
            }
        } else if (Intent.ACTION_SEND == intent.action && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrBlank()) {
                val match = Regex("""https?://[^\s]+""").find(sharedText)
                if (match != null) {
                    return match.value
                }
                return sharedText.trim()
            }
        }
        return null
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
