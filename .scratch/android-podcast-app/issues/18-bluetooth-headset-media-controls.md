# 18-bluetooth-headset-media-controls

Type: task
Status: ready-for-agent
Blocked by: 17

## Question

Why was the Bluetooth headset "pause" command not registered by the app, and how do we ensure all Bluetooth media buttons and podcast-optimized fast-forward/rewind actions are properly handled?

## Scope & Implementation

1. **MediaSession Registration**:
   - Register active `MediaSession` with `PlaybackService`, setting `SessionActivity` PendingIntent to launch `MainActivity`.
   - Add `<intent-filter>` for `android.intent.action.MEDIA_BUTTON` to `PlaybackService` in `AndroidManifest.xml`.
2. **Media Button Actions**:
   - Handle play/pause, media button intents, and transport controls.
   - Configure Custom / Forward / Rewind command callbacks or player command routing so Bluetooth headset double-click / next jumps forward 30s and previous jumps back 15s.
3. **Verification**:
   - Test MediaSession commands and headset key event dispatching.
