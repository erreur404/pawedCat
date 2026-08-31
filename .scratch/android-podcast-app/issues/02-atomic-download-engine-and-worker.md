# 02-atomic-download-engine-and-worker

Type: task
Status: resolved
Blocked by: 01

## Question

How to implement an OkHttp + WorkManager download engine that guarantees zero corrupted files by writing to a temporary `.part` file, verifying complete HTTP streaming / Content-Length / non-empty streams before an atomic file rename, supporting cancellation, and enforcing configurable network constraints (Wi-Fi only vs Cellular)?

## Answer

Implemented `AtomicFileDownloader` with OkHttp streaming into `.part` temporary files, HTTP redirect handling, Content-Type checks (rejecting HTML error pages), Content-Length / minimum 10KB size verification, and atomic file renaming via `Files.move(StandardCopyOption.ATOMIC_MOVE)` / fallback rename. Built `EpisodeDownloadWorker` (WorkManager `CoroutineWorker`) and `PodcastDownloadManager` with network constraint switching (`NetworkType.UNMETERED` when Wi-Fi only is selected vs `NetworkType.CONNECTED`) and automatic cancellation/cleanup.
