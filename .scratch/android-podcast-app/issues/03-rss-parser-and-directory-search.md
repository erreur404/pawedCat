# 03-rss-parser-and-directory-search

Type: task
Status: resolved
Blocked by: 01

## Question

How to implement a lightweight, robust streaming XML RSS/Atom parser (handling edge cases, enclosures, and varied podcast metadata formats), the iTunes Podcast Search API client, OPML import support, and Android `ACTION_VIEW` / `ACTION_SEND` intent filters to subscribe to podcast links directly from browsers and other apps?

## Answer

Implemented zero-dependency streaming XML `RssFeedParser` using Android's native `XmlPullParser` supporting RSS 2.0, Atom, iTunes extensions, multiple date/duration formats, and enclosure metadata. Created `PodcastDirectoryService` for fast iTunes Search API queries, `OpmlParser` for subscription backup imports, and `FeedManager` to handle feed caching, subscription persistence, and positive regex auto-download evaluation.
