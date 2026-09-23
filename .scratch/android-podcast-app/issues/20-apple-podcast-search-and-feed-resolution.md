# 20-apple-podcast-search-and-feed-resolution

Type: task
Status: resolved
Blocked by: 03

## Question

How to ensure Apple Podcasts search works comprehensively (including for shows like France Inter's "La Terre au carré" / Radio France whose search JSON omits direct `feedUrl`), enable seamless Apple Podcasts URL resolution when subscribing, and provide Fyyd as an automatic secondary directory fallback?

## Answer

Updated `PodcastDirectoryService` and `FeedManager`:
1. When Apple Podcasts (iTunes) search returns results that lack direct `feedUrl` fields (e.g. Radio France / France Inter podcasts like "La Terre au carré"), we retain the results and concurrently resolve the underlying RSS feed URLs by querying the Apple Podcasts show page metadata (matching the embedded `feedUrl` property).
2. Added `resolveApplePodcastFeedUrl` and `isApplePodcastsUrl` to allow direct subscription from Apple Podcasts links (e.g., `https://podcasts.apple.com/.../id294060079`) via `FeedManager.fetchFeed()` and `FeedManager.subscribeToFeed()`.
3. Added Fyyd directory query fallback if Apple Podcasts returns zero results or errors.
4. Updated `AddPodcastDialog` UI hints and labels to indicate support for Apple Podcasts links alongside raw RSS URLs.
5. Added comprehensive Robolectric unit tests in `PodcastDirectoryServiceTest` verifying search and feed URL resolution.
