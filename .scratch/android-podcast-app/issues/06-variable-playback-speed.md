# 06-variable-playback-speed

Type: task
Status: resolved
Blocked by: 04, 05

## Question

How to add pitch-corrected variable playback speed (1.0×–2.5×) without external libraries, persisted across app restarts, with a minimal text-first UI?

## Answer

Used ExoPlayer's built-in `PlaybackParameters(speed)` (pitch is automatically corrected by the default audio processor chain). Speed is persisted in DataStore via `SettingsRepository.setPlaybackSpeed()` and restored on `AudioPlaybackManager` init by reading `playbackSpeedFlow.first()`.

UI: Selectable `FilterChip` buttons in the full-player `ModalBottomSheet` for direct 1-tap speed selection (`0.5x`, `0.8x`, `1x`, `1.2x`, `1.5x`, `1.75x`, `2x`). Persisted across app restarts.
