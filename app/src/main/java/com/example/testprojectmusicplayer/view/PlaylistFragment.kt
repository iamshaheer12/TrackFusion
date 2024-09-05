package com.example.testprojectmusicplayer.view

import com.example.testprojectmusicplayer.viewModel.SharedViewModel
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.adapters.PlaylistItemsRecyclerView
import com.example.testprojectmusicplayer.databinding.FragmentPlaylistBinding
import com.example.testprojectmusicplayer.model.Song
import com.example.testprojectmusicplayer.utils.AudioPlaybackService
import com.example.testprojectmusicplayer.utils.ForeGround
import com.example.testprojectmusicplayer.utils.UiStates
import com.example.testprojectmusicplayer.utils.formatDuration.formatDuration
import com.example.testprojectmusicplayer.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlaylistFragment : Fragment(){
    private val permissionCode = 100
    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var binding: FragmentPlaylistBinding
    private val adapter by lazy {
        PlaylistItemsRecyclerView(
            onMoreClicked ={ song,_ ->},
            onItemClicked ={song ->
                playSong(song)
                           } ,

        )
    }
    private val homeViewModel: HomeViewModel by viewModels()


    private var isPlayingAll = false
    private var songs: List<Song> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaylistBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        requestPermissionResult()



        setRecyclerView()
        displayPlaylistDuration()
        onClick()
        observers()
        homeViewModel.getRecentPlayedSongs()
    }
    private fun observers(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                homeViewModel.allSongsState.collect{state ->
                    when (state) {
                        is UiStates.Loading -> {
                            // Show loading indicator
                            Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()

                        }
                        is UiStates.Success -> {
                            songs = state.data.toMutableList()
                            Toast.makeText(requireContext(), "Success"+songs, Toast.LENGTH_SHORT).show()

                            Log.e("SongsData",state.data.toMutableList().toString())
                            adapter.updateList(state.data.toMutableList())

                            // Navigate to the next screen or show success message
                        }
                        is UiStates.Failure -> {
                            // Show error message
                            Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                        }
                    }

                }
            }
        }
    }

    private fun  onClick(){

        binding.playlistPlayBtn.setOnClickListener {
            checkAndRequestPermissions()
//            if (isPlayingAll) {
//              //  pauseAllSongs()
//            } else {
//               // playAllSongs()
//            }
        }

    }
    private fun setRecyclerView() {

        binding.playlistListItems.adapter = adapter
    }

    private fun playAllSongs() {
        if (songs.isNotEmpty()){
            val intent = Intent(requireContext(), AudioPlaybackService::class.java).apply {
                action = AudioPlaybackService.ACTION_PLAY
                Log.e("PlayAll Songs",songs.toString())

               // Send the list to the service
            }
            if (Build.VERSION.SDK_INT >= 26) {
                ContextCompat.startForegroundService(requireContext(), intent)
            } else {
                // Pre-O behavior.
                requireContext().startService(intent)
            }
            isPlayingAll = true
            binding.playlistPlayBtn.setImageResource(R.drawable.ic_play)
        }
        else{
            Toast.makeText(requireContext(),"Sorry List is Empty ", Toast.LENGTH_SHORT).show()

        }

    }


    private fun pauseAllSongs() {
        val intent = Intent(requireContext(), AudioPlaybackService::class.java).apply {
            action = AudioPlaybackService.ACTION_PAUSE
        }
//        if (Build.VERSION.SDK_INT >= 26) {
//            ContextCompat.startForegroundService(requireContext(), intent)
//        } else {
//            // Pre-O behavior.
            requireContext().startService(intent)
       // }
       // requireContext().startForegroundService(intent)
        // Changed from startService to startForegroundService
        binding.playlistPlayBtn.setImageResource(R.drawable.ic_play_button_green)
        isPlayingAll = false
    }



    private fun displayPlaylistDuration() {
        var totalDuration = 0L
        var remainingSongs = songs.size

        val durationCallback: (Long) -> Unit = { duration ->
            totalDuration += duration
            remainingSongs--
            if (remainingSongs == 0) { // All durations have been processed
                val formattedDuration = formatDuration(totalDuration)
                binding.playlistDuration.text = formattedDuration
            }
        }
        Log.e("SongsForPlay",songs.toString())

        songs.forEach { song ->
            getAudioDuration(song.audioFile, durationCallback)
        }
    }

    private fun getAudioDuration(url: String, callback: (Long) -> Unit) {
        val mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepare()
            callback(duration.toLong())
            release()
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK)
                != PackageManager.PERMISSION_GRANTED
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    permissionsToRequest.add(android.Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK)
                }
            }
        }

        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.FOREGROUND_SERVICE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                permissionsToRequest.add( android.Manifest.permission.FOREGROUND_SERVICE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            foregroundNotification() // Permissions already granted
        }
    }



    private fun foregroundNotification(){
        if (isPlayingAll) {
                pauseAllSongs()
            } else {
                playAllSongs()
            }
//        if (Build.VERSION.SDK_INT >= 26) {
//            ContextCompat.startForegroundService(requireContext(), Intent(requireContext(),ForeGround::class.java))
//        } else {
//            // Pre-O behavior.
//            requireContext().startService(Intent(requireContext(),ForeGround::class.java))
//        }
    }
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("This app requires notification and foreground service permissions to function properly.")
            .setPositiveButton("Grant") { _, _ ->
                // Direct user to app settings to manually grant permissions
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun requestPermissionResult() {
        requestPermissionsLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // Handle the permission results
            val allPermissionsGranted = permissions.all { it.value }
            if (allPermissionsGranted) {
                // All required permissions are granted, proceed with functionality
                foregroundNotification()
            } else {
                // Handle the case where permissions are denied
                showPermissionDeniedDialog()
            }
        }

  }


    private fun playSong(pos: Int) {
        val intent = Intent(requireContext(), AudioPlaybackService::class.java).apply {
            action = AudioPlaybackService.ACTION_PLAY
            //putExtra(AudioPlaybackService.EXTRA_POSITION,pos)
        }
//        if (Build.VERSION.SDK_INT >= 26) {
//            ContextCompat.startForegroundService(requireContext(),intent)
//        } else {
//
            // Pre-O behavior.
            requireContext().startService(intent)
       // }
    // Changed from startService to startForegroundService
    }
}
