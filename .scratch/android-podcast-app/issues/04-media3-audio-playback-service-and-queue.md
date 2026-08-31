# 04-media3-audio-playback-service-and-queue

Type: task
Status: resolved
Blocked by: 01

## Question

How to implement the Media3 `MediaSessionService` / ExoPlayer audio engine with single-queue mechanics ("Play now", "Play next", "Add to queue"), persistent playback position tracking in Room, media notification controls, immediate Sleep Timer (hard stop after N minutes or end of episode), and Completion Cleanup (auto-delete audio file upon reaching ≥99% playback)?

## Answer

Built `PlaybackService` (`MediaSessionService`) with Android 8+ foreground notification channels and `ExoPlayer` configured with speech audio attributes. Implemented `AudioPlaybackManager` with StateFlow reactive updates, continuous Room playback position persistence, persistent single-queue execution, automatic Completion Cleanup (marking played and deleting local audio file on reaching ≥99% progress), and a zero-fade hard Sleep Timer (preset minutes or "End of episode").
