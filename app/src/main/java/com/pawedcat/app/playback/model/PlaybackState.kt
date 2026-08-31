package com.pawedcat.app.playback.model

import com.pawedcat.app.data.local.entity.EpisodeEntity

sealed class SleepTimerMode {
    object Off : SleepTimerMode()
    object EndOfEpisode : SleepTimerMode()
    data class Minutes(val totalMinutes: Int, val remainingSeconds: Int) : SleepTimerMode()
}

data class CurrentPlaybackState(
    val currentEpisode: EpisodeEntity? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val sleepTimerMode: SleepTimerMode = SleepTimerMode.Off
)
