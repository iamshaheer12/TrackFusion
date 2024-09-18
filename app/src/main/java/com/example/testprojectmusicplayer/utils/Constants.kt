package com.example.testprojectmusicplayer.utils

import android.annotation.SuppressLint
import java.util.concurrent.TimeUnit

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

}
object FormatDuration{
     @SuppressLint("DefaultLocale")
     fun formatDuration(durationInMillis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationInMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationInMillis) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
