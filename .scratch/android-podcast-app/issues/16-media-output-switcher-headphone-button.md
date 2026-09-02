# 16-media-output-switcher-headphone-button

Type: task
Status: ready-for-agent
Blocked by: 15

## Question

How do we provide a direct playback device switcher (Headphone icon) in both the mini-player bar and expanded full player sheet, triggering the native Android output switcher with Bluetooth fallback?

## Scope & Implementation

1. **Headphone Button in UI**:
   - Add a headphone icon button (`Icons.Default.Headphones`) to `NowPlayingBottomBar.kt` in the mini-bar (beside play controls) and in the expanded full player sheet.
2. **Audio Output Switcher Helper**:
   - Implement `AudioOutputUtils.openAudioOutputSwitcher(context)` that invokes `Settings.Panel.ACTION_MEDIA_OUTPUT` (with extras for current package name / media session token on API 29+ / 30+).
   - Gracefully fallback to `ACTION_BLUETOOTH_SETTINGS` on older Android versions (< API 29).
3. **Verification**:
   - UI and interaction tests verifying the headphone icon triggers the output intent.
