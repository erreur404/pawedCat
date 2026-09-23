package com.pawedcat.app.ui.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

object AudioOutputUtils {
    private const val ACTION_MEDIA_OUTPUT = "com.android.settings.panel.action.MEDIA_OUTPUT"
    private const val EXTRA_PACKAGE_NAME = "com.android.settings.panel.extra.PACKAGE_NAME"

    /**
     * Opens Android's system Media Output Switcher (Android 10+ / API 29+)
     * or gracefully falls back to the Bluetooth Settings panel on older Android versions.
     */
    fun openAudioOutputSwitcher(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val intent = Intent(ACTION_MEDIA_OUTPUT).apply {
                    putExtra(EXTRA_PACKAGE_NAME, context.packageName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return
            } catch (_: Exception) {
                // Fall through to fallback
            }
        }


        // Fallback to Bluetooth settings
        try {
            val btIntent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(btIntent)
        } catch (_: Exception) {
            // General Settings fallback
            try {
                val generalIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(generalIntent)
            } catch (_: Exception) {}
        }
    }
}
