# 19-android14-background-playback-regression-and-session-lifecycle

Type: task
Status: resolved
Blocked by: 18

## Question

Why does background audio playback on Android 14 (Google Pixel 8) stop after approximately 1 minute when the app leaves focus, and how do we resolve the foreground service and session lifecycle issues?

## Answer

1. **Root Cause Analysis**:
   - `PlaybackService` built a `MediaSession` in `onCreate()` but never called `addSession(session)`. Under Media3, this left `MediaNotificationManager` detached from the session, so `startForeground(...)` was never called and no active foreground service status (`mediaPlayback`) was assigned to the service.
   - On Android 14, background processes without active foreground service status are frozen by the OS cgroup freezer after ~60 seconds.
   - Stream-to-download concurrent hot-swap dropped `isPlaying` during `setMediaItem()` and released wake locks prematurely right when downloads finished (~40-60s).

2. **Implementation**:
   - **Service Registration**: Called `addSession(session)` in `PlaybackService.onCreate()` and `removeSession(this)` in `onDestroy()`.
   - **Foreground Notification Sync**: Updated `onStartCommand` to trigger `onUpdateNotification(session, true)` when playback is active to satisfy Android 8+ foreground service requirements immediately upon `startForegroundService()`.
   - **Lock Protection During Hot-Swap**: Introduced `@Volatile var isHotSwapping = true` guard in `AudioPlaybackManager` to prevent releasing wake lock/wifi lock and interrupting progress tracking during stream-to-download transitions.
   - **Version Bump**: Bumped application minor version to `1.2.0` (versionCode `2`) in `gradle.properties` and `app/build.gradle.kts`.

3. **Verification**:
   - Verified with unit tests (`./gradlew testDebugUnitTest`) including Robolectric lifecycle validation.
   - Built debug APK (`./gradlew assembleDebug`).
