# 18-bluetooth-headset-media-controls

Type: task
Status: resolved
Blocked by: 17

## Question

Why was the Bluetooth headset "pause" command not registered by the app, and how do we ensure all Bluetooth media buttons and podcast-optimized fast-forward/rewind actions are properly handled?

## Answer

1. **MediaSession Binding**: Attached active MediaSession in `PlaybackService` with `setSessionActivity` PendingIntent and `android.intent.action.MEDIA_BUTTON` intent filter in `AndroidManifest.xml`.
2. **Podcast-Optimized Bluetooth Action Routing**: Implemented `MediaSession.Callback` intercepting `Player.COMMAND_SEEK_TO_NEXT` and `Player.COMMAND_SEEK_TO_PREVIOUS` to route Bluetooth hardware next / double-click to skip 30s forward and prev to skip 15s back.
3. **Verification**: Successfully compiled, verified all unit tests (`./gradlew testDebugUnitTest`), and built debug APK (`./gradlew assembleDebug`).

