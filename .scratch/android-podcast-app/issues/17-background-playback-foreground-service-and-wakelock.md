# 17-background-playback-foreground-service-and-wakelock

Type: task
Status: ready-for-agent
Blocked by: 16

## Question

Why does playback stop after 5-6 minutes when the screen is locked and the phone is on battery, and how do we resolve it with proper Media3 foreground service lifecycles, network wake locks, and battery optimization settings?

## Scope & Implementation

1. **Foreground Service Lifecycle**:
   - Ensure `PlaybackService` is properly started and active as a `FOREGROUND_SERVICE_MEDIA_PLAYBACK` foreground service whenever audio begins.
   - Maintain media notification while playing or paused in foreground.
2. **ExoPlayer WakeLock & Wi-Fi Lock**:
   - Configure `ExoPlayer` builder with `setWakeMode(C.WAKE_MODE_NETWORK)`.
   - Acquire and release CPU `WakeLock` / `WifiLock` during streaming playback to prevent OS Doze and CPU sleep.
3. **Battery Optimization Settings UI**:
   - Add a "Background Playback & Battery" card/preference in `SettingsScreen.kt`.
   - Show status ("Optimized" vs "Unrestricted") and a one-tap button to launch `Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`.
4. **Verification**:
   - Verify foreground service starts properly and player holds wake lock during playback.
