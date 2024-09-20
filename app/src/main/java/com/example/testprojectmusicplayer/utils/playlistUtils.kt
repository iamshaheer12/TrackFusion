package com.example.yourappname.utils // Change to your actual package name

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.testprojectmusicplayer.model.Song
import java.io.IOException

object PlaylistUtils {

    // Method to display playlist duration
    fun displayPlaylistDuration(
        songs: List<Song>,
        formatDuration: (Long) -> String,
        onDurationCalculated: (String) -> Unit
    ) {
        var totalDuration = 0L
        var remainingSongs = songs.size

        val durationCallback: (Long) -> Unit = { duration ->
            totalDuration += duration
            remainingSongs--
            if (remainingSongs == 0) {
                val formattedDuration = formatDuration(totalDuration)
                onDurationCalculated(formattedDuration) // Notify when duration is calculated
            }
        }

        songs.forEach { song ->
            getAudioDuration(song.audioFile, durationCallback)
        }
    }


    // Method to get the audio duration using MediaPlayer
    private fun getAudioDuration(url: String, callback: (Long) -> Unit) {
        try {
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                prepare()
                callback(duration.toLong()) // Pass the duration to callback
                release() // Release the media player after usage
            }
        } catch (e: IOException) {
            Log.e("PlaylistUtils", "Error setting data source: ${e.message}")
            callback(0L) // In case of error, return 0 duration
        }
    }



    // Method to check and request required permissions
    fun checkAndRequestPermissions(
        context: Context,
        requestPermissionsLauncher: ActivityResultLauncher<Array<String>>,
        onPermissionsGranted: () -> Unit
    ) {
        val permissionsToRequest = mutableListOf<String>()

        // Check for POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Check for FOREGROUND_SERVICE_MEDIA_PLAYBACK permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK)
                != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    permissionsToRequest.add(Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK)
                }
            }
        }

        // Check for FOREGROUND_SERVICE permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.FOREGROUND_SERVICE)
            != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                permissionsToRequest.add(Manifest.permission.FOREGROUND_SERVICE)
            }
        }

        // Request permissions if needed
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            onPermissionsGranted()
        }
    }

    // Method to show a dialog when permissions are denied
    fun showPermissionDeniedDialog(
        context: Context
    ) {
        AlertDialog.Builder(context)
            .setTitle("Permission Required")
            .setMessage("This app requires notification and foreground service permissions to function properly.")
            .setPositiveButton("Grant") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


}
