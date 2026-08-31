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

## Not yet specified

- **Android Auto / MediaBrowser support**: Extending MediaSession to car head units if requested.
- **Variable playback speed**: Pitch-corrected speed stepping (1.0x to 2.5x).
- **OPML bulk export/backup**: One-click export of subscribed feeds to an OPML file.

## Out of scope

- **Episode artwork & album thumbnails**: Ruled out to preserve zero memory footprint, fast rendering, and low bandwidth.
- **Multiple playlists**: Strictly single-queue architecture.
- **User accounts & cloud sync**: Entirely local-first and privacy-focused.
- **Audio fade-out on sleep timer**: Explicitly excluded; immediate cutoff requested.
