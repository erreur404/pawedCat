# 11-voice-assistant-playback-integration

Type: task
Status: resolved
Blocked by: 04, 05

## Question

How to integrate hands-free voice playback control with Google Assistant and Gemini for safe in-car listening?

## Answer

Registered the standard Android `android.media.action.MEDIA_PLAY_FROM_SEARCH` intent filter in `AndroidManifest.xml` and routed search queries through `AudioPlaybackManager.playFromVoiceQuery()`:
1. **Empty Query**: Resumes current playing episode, plays first item in the play queue, or starts the latest downloaded episode.
2. **Subscribed Podcast Title/Author Match**: Finds the show in the user's library and plays the latest published episode.
3. **Episode Title Match**: Searches local library by title and plays the match.
4. **Online Directory Fallback**: If the show isn't subscribed yet, searches the podcast directory, subscribes, and starts streaming/playing immediately hands-free.
