# Wayfinder Map: Lean Android Podcast App

## Destination

A complete, production-ready, lightweight native Android podcast app (Kotlin + Jetpack Compose + Media3/ExoPlayer + Room + WorkManager) featuring uncorrupted atomic downloads, regex auto-download rules, a persistent single queue, and zero-bloat text-first navigation.

## Notes

- **Language & Domain**: Refer to `CONTEXT.md` for canonical terms (`Podcast`, `Episode`, `Queue`, `Auto-Download Rule`, `Download Constraint`, `Sleep Timer`, `Completion Cleanup`).
- **Core Principles**:
  - **Zero file corruption**: Atomic writing via `.part` temp files, streaming verification, and clean error recovery.
  - **No bloat / No thumbnails**: Text-only, high-contrast, lightning-fast Jetpack Compose UI.
  - **Single Queue**: "Play now", "Play next", "Add to queue" without complex playlist machinery.
  - **Hard sleep timer**: Stop immediately after N minutes or at end of episode (no audio fade).
  - **Auto-cleanup**: Delete local audio files on reaching ≥99% playback or manual played mark.
  - **Network rules**: Download over Wi-Fi only or Wi-Fi + Cellular (configurable).
  - **Deep link integration**: Catch RSS feed URLs and podcast links via Android intent filters.

## Decisions so far

- [00-generate-app-icon](./issues/00-generate-app-icon.md): Generated 3-color manga calico cat icon wearing headphones for launcher and Play Store.
- [01-project-scaffold-and-data-layer](./issues/01-project-scaffold-and-data-layer.md): Android Gradle project structure, Room database entities, DAOs with atomic transactions, repositories, and ServiceLocator.
- [02-atomic-download-engine-and-worker](./issues/02-atomic-download-engine-and-worker.md): OkHttp atomic `.part` download engine, Content-Length & stream integrity checks, and WorkManager network constraint worker.
- [03-rss-parser-and-directory-search](./issues/03-rss-parser-and-directory-search.md): XmlPullParser RSS/Atom engine, iTunes Search API client, OPML import, and FeedManager auto-download triggers.
- [04-media3-audio-playback-service-and-queue](./issues/04-media3-audio-playback-service-and-queue.md): Media3 ExoPlayer service, single-queue mechanics, position persistence, completion cleanup, and zero-fade sleep timer.
- [05-compose-ui-and-regex-auto-download](./issues/05-compose-ui-and-regex-auto-download.md): Jetpack Compose text-first UI, bottom player sheet, sleep timer modal, auto-download regex dialog, OPML import, and intent filter deep linking.
- [06-variable-playback-speed](./issues/06-variable-playback-speed.md): ExoPlayer `PlaybackParameters` speed cycling (1.0×–2.5×), persisted in DataStore, surfaced as tap-to-cycle / long-press-to-reset button in the full-player sheet.
- [07-opml-export](./issues/07-opml-export.md): `OpmlExporter` writes OPML 2.0 via platform `XmlSerializer`; Settings Export card uses SAF `CreateDocument` for pick-and-save.
- [08-per-podcast-volume-boost](./issues/08-per-podcast-volume-boost.md): Per-podcast volume boost using Android DSP `LoudnessEnhancer` (0 dB to +10 dB slider in Podcast Settings dialog).
- [09-podcast-downloaded-episodes-filter](./issues/09-podcast-downloaded-episodes-filter.md): Dedicated 1-tap `DownloadDone` toggle button on the podcast details screen to filter and view only downloaded episodes.
- [10-podcast-and-episode-sharing](./issues/10-podcast-and-episode-sharing.md): Universal plain-text episode sharing with direct audio URLs and podcast feed sharing with PawedCat signature.
- [11-voice-assistant-playback-integration](./issues/11-voice-assistant-playback-integration.md): Google Assistant / Gemini voice search & playback integration (`MEDIA_PLAY_FROM_SEARCH`).
- [12-github-ci-release-pipeline](./issues/12-github-ci-release-pipeline.md): GitHub Actions CI workflow to build release APKs and publish GitHub Releases on tags.

## Not yet specified

- **Android Auto / MediaBrowser support**: Extending MediaSession to car head units if requested.

## Out of scope

- **Episode artwork & album thumbnails**: Ruled out to preserve zero memory footprint, fast rendering, and low bandwidth.
- **Multiple playlists**: Strictly single-queue architecture.
- **User accounts & cloud sync**: Entirely local-first and privacy-focused.
- **Audio fade-out on sleep timer**: Explicitly excluded; immediate cutoff requested.
