package com.example.testprojectmusicplayer.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import dagger.hilt.android.qualifiers.ApplicationContext

object ShareIntent {

    fun createSharePlaylistIntent(playlistUrl: String): Intent {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, playlistUrl) // Add the URL to share
            type = "text/plain" // Specify MIME type
        }
        return Intent.createChooser(shareIntent, "Share Playlist") // Optional: Add a chooser for selecting sharing method
    }



}