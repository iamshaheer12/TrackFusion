package com.example.testprojectmusicplayer.utils

import android.annotation.SuppressLint
import java.util.concurrent.TimeUnit

object SharedPrefCons {
    val storeSession = "user_session"
    const val localPref = "Local Storage"
    val loggingEmail = "email"

}
object FireStoreCons{
    const val user = "user"
    val loggingEmail = "https://Fusion.track/finishLogIn?screen=home"
    const val artistCollection = "Artist"
    const val albumCollection = "Albums"
    const val songsCollection = "Songs"

}
object formatDuration{
     @SuppressLint("DefaultLocale")
     fun formatDuration(durationInMillis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationInMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationInMillis) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}