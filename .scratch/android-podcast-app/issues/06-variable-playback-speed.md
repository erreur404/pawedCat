# 06-variable-playback-speed

Type: task
Status: resolved
Blocked by: 04, 05

## Question

How to add pitch-corrected variable playback speed (1.0×–2.5×) without external libraries, persisted across app restarts, with a minimal text-first UI?

## Answer

Used ExoPlayer's built-in `PlaybackParameters(speed)` (pitch is automatically corrected by the default audio processor chain). Speed is persisted in DataStore via `SettingsRepository.setPlaybackSpeed()` and restored on `AudioPlaybackManager` init by reading `playbackSpeedFlow.first()`.

UI: an `OutlinedButton` in the full-player `ModalBottomSheet` displays the current speed (e.g. `1.5×`). Tap cycles through six steps: `1.0× → 1.2× → 1.5× → 1.75× → 2.0× → 2.5×`. Long-press resets to 1.0×. Requires `@ExperimentalFoundationApi` for `combinedClickable`.
