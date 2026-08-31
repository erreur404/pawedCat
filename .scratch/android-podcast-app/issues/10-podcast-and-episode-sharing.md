# 10-podcast-and-episode-sharing

Type: task
Status: resolved
Blocked by: 04, 05

## Question

How to implement zero-friction universal podcast and episode sharing without proprietary lock-in, enabling recipients to play episodes immediately anywhere while gently aiding PawedCat app discovery?

## Answer

Created `ShareUtils` with two sharing mechanics:
1. **Universal Episode Sharing**: Shares `Podcast Title — "Episode Title"` followed by the direct audio enclosure URL (`https://.../episode.mp3`). Playable immediately in any messaging app, desktop browser, or media player with no installation required. Surfaced in:
   - Episode three-dot dropdown menu (`"Share"`) in `PodcastDetailScreen`.
   - Dedicated `Share` button on downloaded cards in `DownloadsScreen`.
   - Dedicated `Share` button in the full-player sheet controls row in `NowPlayingBottomBar`.
2. **Podcast Feed Sharing**: Shares `Podcast: "<Title>"` with raw RSS feed URL and the discovery signature `(Open in PawedCat — zero-bloat, ad-free podcast player)`. Surfaced in the TopAppBar of `PodcastDetailScreen`.
