package com.example.testprojectmusicplayer.utils

import android.media.MediaMetadataRetriever
import java.util.concurrent.TimeUnit


object AudioDuration{
    fun getAudioFileDuration(filePath: String): String {
        val retriever = MediaMetadataRetriever()
        var durationInMillis: Long = 0

        try {
            // Set the data source to the audio file path
            retriever.setDataSource(filePath)

            // Retrieve the duration from the metadata in milliseconds
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationInMillis = durationStr?.toLong() ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // Release the retriever after use
            retriever.release()
        }

        // Convert milliseconds to minutes and seconds
        return durationInMillis.toString()
    }

     fun formatDuration(durationInMillis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationInMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationInMillis) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}


