# 17-background-playback-foreground-service-and-wakelock

Type: task
Status: resolved
Blocked by: 16

## Question

Why does playback stop after 5-6 minutes when the screen is locked and the phone is on battery, and how do we resolve it with proper Media3 foreground service lifecycles, network wake locks, and battery optimization settings?

## Answer

1. **Foreground Service Lifecycle**: Integrated explicit service startup `context.startService(Intent(context, PlaybackService::class.java))` in `AudioPlaybackManager.ensureServiceRunning()`. Media3 `MediaSessionService` automatically maintains the foreground service notification while audio is playing.
2. **ExoPlayer Network WakeMode & Locks**: Configured ExoPlayer with `setWakeMode(C.WAKE_MODE_NETWORK)` and added PowerManager `PARTIAL_WAKE_LOCK` + WifiManager `WIFI_MODE_FULL_LOW_LATENCY` / `WIFI_MODE_FULL` acquired upon playback and released upon pause/stop.
3. **Battery Optimization Diagnostic Card (`SettingsScreen.kt`)**: Added a dedicated card displaying current optimization status ("Optimized" vs "Unrestricted") and a one-tap button requesting unrestricted background execution (`ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`).
4. **Manifest Permissions**: Added `WAKE_LOCK` and `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` in `AndroidManifest.xml`.
5. **Verification**: Full unit test suite passing (`./gradlew testDebugUnitTest`).

