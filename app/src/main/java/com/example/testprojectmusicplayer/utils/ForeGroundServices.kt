package com.example.testprojectmusicplayer.utils

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AudioPlaybackService : Service() {

    private val notificationChannelId = "MEDIA_PLAYBACK_CHANNEL"
    private val serviceId = 1
    private lateinit var notificationManager: NotificationManager
    private lateinit var notification: Notification
    private lateinit var mediaSession: MediaSessionCompat
    private var mediaPlayer: MediaPlayer? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var playbackCallback: PlaybackCallback? = null
    private var audioFiles: List<Song> = emptyList()
    private var currentSongIndex = 0

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): AudioPlaybackService = this@AudioPlaybackService
    }

    fun updateSongList(songs: List<Song>) {
        audioFiles = songs
    }

    interface PlaybackCallback {
        fun onPlaybackPositionChanged(position: Int)
        fun onPlaybackCompleted()
        fun onPlaybackStopped()
        fun onPlaybackError(error: String)
        fun onSongChanged(song: Song)
        fun onIndexChanged(index: Int)
        fun onPlaybackStateChanged(isPlaying: Boolean)
    }

    override fun onCreate() {
        super.onCreate()

       // setPlaybackCallback()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()


        setupMediaPlayer()
        createNotificationChannel()

        mediaSession = MediaSessionCompat(this, "AudioPlaybackService").apply {
            setCallback(mediaSessionCallback)
            isActive = true
        }

        registerReceiver(becomingNoisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
        updateNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(serviceId, notification)

        when (intent?.action) {
            ACTION_PLAY -> {
                if (mediaPlayer?.isPlaying == true) {
                    resumePlayback()
                } else {
                    playSong(currentSongIndex)
                }
            }
            ACTION_PAUSE -> pausePlayback()
            ACTION_NEXT -> nextSong()
            ACTION_PREVIOUS -> previousSong()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
        mediaSession.release()
        unregisterReceiver(becomingNoisyReceiver)
    }

    fun setPlaybackCallback(callback: PlaybackCallback) {
        this.playbackCallback = callback
    }

    fun playSong(index: Int) {
        if (audioFiles.isNotEmpty()) {
            serviceScope.launch {
                try {
                    val newSong = audioFiles[index]
                    val currentSong = mediaPlayer?.let {
                        it.isPlaying || it.currentPosition > 0
                    } == true && audioFiles.getOrNull(currentSongIndex)?.songId == newSong.songId


                    if (currentSong && mediaPlayer?.isPlaying == false) {
                        resumePlayback()
//                        mediaPlayer?.start()
//                        notifyPlaybackStateChanged(true)
//                        updateNotification()
                        return@launch
                    }
                    else{
                        mediaPlayer?.reset()
                        mediaPlayer?.setDataSource(newSong.audioFile)
                        mediaPlayer?.prepareAsync()

                        mediaPlayer?.setOnPreparedListener {
                            it.start()
                            notifyPlaybackStateChanged(true)
                            notifyPositionChange()
                            updateNotification()
                        }

                        mediaPlayer?.setOnCompletionListener {
                            nextSong()
                           // notifyPlaybackCompleted()
                        }

                        mediaPlayer?.setOnErrorListener { _, what, extra ->
                            handlePlaybackError("Error code: $what Extra code: $extra")
                            true
                        }

                        notifySongChanged(newSong)
                        currentSongIndex = index

                    }



                } catch (e: Exception) {
                    Log.e("AudioPlaybackService", "Error playing song: ${e.message}")
                    notifyPlaybackStateChanged(false)
                    handlePlaybackError(e.message.toString())
                }
            }
        }
    }

    fun pausePlayback() {
        mediaPlayer?.takeIf { it.isPlaying }?.pause()
        notifyPlaybackStateChanged(false)
        updateNotification()
    }

    fun resumePlayback() {
        mediaPlayer?.takeIf { !it.isPlaying }?.start()
        notifyPlaybackStateChanged(true)
        updateNotification()
    }

    fun stopPlayback() {
        mediaPlayer?.takeIf { it.isPlaying }?.stop()
        notifyPlaybackStopped()
        notifyPlaybackStateChanged(false)
        updateNotification()
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
        notifyPositionChange()
    }

    fun nextSong() {
        if (currentSongIndex < audioFiles.size - 1) {
            currentSongIndex++
            notifySongChanged(audioFiles[currentSongIndex])
            notifyIndexChanged(currentSongIndex)

            playSong(currentSongIndex)
        } else {
            currentSongIndex = 0
            playSong(currentSongIndex)
            notifySongChanged(audioFiles[currentSongIndex])
            notifyIndexChanged(currentSongIndex)
        }
    }

    fun previousSong() {
        if (currentSongIndex > 0) {
            currentSongIndex--
            notifySongChanged(audioFiles[currentSongIndex])
            notifyIndexChanged(currentSongIndex)
            playSong(currentSongIndex)
        }
        else{
           currentSongIndex = 0
            playSong(currentSongIndex)
            notifySongChanged(audioFiles[currentSongIndex])
            notifyIndexChanged(currentSongIndex)

        }
    }

    private fun updatePlaybackState(state: Int) {
        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(state, mediaPlayer?.currentPosition?.toLong() ?: 0L, 1.0f)
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
                .build()
        )
    }

    private fun setupMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setOnCompletionListener {
                nextSong()
                //notifyPlaybackCompleted()
            }
            setOnSeekCompleteListener {
                notifyPositionChange()
            }
        }
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun notifySongChanged(song: Song) {
        playbackCallback?.onSongChanged(song)
        Log.d("SongChanged", song.toString())
    }

    private fun notifyIndexChanged(index: Int) {
        playbackCallback?.onIndexChanged(index)
    }

    private fun notifyPlaybackStateChanged(isPlaying: Boolean) {
        playbackCallback?.onPlaybackStateChanged(isPlaying)
    }

    private fun notifyPlaybackCompleted() {
        playbackCallback?.onPlaybackCompleted()
    }

    private fun notifyPlaybackStopped() {
        playbackCallback?.onPlaybackStopped()
    }

    private fun handlePlaybackError(error: String) {
        playbackCallback?.onPlaybackError(error)
    }

    private fun notifyPositionChange() {

        serviceScope.launch {
            while (mediaPlayer?.isPlaying == true) {
                val position = mediaPlayer?.currentPosition
                playbackCallback?.onPlaybackPositionChanged(position?:0)
                delay(1000L) // Update every second
            }
        }

    }

    @SuppressLint("RemoteViewLayout")
    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val customView = RemoteViews(packageName, R.layout.notification_layout)
        val currentSong = audioFiles.getOrNull(currentSongIndex)
        customView.setTextViewText(R.id.notification_title, currentSong?.title ?: "Unknown Title")
        customView.setTextViewText(R.id.notification_description, currentSong?.description ?: "Unknown Description")

        currentSong?.imageUrl?.let { imageUrl ->
            customView.setImageViewUri(R.id.notification_image, Uri.parse(imageUrl))
        }

        if (mediaPlayer?.isPlaying == true) {
            customView.setImageViewResource(R.id.notification_play, R.drawable.ic_play_button_green)
            customView.setOnClickPendingIntent(R.id.notification_play, getPendingIntent(ACTION_PAUSE))
        } else {
            customView.setImageViewResource(R.id.notification_play, R.drawable.ic_play)
            customView.setOnClickPendingIntent(R.id.notification_play, getPendingIntent(ACTION_PLAY))
        }

        customView.setOnClickPendingIntent(R.id.notification_next, getPendingIntent(ACTION_NEXT))
        customView.setOnClickPendingIntent(R.id.notification_previous, getPendingIntent(ACTION_PREVIOUS))

        notification = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.default_image)
            .setCustomContentView(customView)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCustomBigContentView(customView)
            .setStyle(
                NotificationCompat.DecoratedCustomViewStyle()
            )

            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        notificationManager.notify(serviceId, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(notificationChannelId, "Media Playback", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Media Playback Notifications"
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, AudioPlaybackService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_MUTABLE)
    }

    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent?.action) {
                pausePlayback()
            }
        }
    }


     private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            resumePlayback()
        }

        override fun onPause() {
            pausePlayback()
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            seekTo(pos.toInt())
        }
        override fun onSkipToNext() {
            nextSong()
        }

        override fun onSkipToPrevious() {
            previousSong()
        }

        override fun onStop() {
            stopPlayback()
        }
    }

    companion object {
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_NEXT = "ACTION_NEXT"
        const val ACTION_PREVIOUS = "ACTION_PREVIOUS"
    }
}
