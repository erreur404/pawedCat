# 05-compose-ui-and-regex-auto-download

Type: task
Status: resolved
Blocked by: 02, 03, 04

## Question

How to design and assemble the text-first, high-speed Jetpack Compose UI (Podcasts screen, Episode browser with 1-tap download, per-podcast regex auto-download configuration dialog, Queue management screen with reordering, Now Playing bar with sleep timer controls, Settings screen with Wi-Fi constraint toggle) and wire the periodic Auto-Download WorkManager worker?

## Answer

Built the text-first, high-contrast Jetpack Compose UI with dark theme styling. Created `PodcastsScreen` with iTunes search and RSS subscription dialogs, `PodcastDetailScreen` with positive regex auto-download configuration and 1-tap downloads, `DownloadsScreen` for offline access, `QueueScreen` for single-queue management, `SettingsScreen` with Wi-Fi only constraint toggle and OPML import, and `MainActivity` deep-link URL intent handling. Configured `AutoDownloadPeriodicWorker` with WorkManager for background sync.
