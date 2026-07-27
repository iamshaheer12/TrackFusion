package com.example.testprojectmusicplayer.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.databinding.FragmentMainBinding
import com.example.testprojectmusicplayer.model.Song
import com.example.testprojectmusicplayer.utils.AudioDuration
import com.example.testprojectmusicplayer.utils.UiStates
import com.example.testprojectmusicplayer.utils.UserObject
import com.example.testprojectmusicplayer.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment() {
    lateinit var binding: FragmentMainBinding
    private lateinit var navController: NavController

    private val viewModel: HomeViewModel by activityViewModels()


    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var userObject: UserObject


    private var userId: String? = null
    private var albumId: String? = null
    private var songId: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {





        binding = FragmentMainBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Launch a coroutine to handle asynchronous tasks
      //  lifecycleScope.launch(Dispatchers.IO) {
            // Fetch user data in the background thread
            val user = userObject.getUser()
            userId = user?.userId
            albumId = user?.likedAlbums?.firstOrNull() ?: ""
            // Switch to the main thread for UI-related operations
         //   withContext(Dispatchers.Main) {
                // Initialize the navigation controller
                val navHostFragment =
                    childFragmentManager.findFragmentById(binding.nestedNavHost.id) as NavHostFragment
                navController = navHostFragment.navController
                binding.bottomNav.setupWithNavController(navController)

                // Set up all the observers and handle user interactions
                observer()
               // likedFunctionalityObserver()
               // handleClickOnLikedSong()
                handlePlayPause()
                playPauseObserver()

                onClick()
            }
        //}
   // }



    private fun onClick() {

        binding.mainBottomSheetForSong.root.setOnClickListener {
            openMusicPlayerBottomSheet()
        }

        // Remove this from here if the bottom sheet has its own play/pause logic
        binding.mainBottomSheetForSong.btmSheetSongPlayBtn.setOnClickListener {
            handlePlayPause()
        }



    }

    private fun observer() {
        lifecycleScope.launch {
            viewModel.currentSong.collectLatest { song ->
                if (song != null) {
                    initUi(song)
                    handlePlaybackPositionOrSeekBar()


                    songId = song.songId

                } else {
                    binding.mainBottomSheetForSong.root.visibility = View.GONE
                }
            }
        }





        lifecycleScope.launch {
            viewModel.playbackPosition.collect { position ->
                binding.mainBottomSheetForSong.mpSeekBar.progress = position

            }
        }

    }

    private fun likedFunctionalityObserver() {
        lifecycleScope.launch {
            viewModel.isLikedSong.collect { state ->
                updateLikedButtonStateSong(state)


            }

        }

        lifecycleScope.launch {
            viewModel.likedSong.collect { state ->
                when (state) {
                    is UiStates.Loading -> {
                        //   Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()


                    }

                    is UiStates.Success -> {
                    //    Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()


                    }

                    is UiStates.Failure -> {
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()


                    }

                    is UiStates.Initial -> {

                    }
                }
            }

        }

        lifecycleScope.launch {
            viewModel.unLikedSong.collect { state ->
                when (state) {
                    is UiStates.Loading -> {
                        //     Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()


                    }

                    is UiStates.Success -> {
                      //  Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()


                    }

                    is UiStates.Failure -> {
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()


                    }

                    is UiStates.Initial -> {

                    }
                }
            }

        }


    }


    private fun playPauseObserver() {
        lifecycleScope.launch {
            viewModel.isPlaying.collect { state ->
                updatePlayPauseButtonState(state)
            }
        }
    }

    private fun updateLikedButtonStateSong(isLiked: Boolean) {
        if (isLiked) {

            this.binding.mainBottomSheetForSong.btmSheetSongLikeBtn.setImageResource(R.drawable.liked_button)


        } else {
            this.binding.mainBottomSheetForSong.btmSheetSongLikeBtn.setImageResource(R.drawable.ic_unlike)


        }

    }

    private fun handlePlayPause() {
        Log.d(
            "MainFragment",
            "Play/Pause button clicked. Current state: ${viewModel.isPlaying.value}"
        )

        if (viewModel.isPlaying.value) {
            viewModel.pauseSong()
        } else {
            viewModel.playSong()
        }


    }


    private fun updatePlayPauseButtonState(isPlaying: Boolean) {
        if (isPlaying) {

            this.binding.mainBottomSheetForSong.btmSheetSongPlayBtn.setImageResource(R.drawable.ic_play)


        } else {
            this.binding.mainBottomSheetForSong.btmSheetSongPlayBtn.setImageResource(R.drawable.ic_play_button_green)


        }
    }


    private fun handleClickOnLikedSong() {
        this.binding.mainBottomSheetForSong.btmSheetSongLikeBtn
            .setOnClickListener {
                if (viewModel.isLikedSong.value) {
                    viewModel.onUnLikedSong(songId ?: "", userId ?: "", albumId = albumId ?: "")
                } else {
                    viewModel.onLikedSong(songId ?: "", userId ?: "", albumId = albumId ?: "")
                }
            }

    }

    private fun handlePlaybackPositionOrSeekBar() {

            lifecycleScope.launch {
                viewModel.playbackPosition.collect { seekBarPosition ->

                    Log.d("PlaybackMain", "handlePlaybackPositionOrSeekBar: $seekBarPosition")
                    Log.d("PlaybackMain", "handlePlaybackPositionOrSeekBar: ${binding.mainBottomSheetForSong.mpSeekBar.max}")

                    binding.mainBottomSheetForSong.mpSeekBar.progress = seekBarPosition


                }
            }


    }

    private fun initUi(song: Song) {

        binding.mainBottomSheetForSong.btmSheetSongTitle.text = song.title
        binding.mainBottomSheetForSong.btmSheetSongArtist.text = song.description
        val duration = AudioDuration.getAudioFileDuration(song.audioFile).toInt()
        Log.d("InitUI", "Song duration: $duration")
        binding.mainBottomSheetForSong.mpSeekBar.max =duration
       // Log.d("InitUI", "Song duration: $duration")

        glide
            .load(song.imageUrl)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.default_image) // Replace with your default image resource
                    .error(R.drawable.default_image) // Shown when there is an error loading the image
            )
            .into(binding.mainBottomSheetForSong.btmSheetSongImage)

        binding.mainBottomSheetForSong.root.visibility = View.VISIBLE
    }

    private fun openMusicPlayerBottomSheet() {
        // Create an instance of the MusicPlayerBottomSheet
        val musicPlayerBottomSheet = MusicPlayerBottomSheet()

        musicPlayerBottomSheet.show(
            requireActivity().supportFragmentManager,
            musicPlayerBottomSheet.tag
        )
    }


}