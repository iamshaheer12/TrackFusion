package com.example.testprojectmusicplayer.utils

import android.content.Intent

object ShareIntent {

    fun createSharePlaylistIntent(playlistUrl: String): Intent {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, playlistUrl) // Add the URL to share
            type = "text/plain" // Specify MIME type
        }
        return Intent.createChooser(shareIntent, "playlistUrl") // Optional: Add a chooser for selecting sharing method
    }



}