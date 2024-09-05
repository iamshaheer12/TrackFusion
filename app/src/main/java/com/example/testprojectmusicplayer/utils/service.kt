package com.example.testprojectmusicplayer.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.model.Song
import com.example.testprojectmusicplayer.view.MainActivity

class AudioPlaybackService1 : Service() {

    override fun onCreate() {
        super.onCreate()
        //startForegroundServiceWithNotification()
    }

    companion object {
        const val ACTION_PLAY = "com.example.testprojectmusicplayer.ACTION_PLAY"
        const val ACTION_NEXT = "com.example.testprojectmusicplayer.NEXT"
        const val ACTION_PREVIOUS = "com.example.testprojectmusicplayer.PREVIOUS"
        const val EXTRA_POSITION = "com.example.testprojectmusicplayer.EXTRA_POSITION"

        const val ACTION_PLAY_ALL = "com.example.testprojectmusicplayer.ACTION_PLAY_ALL"
        const val ACTION_PAUSE = "com.example.testprojectmusicplayer.ACTION_PAUSE"
        const val ACTION_STOP = "com.example.testprojectmusicplayer.ACTION_STOP"
        const val EXTRA_AUDIO_URLS = "com.example.testprojectmusicplayer.EXTRA_AUDIO_URLS"
        const val CHANNEL_ID = "AudioPlaybackChannel"
        const val NOTIFICATION_ID = 1
    }

    private var mediaPlayer: MediaPlayer? = null
    private var audioFiles: List<Song> = listOf()
    private var currentSongIndex = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundServiceWithNotification()
        val action = intent?.action
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableArrayListExtra<Song>(EXTRA_AUDIO_URLS,Song::class.java)?.let {
                audioFiles = it // This is now a List<Song>
            }
        }
        else{
            intent?.getParcelableArrayListExtra<Song>(EXTRA_AUDIO_URLS)?.let {
                audioFiles = it
            }

        }
        Log.d("AudioPlaybackService", "Received audio files: ${audioFiles.size}")
        when (action) {
            ACTION_PLAY -> playSpecificSong(intent.getIntExtra(EXTRA_POSITION,0), )
            ACTION_PLAY_ALL ->{
                playAll()
            }
            ACTION_PAUSE -> pauseAudio()
            ACTION_STOP -> stopAudio()
            ACTION_PREVIOUS -> previousSong()
            ACTION_NEXT -> nextSong()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // No binding is provided
    }

    private fun playAll() {
        if (audioFiles.isNotEmpty()) {
            currentSongIndex = 0 // Start from the first song
            playAudio(audioFiles[currentSongIndex].audioFile)
        } else {
            Toast.makeText(this, "No audio files available to play", Toast.LENGTH_SHORT).show()
        }
    }
    private fun playSpecificSong(currentIndex:Int){
        if (audioFiles.isNotEmpty()){
            currentSongIndex = currentIndex
            playAudio(audioFiles[currentSongIndex].audioFile)
        }
        else{
            Toast.makeText(this, "No audio files available to play", Toast.LENGTH_SHORT).show()

        }
    }

    private fun playAudio(audioUrl: String) {
        stopAudio() // Release previous instance
        mediaPlayer = MediaPlayer().apply {
            setDataSource(audioUrl)
            setOnPreparedListener {
                start()
            }
            setOnCompletionListener {
                if (isLooping) {
                    start()
                } else {
                    nextSong()
                }
            }
            prepareAsync() // Use asynchronous preparation
        }
        startForegroundServiceWithNotification()
    }


    private fun nextSong() {
        if (currentSongIndex < audioFiles.size - 1) {
            currentSongIndex++
            playAudio(audioFiles[currentSongIndex].audioFile)
        } else {
            stopAudio()
        }
    }
    private fun previousSong(){
        if(currentSongIndex > 0 ){
            currentSongIndex --
            playAudio(audioFiles[currentSongIndex].audioFile)
        }
        else{
            playAudio(audioFiles[currentSongIndex].audioFile)
        }
    }

    private fun pauseAudio() {
        mediaPlayer?.pause()
    }

    private fun stopAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        this.stopForeground(true) // Stop foreground state and remove notification
    }

    private fun startForegroundServiceWithNotification() {
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java) // Your activity to open
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val title = if (audioFiles.isNotEmpty()) audioFiles[currentSongIndex].title else "No Song"
        val description = if (audioFiles.isNotEmpty()) audioFiles[currentSongIndex].description else "No Description"

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(description)
            .setSmallIcon(R.drawable.default_image) // Replace with your own icon
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }


    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Audio Playback Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
