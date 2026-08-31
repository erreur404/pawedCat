# 01-project-scaffold-and-data-layer

Type: task
Status: resolved

## Question

How should the Android Gradle project, Room database entities (`Podcast`, `Episode`, `QueueItem`, `AutoDownloadRule`), DAOs, and repository layer be structured to support offline-first atomic operations, reliable playback state, and reactive Flow streams for Jetpack Compose?

## Answer

Established modern Android Gradle project structure with Compose BOM, Room 2.6.1, Media3 1.3.1, and WorkManager 2.9.0. Built Room schema with `podcasts`, `episodes`, `queue_items`, and `auto_download_rules` tables with foreign key cascading and indices. Created DAOs with atomic transactions for queue manipulation (`playNow`, `playNext`, `addToQueueEnd`, `removeAndCompact`), and implemented corresponding Repository interfaces + `ServiceLocator` container.
