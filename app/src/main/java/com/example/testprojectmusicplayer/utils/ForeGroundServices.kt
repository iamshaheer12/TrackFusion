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
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.example.testprojectmusicplayer.R
//import com.example.testprojectmusicplayer.di.ViewModelFactory
import com.example.testprojectmusicplayer.model.Song
import com.example.testprojectmusicplayer.viewModel.HomeViewModel
import com.example.testprojectmusicplayer.viewModel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AudioPlaybackService : Service() {
    private val notificationChannelId = "MEDIA_PLAYBACK_CHANNEL"
    private val serviceId = 1
    private lateinit var notificationManager: NotificationManager
   private lateinit var notification: Notification
    private lateinit var mediaSession: MediaSessionCompat
//    @Inject
//    lateinit var viewModelFactory: ViewModelFactory
    private var mediaPlayer: MediaPlayer? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)


   lateinit  var viewModel: HomeViewModel
    private var audioFiles: List<Song> = emptyList()
//        arrayListOf(
//            Song(
//                audioFile = "https://firebasestorage.googleapis.com/v0/b/fir-d60af.appspot.com/o/_Tum%20Hi%20Ho_%20Aashiqui%202%20Full%20Song%20With%20Lyrics%20_%20Aditya%20Roy%20Kapur%2C%20Shraddha%20Kapoor.mp3?alt=media&token=f3fb7bd6-f910-425b-ae5f-7185ce40b430"
//            ),
//            Song(audioFile = "https://firebasestorage.googleapis.com/v0/b/fir-d60af.appspot.com/o/Aadat%20(Juda%20Hoke%20Bhi)%20_%20Atif%20Aslam%20_%20Kunal%20Khemu%20_%20Kalyug%20_%20Sayeed%20Q%20_%20Emraan%20Hashmi.mp3?alt=media&token=65a3142d-dd3c-42f5-a4fc-aeb9af8e5e30")
//        )
    private var currentSongIndex = 0

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        mediaSession = MediaSessionCompat(this, "AudioPlaybackService").apply {
            setCallback(mediaSessionCallback)
            isActive = true
        }


        //notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager



//        viewModel =
//            ViewModelProvider(this.applicationContext as ViewModelStoreOwner, viewModelFactory)[HomeViewModel::class.java]//
//           serviceScope.launch {
//           viewModel.allSongsState.collect{ state ->
//               when (state) {
//                   is UiStates.Loading -> {
//                       // Show loading indicator
//                     //  Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()
//
//                   }
//                   is UiStates.Success -> {
//                       audioFiles = state.data.toMutableList()
//                      // Toast.makeText(requireContext(), "Success"+songs, Toast.LENGTH_SHORT).show()
//
//                       Log.e("SongsData",state.data.toMutableList().toString())
//                       updateNotification()
//                      // adapter.updateList(state.data.toMutableList())
//
//                       // Navigate to the next screen or show success message
//                   }
//                   is UiStates.Failure -> {
//                       // Show error message
//                       //Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
//                   }
//               }
//
//
//
//
//           }
//       }




       // audioFiles = sharedViewModel.getSongsList()



        registerReceiver(becomingNoisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
        updateNotification()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(serviceId, notification)

        when (intent?.action) {
            ACTION_PLAY -> playSong(currentSongIndex)
            ACTION_PAUSE -> pauseSong()
            ACTION_NEXT -> nextSong()
            ACTION_PREVIOUS -> previousSong()
        }

        updateNotification()
        return START_STICKY
    }

    private fun playSong(index: Int) {
        Log.d("service", "playing")
        Log.d("service", audioFiles.isNotEmpty().toString())

        if (audioFiles.isNotEmpty()) {
            serviceScope.launch {
                try {
                    // If the song is already playing or is paused, just resume it
                    if (mediaPlayer != null) {
                        if (mediaPlayer!!.isPlaying) {
                            Log.d("service", "Song is already playing")
                            // Song is already playing, do nothing
                            return@launch
                        } else {
                            // Resume the song if it was paused
                            Log.d("service", "Resuming the current song from paused position")
                            mediaPlayer?.start()
                            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                            updateNotification()
                            return@launch
                        }
                    }

                    // If mediaPlayer is null or hasn't been initialized, start a new song
                    this@AudioPlaybackService.mediaPlayer?.release() // Release existing player if it exists
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(audioFiles[index].audioFile)
                        setOnPreparedListener {
                            start()
                            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                            updateNotification()
                        }
                        setOnCompletionListener {
                            nextSong()
                        }
                        prepareAsync()
                    }
                } catch (e: Exception) {
                    Log.d("service manager test", e.message.toString())
                }
            }
        }
    }

    private fun pauseSong() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
                //updateNotification()
            }
        }
    }

    private fun nextSong() {
        if (currentSongIndex < audioFiles.size - 1) {
            currentSongIndex++
            playSong(currentSongIndex)

            Log.e("Current Indes of song ",currentSongIndex.toString())
          //  sharedViewModel.setCurrentSongIndex(currentSongIndex)
        }
    }

    private fun previousSong() {
        if (currentSongIndex > 0) {
            currentSongIndex--
            playSong(currentSongIndex)
            //sharedViewModel.setCurrentSongIndex(currentSongIndex)

        }
    }

    private fun updatePlaybackState(state: Int) {
        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(state, mediaPlayer?.currentPosition?.toLong() ?: 0L, 1.0f)
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                .build()
        )
    }

    private fun updateNotification() {
        val playPauseAction = if (mediaPlayer?.isPlaying == true) {
            NotificationCompat.Action(R.drawable.ic_pause_circle, "Pause", getPendingIntent(ACTION_PAUSE))
        } else {
            NotificationCompat.Action(R.drawable.ic_play, "Play", getPendingIntent(ACTION_PLAY))
        }

        val notification = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Playing: ")
            .setContentText("Artist: ")
            .setSmallIcon(R.drawable.default_image) // Replace with your actual icon
            .addAction(R.drawable.ic_previous_play, "Previous", getPendingIntent(ACTION_PREVIOUS))
            .addAction(playPauseAction)
            .addAction(R.drawable.ic_next_play, "Next", getPendingIntent(ACTION_NEXT))
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
            )
            .build()
        this.notification = notification
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(notificationChannelId, "Media Playback", NotificationManager.IMPORTANCE_HIGH)
            channel.description = "Media Playback Notifications"
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, AudioPlaybackService::class.java).apply { this.action = action }
        val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getService(this, 0, intent, pendingFlags)
    }


    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        mediaSession.release()
        unregisterReceiver(becomingNoisyReceiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            pauseSong()
        }
    }

    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            playSong(currentSongIndex)
        }

        override fun onPause() {
            pauseSong()
        }

        override fun onSkipToNext() {
            nextSong()
        }

        override fun onSkipToPrevious() {
            previousSong()
        }
    }

    companion object {
        const val ACTION_PLAY = "com.example.testprojectmusicplayer.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.testprojectmusicplayer.ACTION_PAUSE"
        const val ACTION_NEXT = "com.example.testprojectmusicplayer.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.example.testprojectmusicplayer.ACTION_PREVIOUS"
    }
}
