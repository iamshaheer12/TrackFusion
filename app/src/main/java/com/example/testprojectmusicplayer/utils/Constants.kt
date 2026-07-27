package com.example.testprojectmusicplayer.utils

import android.annotation.SuppressLint

object SharedPrefConstants {
    const val STORE_SESSION = "user_session"
    const val LOCAL_PREF = "local_storage"
    const val LOGGING_EMAIL = "email"

}
object FireStoreCons{
    const val USER = "user"
    const val LOGGING_EMAIL = "https://Fusion.track/finishLogIn?screen=home"
    const val ARTIST_COLLECTION = "Artist"
    const val ALBUM_COLLECTION = "Albums"
    const val SONG_COLLECTION = "Songs"
    const val RECENT_PLAY_COLLECTION = "recent_play"

}
object FormatDuration{
     @SuppressLint("DefaultLocale")
     fun formatDuration(durationInMillis: Long): String {
         val seconds = (durationInMillis / 1000) % 60
         val minutes = (durationInMillis / (1000 * 60)) % 60
         val hours = (durationInMillis / (1000 * 60 * 60))

         return if (hours > 0) {
             // If the duration is more than an hour, format it as HH:mm:ss
             String.format("%02d:%02d:%02d", hours, minutes, seconds)
         } else {
             // If the duration is less than an hour, format it as mm:ss
             String.format("%02d:%02d", minutes, seconds)
         }
     }

}
