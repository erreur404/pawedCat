# 08-per-podcast-volume-boost

Type: task
Status: resolved
Blocked by: 04, 05

## Question

How to implement per-podcast volume boost to normalize quieter shows against system navigation (GPS) and phone audio without digital clipping?

## Answer

Integrated Android's hardware/DSP `android.media.audiofx.LoudnessEnhancer` attached to ExoPlayer's `audioSessionId`. The target gain is computed as `boostDb * 100` millibels (0 dB to +10 dB).

Stored `volumeBoostDb` in `PodcastEntity` (with Room `MIGRATION_1_2` schema migration). Surfaced a responsive 0 dB to +10 dB volume boost slider inside the Podcast Settings dialog (accessible via the top bar Tune icon, right below the auto-download regex parameters) to keep the main podcast screen clutter-free. Settings save to the database and dynamically adjust active audio gain in real time.
