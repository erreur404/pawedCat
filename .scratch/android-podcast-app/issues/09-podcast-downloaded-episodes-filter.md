# 09-podcast-downloaded-episodes-filter

Type: task
Status: resolved
Blocked by: 05

## Question

How to provide a fast, 1-tap filter on the podcast details screen to quickly view only locally downloaded episodes?

## Answer

Added a `FilledIconToggleButton` displaying the canonical `Icons.Default.DownloadDone` icon directly to the right of the episode search bar in `PodcastDetailScreen`. 

When active, it dynamically filters the episode list to only show items where `downloadStatus == DownloadStatus.DOWNLOADED`. It also composes seamlessly with the search query filter so users can search within their downloaded episodes.
