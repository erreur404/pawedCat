package com.pawedcat.app.playback

import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.pawedcat.app.ServiceLocator
import com.pawedcat.app.data.local.entity.EpisodeEntity
import com.pawedcat.app.playback.model.CurrentPlaybackState
import com.pawedcat.app.playback.model.SleepTimerMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class AudioPlaybackManager(
    private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val serviceLocator = ServiceLocator.getInstance(context)
    private val episodeRepo = serviceLocator.episodeRepository
    private val queueRepo = serviceLocator.queueRepository

    private var exoPlayer: ExoPlayer? = null
    private var progressTrackingJob: Job? = null
    private var sleepTimerJob: Job? = null

    private val _playbackState = MutableStateFlow(CurrentPlaybackState())
    val playbackState: StateFlow<CurrentPlaybackState> = _playbackState.asStateFlow()

    init {
        initPlayer()
        // Restore persisted playback speed
        scope.launch {
            val savedSpeed = serviceLocator.settingsRepository.playbackSpeedFlow.first()
            if (savedSpeed != 1.0f) {
                exoPlayer?.setPlaybackParameters(PlaybackParameters(savedSpeed))
                _playbackState.update { it.copy(playbackSpeed = savedSpeed) }
            }
        }
    }

    private fun initPlayer() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
            .build()

        exoPlayer = ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build().apply {
                addListener(playerListener)
            }
    }

    fun getPlayer(): ExoPlayer? = exoPlayer

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playbackState.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) {
                startProgressTracker()
            } else {
                stopProgressTracker()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    val duration = exoPlayer?.duration?.coerceAtLeast(0L) ?: 0L
                    _playbackState.update { it.copy(durationMs = duration) }
                }
                Player.STATE_ENDED -> {
                    handleEpisodeCompleted()
                }
                else -> {}
            }
        }
    }

    fun playNow(episodeId: Long) {
        scope.launch {
            queueRepo.playNow(episodeId)
            loadAndPlayEpisode(episodeId)
        }
    }

    fun playNext(episodeId: Long) {
        scope.launch {
            queueRepo.playNext(episodeId)
        }
    }

    fun addToQueue(episodeId: Long) {
        scope.launch {
            queueRepo.addToQueueEnd(episodeId)
        }
    }

    fun togglePlayPause() {
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                if (player.playbackState == Player.STATE_IDLE && _playbackState.value.currentEpisode != null) {
                    playNow(_playbackState.value.currentEpisode!!.id)
                } else {
                    player.play()
                }
            }
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        _playbackState.update { it.copy(currentPositionMs = positionMs) }
        scope.launch(Dispatchers.IO) {
            _playbackState.value.currentEpisode?.let { ep ->
                episodeRepo.updatePlaybackPosition(ep.id, positionMs, false)
            }
        }
    }

    fun skipForward(seconds: Int = 30) {
        val current = exoPlayer?.currentPosition ?: 0L
        val target = (current + seconds * 1000L).coerceAtMost(exoPlayer?.duration ?: Long.MAX_VALUE)
        seekTo(target)
    }

    fun skipBackward(seconds: Int = 15) {
        val current = exoPlayer?.currentPosition ?: 0L
        val target = (current - seconds * 1000L).coerceAtLeast(0L)
        seekTo(target)
    }

    fun nextInQueue() {
        scope.launch {
            val currentId = _playbackState.value.currentEpisode?.id
            if (currentId != null) {
                queueRepo.removeAndCompact(currentId)
            }
            advanceQueue()
        }
    }

    private suspend fun advanceQueue() {
        val nextItem = queueRepo.getCurrentQueueItem()
        if (nextItem != null) {
            loadAndPlayEpisode(nextItem.episodeId)
        } else {
            exoPlayer?.stop()
            _playbackState.update { CurrentPlaybackState() }
        }
    }

    private suspend fun loadAndPlayEpisode(episodeId: Long) {
        val episode = episodeRepo.getEpisodeById(episodeId) ?: return
        val player = exoPlayer ?: return

        val mediaUri = getPlayableUri(episode)
        val metadata = MediaMetadata.Builder()
            .setTitle(episode.title)
            .setDisplayTitle(episode.title)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(mediaUri)
            .setMediaId(episode.id.toString())
            .setMediaMetadata(metadata)
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()

        // Resume from saved position if valid (< 95% of duration)
        val resumePos = if (episode.durationMs > 0 && episode.playbackPositionMs >= (episode.durationMs * 0.95)) {
            0L
        } else {
            episode.playbackPositionMs
        }

        if (resumePos > 0) {
            player.seekTo(resumePos)
        }

        player.play()

        _playbackState.update {
            it.copy(
                currentEpisode = episode,
                isPlaying = true,
                currentPositionMs = resumePos,
                durationMs = episode.durationMs
            )
        }
    }

    private fun getPlayableUri(episode: EpisodeEntity): Uri {
        // If downloaded file exists, use local file URI
        episode.localFilePath?.let { path ->
            val file = File(path)
            if (file.exists() && file.length() > 0) {
                return Uri.fromFile(file)
            }
        }
        // Fallback to streaming enclosure URL
        return Uri.parse(episode.enclosureUrl)
    }

    private fun handleEpisodeCompleted() {
        scope.launch {
            val currentEpisode = _playbackState.value.currentEpisode ?: return@launch

            // Completion Cleanup: Mark played and delete local audio file
            episodeRepo.updatePlaybackPosition(currentEpisode.id, 0L, isPlayed = true)
            episodeRepo.deleteEpisodeAudioFile(currentEpisode.id)

            // Remove from queue
            queueRepo.removeAndCompact(currentEpisode.id)

            // Check Sleep Timer "End of Episode"
            if (_playbackState.value.sleepTimerMode is SleepTimerMode.EndOfEpisode) {
                cancelSleepTimer()
                exoPlayer?.stop()
                _playbackState.update { CurrentPlaybackState() }
                return@launch
            }

            advanceQueue()
        }
    }

    private fun startProgressTracker() {
        progressTrackingJob?.cancel()
        progressTrackingJob = scope.launch {
            while (isActive) {
                exoPlayer?.let { player ->
                    if (player.isPlaying) {
                        val pos = player.currentPosition
                        val dur = player.duration.coerceAtLeast(0L)
                        _playbackState.update {
                            it.copy(currentPositionMs = pos, durationMs = dur)
                        }

                        // Check >= 99% progress for automatic cleanup
                        if (dur > 10000L && pos >= (dur * 0.99)) {
                            handleEpisodeCompleted()
                            return@launch
                        }

                        // Save position to DB periodically
                        _playbackState.value.currentEpisode?.let { ep ->
                            episodeRepo.updatePlaybackPosition(ep.id, pos, false)
                        }
                    }
                }
                delay(1000L)
            }
        }
    }

    private fun stopProgressTracker() {
        progressTrackingJob?.cancel()
        progressTrackingJob = null
    }

    // Hard Sleep Timer
    fun setSleepTimerMinutes(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            cancelSleepTimer()
            return
        }

        var totalSeconds = minutes * 60
        _playbackState.update {
            it.copy(sleepTimerMode = SleepTimerMode.Minutes(minutes, totalSeconds))
        }

        sleepTimerJob = scope.launch {
            while (isActive && totalSeconds > 0) {
                delay(1000L)
                totalSeconds--
                _playbackState.update {
                    it.copy(sleepTimerMode = SleepTimerMode.Minutes(minutes, totalSeconds))
                }
            }

            // Timer Expired: Immediate Hard Stop (no fade-out)
            exoPlayer?.pause()
            _playbackState.update {
                it.copy(isPlaying = false, sleepTimerMode = SleepTimerMode.Off)
            }
        }
    }

    fun setSleepTimerEndOfEpisode() {
        sleepTimerJob?.cancel()
        _playbackState.update {
            it.copy(sleepTimerMode = SleepTimerMode.EndOfEpisode)
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _playbackState.update {
            it.copy(sleepTimerMode = SleepTimerMode.Off)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.5f, 3.0f)
        exoPlayer?.setPlaybackParameters(PlaybackParameters(clamped))
        _playbackState.update { it.copy(playbackSpeed = clamped) }
        scope.launch(Dispatchers.IO) {
            serviceLocator.settingsRepository.setPlaybackSpeed(clamped)
        }
    }

    fun release() {
        stopProgressTracker()
        cancelSleepTimer()
        exoPlayer?.release()
        exoPlayer = null
    }
}
