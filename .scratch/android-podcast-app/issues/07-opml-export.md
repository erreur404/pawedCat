# 07-opml-export

Type: task
Status: resolved
Blocked by: 03, 05

## Question

How to export all subscribed podcasts as a portable OPML file without any external XML library, letting users migrate away freely?

## Answer

Created `OpmlExporter` (pure class, no dependencies) that writes a valid OPML 2.0 document using the platform's `XmlSerializer` from `android.util.Xml`. Each `PodcastEntity` becomes one `<outline type="rss">` element with `title`, `xmlUrl`, and `htmlUrl`.

UI: an **Export Subscriptions (OPML)** card in `SettingsScreen`, directly below the existing Import card. Uses `ActivityResultContracts.CreateDocument("text/x-opml")` so Android shows the system file-picker pre-filled with `pawedcat-subscriptions.opml`. After writing, a status line reports the exported count. The exported file passes round-trip import via the existing `OpmlParser`.
