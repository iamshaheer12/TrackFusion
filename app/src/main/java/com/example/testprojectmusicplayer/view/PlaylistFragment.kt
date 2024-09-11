package com.example.testprojectmusicplayer.view

import android.annotation.SuppressLint
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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.adapters.PlaylistItemsRecyclerView
import com.example.testprojectmusicplayer.databinding.FragmentPlaylistBinding
import com.example.testprojectmusicplayer.databinding.MoreBottomSheetLayoutBinding
import com.example.testprojectmusicplayer.model.Song
import com.example.testprojectmusicplayer.utils.UiStates
import com.example.testprojectmusicplayer.utils.formatDuration.formatDuration
import com.example.testprojectmusicplayer.viewModel.HomeViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlaylistFragment : Fragment() {

    @Inject
    lateinit var glide: RequestManager
    private val permissionCode = 100
    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var binding: FragmentPlaylistBinding
    private lateinit var bottomBinding: MoreBottomSheetLayoutBinding

    private var isLikedAlbum = false
    private val adapter by lazy {
        PlaylistItemsRecyclerView(glide = glide,
            onMoreClicked = { song, pos ->
                settingUpBottomSheet(song)

            },
            onItemClicked = { song ->
                Log.d("CurrentIndex",song.toString())
                playSong(song) // Handles song item click
            }
        )
    }

    private val homeViewModel: HomeViewModel by activityViewModels()
    private var isPlayingAll = false
    private var songs: List<Song> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomBinding = MoreBottomSheetLayoutBinding.inflate(layoutInflater)

        requestPermissionResult()
        setRecyclerView()
        displayPlaylistDuration()

        onClick()
        observers()
        homeViewModel.getRecentPlayedSongs()
        homeViewModel.isLikedSong("mnw6BmhcgP0ajRFOZkmq","1234")
        homeViewModel.isLikedAlbum("Lw6vYw6iz72gmU4CyF49","1234")
    }

    private fun observers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    homeViewModel.allSongsState.collect { state ->
                        when (state) {
                            is UiStates.Loading -> {
                                Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()
                            }
                            is UiStates.Success -> {
                                songs = state.data.toMutableList()
                                Toast.makeText(requireContext(), "Success: ${songs.size} songs loaded", Toast.LENGTH_SHORT).show()
                                Log.e("SongsData", songs.toString())
                                adapter.updateList(songs)
                            }
                            is UiStates.Failure -> {
                                Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                launch {
                    homeViewModel.isPlaying.collect { isPlaying ->
                        isPlayingAll = isPlaying
                        val playButtonIcon = if (isPlaying) R.drawable.ic_play else R.drawable.ic_play_button_green
                        binding.playlistPlayBtn.setImageResource(playButtonIcon)
                    }
                }

                launch {
                    homeViewModel.currentSongIndex.collect { currentIndex ->
                        Log.d("CurrentIndes",currentIndex.toString())
                        adapter.updateSelection(currentIndex)
                    }
                }

                launch {
                    homeViewModel.isLikedAlbum.collect{
                        state ->
                        updateLikeButtonStateAlbum(state)

                        isLikedAlbum = state
                    }

                }

                launch {
                    homeViewModel.likedAlbum.collect{
                            state ->
                        when(state){
                            is UiStates.Loading -> {
                                Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()


                            }
                            is UiStates.Success -> {
                                Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()


                            }
                            is UiStates.Failure -> {
                                Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()


                            }
                        }
                    }

                }
                launch {
                    homeViewModel.unLikedAlbum.collect{
                            state ->
                        when(state){
                            is UiStates.Loading -> {
                                Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()


                            }
                            is UiStates.Success -> {
                                Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()


                            }
                            is UiStates.Failure -> {
                                Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()


                            }
                        }
                    }

                }
                launch {
                    homeViewModel.isLikedSong.collect{
                            state ->
                        updateLikedButtonStateSong(state)


                    }

                }

                launch {
                    homeViewModel.likedSong.collect{
                            state ->
                        when(state){
                            is UiStates.Loading -> {
                                Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()


                            }
                            is UiStates.Success -> {
                                Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()


                            }
                            is UiStates.Failure -> {
                                Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()


                            }
                        }
                    }

                }
                launch {
                    homeViewModel.unLikedSong.collect{
                            state ->
                        when(state){
                            is UiStates.Loading -> {
                                Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()


                            }
                            is UiStates.Success -> {
                                Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()


                            }
                            is UiStates.Failure -> {
                                Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()


                            }
                        }
                    }

                }
            }
        }


    }

    private fun onClick() {
        binding.playlistPlayBtn.setOnClickListener {
            checkAndRequestPermissions()
        }
        binding.playlistLikedBtn.setOnClickListener {
            Log.d("LikedSong343", isLikedAlbum.toString())
            if (homeViewModel.isLikedAlbum.value){
                homeViewModel.onUnLikedAlbum("Lw6vYw6iz72gmU4CyF49","1234")
            }
            else{
                homeViewModel.onLikedAlbum("Lw6vYw6iz72gmU4CyF49","1234")
            }

        }

        this.bottomBinding.moreBottomLinearlayoutLike
            .setOnClickListener {
            if (homeViewModel.isLikedSong.value){
                homeViewModel.onUnLikedSong("mnw6BmhcgP0ajRFOZkmq","1234")
            }
            else{
                homeViewModel.onLikedSong("mnw6BmhcgP0ajRFOZkmq","1234")
            }
        }
    }

    private fun setRecyclerView() {
        binding.playlistListItems.adapter = adapter
    }

    private fun playAllSongs() {
        lifecycleScope.launch {
            homeViewModel.playSong()
            binding.playlistPlayBtn.setImageResource(R.drawable.ic_play)
            homeViewModel.updatePlayPauseState(true)
        }
    }

    private fun pauseAllSongs() {
        lifecycleScope.launch {
            homeViewModel.pausePlayback()
            homeViewModel.updatePlayPauseState(false)
        }
        binding.playlistPlayBtn.setImageResource(R.drawable.ic_play_button_green)
    }

    private fun displayPlaylistDuration() {
        var totalDuration = 0L
        var remainingSongs = songs.size

        val durationCallback: (Long) -> Unit = { duration ->
            totalDuration += duration
            remainingSongs--
            if (remainingSongs == 0) {
                val formattedDuration = formatDuration(totalDuration)
                binding.playlistDuration.text = formattedDuration
            }
        }
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
                permissionsToRequest.add(android.Manifest.permission.FOREGROUND_SERVICE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            foregroundNotification()
        }
    }

    private fun foregroundNotification() {
        if (isPlayingAll) {
            pauseAllSongs()
        } else {
            playAllSongs()
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("This app requires notification and foreground service permissions to function properly.")
            .setPositiveButton("Grant") { _, _ ->
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
            val allPermissionsGranted = permissions.all { it.value }
            if (allPermissionsGranted) {
                foregroundNotification()
            } else {
                showPermissionDeniedDialog()
            }
        }
    }

    private fun playSong(pos: Int) {
        lifecycleScope.launch {
            homeViewModel.updateIndex(pos)
            homeViewModel.playSong()
            adapter.updateSelection(pos)
        }
    }


    private fun updateLikeButtonStateAlbum(isLiked: Boolean) {
        if (isLiked) {
            binding.playlistLikedBtn.setImageResource(R.drawable.liked_button)
        } else {
            binding.playlistLikedBtn.setImageResource(R.drawable.ic_unlike)
        }
    }

    private fun updateLikedButtonStateSong(isLiked: Boolean){
        if (isLiked){
            this.bottomBinding.moreBottomSheetLikeText.text = "Remove from liked Song"
            this.bottomBinding.moreBottomSheetLikeIcn.setImageResource(R.drawable.liked_button)


        }
        else{
            this.bottomBinding.moreBottomSheetLikeText.text ="Add to Liked Songs"
            this.bottomBinding.moreBottomSheetLikeIcn.setImageResource(R.drawable.ic_unlike)


        }

    }
    private fun onMoreClick(){}

    @SuppressLint("InflateParams")
    private fun settingUpBottomSheet(song: Song){
        val dialog = BottomSheetDialog(requireContext())
        // Check if the binding is already initialized and remove it from its parent
        if (::bottomBinding.isInitialized && bottomBinding.root.parent != null) {
            (bottomBinding.root.parent as? ViewGroup)?.removeView(bottomBinding.root)
        } else {
            // Initialize the binding only if it's not already initialized
            bottomBinding = MoreBottomSheetLayoutBinding.inflate(layoutInflater)
        }


        // on below line we are inflating a layout file which we have created.
       bottomBinding.moreBottomLinearlayoutDeletePlaylist.visibility = View.GONE
       bottomBinding.moreBottomLinearlayoutEditPlaylist.visibility = View.GONE

        glide
            .load(song.imageUrl)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.default_image) // Replace with your default image resource
                    .error(R.drawable.default_image) // Shown when there is an error loading the image
            )
            .into(bottomBinding.moreBottomSheetImage)

        bottomBinding.moreBottomSheetSongTitle.text = song.title

       bottomBinding.moreBottomSheetSongDescription.text = song.description


        dialog.setCancelable(true)

        // on below line we are setting
        // content view to our view.
        dialog.setContentView(this.bottomBinding.root)

        // on below line we are calling
        // a show method to display a dialog.
        dialog.show()
    }
}
