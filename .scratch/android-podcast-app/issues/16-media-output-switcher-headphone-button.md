# 16-media-output-switcher-headphone-button

Type: task
Status: resolved
Blocked by: 15

## Question

How do we provide a direct playback device switcher (Headphone icon) in both the mini-player bar and expanded full player sheet, triggering the native Android output switcher with Bluetooth fallback?

## Answer

1. **Audio Output Switcher Helper (`AudioOutputUtils.kt`)**: Created helper invoking Android's system output switcher (`com.android.settings.panel.action.MEDIA_OUTPUT`) on Android 10+ (API 29+) with package name extras, with graceful fallback to `Settings.ACTION_BLUETOOTH_SETTINGS`.
2. **Headphone Button in Mini-Player & Full Player Sheet (`NowPlayingBottomBar.kt`)**: Added `IconButton(Icons.Default.Headphones)` to both the persistent mini-player bar and the full modal bottom sheet player controls row.
3. **Verification**: Verified compilation and unit test suite passing (`./gradlew testDebugUnitTest`).

