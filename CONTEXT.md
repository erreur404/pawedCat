# PawedCat

A lightweight, distraction-free native Android podcast client focused on reliable audio downloads, regex auto-downloads, single-queue playback, and clean text-first navigation.

## Language

**Podcast**:
A syndicated audio show defined by an RSS feed URL, metadata, and collection of episodes.
_Avoid_: Show, channel, subscription, series

**Episode**:
An individual audio item belonging to a Podcast, with an enclosure URL, publication date, and local playback state.
_Avoid_: Track, audio file, entry, item

**Queue**:
The single, ordered sequence of Episodes scheduled for sequential playback.
_Avoid_: Playlist, play list, tracklist, upcoming list

**Auto-Download Rule**:
A per-Podcast positive regex filter and capacity limit (N) determining which new Episodes are saved to local storage.
_Avoid_: Auto-fetcher, sync rule, filter rule, smart download

**Download Constraint**:
A network requirement (Wi-Fi only vs. any network) gating background and manual episode downloads.
_Avoid_: Network policy, data saver, connection mode

**Sleep Timer**:
A countdown timer or end-of-episode trigger that stops audio playback immediately upon expiring.
_Avoid_: Auto-pause, bed timer, sleep mode

**Completion Cleanup**:
The automatic deletion of an Episode's local audio file once playback reaches 99% progress or when manually marked played.
_Avoid_: Auto-purge, garbage collection, storage cleaner
