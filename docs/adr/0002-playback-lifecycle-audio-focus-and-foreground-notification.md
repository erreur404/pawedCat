# ADR 0002: Playback Lifecycle, Audio Focus Timeout, and Android 14 Foreground Media Notification

## Status

Accepted

## Context

On modern Android devices (specifically Android 14 on Google Pixel 8), audio playback in PawedCat suffered from two interrelated bugs:
1. **Unintended Pause & Background Suspension**: Playback stopped unexpectedly when the device was locked. Android 14's power manager aggressively freezes background processes and restricts cellular/Wi-Fi data unless the service is backed by a valid, active foreground service notification with user-granted `POST_NOTIFICATIONS` runtime permissions. Because PawedCat lacked a runtime notification permission prompt and explicit `DefaultMediaNotificationProvider` channel integration in `PlaybackService`, the system suspended playback and network streaming.
2. **Ghost Auto-Resumption Hours Later**: When audio output was suppressed due to transient audio focus loss (e.g. from third-party media, navigation apps, or background web tabs), ExoPlayer retained `playWhenReady = true`. If the competing application held focus until Android's low-memory killer terminated it hours later, Android's `AudioManager` returned focus to PawedCat, which immediately resumed audio playback without user action.
3. **Redundant Streaming**: When playing undownloaded Episodes, users experienced network buffering during poor connectivity.

## Decision

We establish three architectural mechanisms governing audio playback and background lifecycle:

### 1. Transient Audio Focus 60-Second Timeout
- When ExoPlayer receives a transient audio focus loss (`AUDIOFOCUS_LOSS_TRANSIENT` or suppression), audio playback is paused and a 60-second watchdog timer starts.
- If audio focus is restored within 60 seconds (e.g. short GPS prompt or camera click), playback resumes automatically.
- If the 60-second window expires without focus restoration, PawedCat performs a definitive pause: sets `playWhenReady = false`, abandons audio focus with the system `AudioManager`, persists the current playback position to the database, and updates the media notification to a paused state. Subsequent restoration of audio focus by the OS will not trigger automatic playback.

### 2. Stream-to-Download Concurrent Hot-Swap
- Tapping Play on an undownloaded Episode immediately begins streaming playback via ExoPlayer and concurrently triggers an expedited background download of the Episode audio file via `PodcastDownloadManager`.
- When the download completes, if that Episode is still the active playing item, `AudioPlaybackManager` hot-swaps the underlying `MediaItem` from the remote HTTP URI to the local `file://` URI at the exact millisecond playback position, preserving playback state and preventing redundant full-file network streaming.

### 3. Android 14 Foreground Media Notification & Lock Screen Integration
- `PlaybackService` configures Media3's `DefaultMediaNotificationProvider` explicitly bound to `pawedcat_playback_channel` with actions: Seek Backward (-15s), Play/Pause, Seek Forward (+30s), and Next in Queue.
- The notification is marked ongoing while playing (preventing accidental dismissal) and dismissible when paused.
- The app requests the `POST_NOTIFICATIONS` runtime permission on Android 13+ (API 33+) so that Android's System UI renders the persistent media player both in the notification drawer and on the lock screen.

## Consequences

- Playback is fully protected against Android 14 Doze restrictions and background process throttling while the screen is locked.
- Ghost auto-resumptions hours later are completely eliminated.
- Users gain full lock screen and notification shade media controls (15s/30s skips, play/pause, next in queue).
- Undownloaded Episodes transition into saved local files automatically during initial playback.
