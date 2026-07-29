package com.example.testprojectmusicplayer.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.databinding.MusciPlayerScreenBinding
import com.example.testprojectmusicplayer.model.Artist
import com.example.testprojectmusicplayer.model.Song
import com.example.testprojectmusicplayer.utils.AudioDuration
import com.example.testprojectmusicplayer.utils.UiStates
import com.example.testprojectmusicplayer.utils.UserObject
import com.example.testprojectmusicplayer.viewModel.HomeViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class MusicPlayerBottomSheet : BottomSheetDialogFragment() {

    private val homeViewModel: HomeViewModel by activityViewModels()

    private lateinit var binding: MusciPlayerScreenBinding

    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var userObject: UserObject

    private var userId: String? = null
    private var  albumId: String? = null
    private var songId: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this bottom sheet
        binding = MusciPlayerScreenBinding.inflate(layoutInflater)

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Launch coroutine for background work
        //lifecycleScope.launch(Dispatchers.IO) {
            // Fetch user data in the background
            val user = userObject.getUser()
            userId = user?.userId
             albumId = user?.likedAlbums?.firstOrNull() ?: ""

        // Switch to the main thread for UI-related tasks
            //withContext(Dispatchers.Main) {
                // Initialize BottomSheetBehavior
                val bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED // Start collapsed
                bottomSheetBehavior.isHideable = false // Prevent hiding the bottom sheet completely

                // Get screen height
                val displayMetrics = DisplayMetrics()
                requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
                val screenHeight = displayMetrics.heightPixels

                // Set peek height to max screen height
                bottomSheetBehavior.peekHeight = screenHeight



                // Set up all the observers and handle media player functionality on the main thread
                handleClickOnLikedSong()
                likedFunctionalityObserver()
                observer()
                onClick()
                handlePlaybackPositionOrSeekBar()
                handleMediaPlayerFunctionality()
                handleMediaPlayerObserver()
           // }
       // }
    }






    private fun observer() {
        lifecycleScope.launch {
            homeViewModel.currentSong.collect { song ->
                if (song != null) {

                    initUi(song)
                    songId = song.songId
                    homeViewModel.setCurrentSongArtist()

                } else {
                    Toast.makeText(requireContext(), "Current Song is Empty", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        }

        lifecycleScope.launch {
            homeViewModel.currentSongArtist.collect { artist ->
                if (artist != null) {
                    initializeArtistCard(artist = artist)
                } else {
                    binding.mpArtist.visibility = View.GONE
                }

            }
        }


    }

    private fun onClick() {
        // Assuming you have already initialized bottomSheetBehavior

        binding.mpArrowDown.setOnClickListener {

            val bottomSheetBehavior = BottomSheetBehavior.from(binding.root.parent as View)

            bottomSheetBehavior.isHideable = true

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN


        }

    }

    private fun handleClickOnLikedSong() {
        this.binding.mpLikeBtn.setOnClickListener {
            // Immediately toggle the like state
            val isLiked = !homeViewModel.isLikedSong.value
            updateLikedButtonStateSong(isLiked) // Update UI immediately
            if (isLiked) {
                homeViewModel.onLikedSong(songId ?: "", userId ?: "", albumId = albumId ?: "")
            } else {
                homeViewModel.onUnLikedSong(songId ?: "", userId ?: "", albumId = albumId ?: "")
            }
        }
    }

    // Update this observer to ensure it updates the button state when the state changes
    private fun likedFunctionalityObserver() {
        lifecycleScope.launch {
            homeViewModel.isLikedSong.collect { state ->
                updateLikedButtonStateSong(state)
            }
        }

        lifecycleScope.launch {
            homeViewModel.likedSong.collect { state ->
                when (state) {
                    is UiStates.Loading -> {
                        // Handle loading state if needed
                    }
                    is UiStates.Success -> {
                        // Optionally handle success state if needed
                    }
                    is UiStates.Failure -> {
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                    }
                    is UiStates.Initial -> {
                        // Handle initial state if needed
                    }
                }
            }
        }

        lifecycleScope.launch {
            homeViewModel.unLikedSong.collect { state ->
                when (state) {
                    is UiStates.Loading -> {
                        // Handle loading state if needed
                    }
                    is UiStates.Success -> {
                        // Optionally handle success state if needed
                    }
                    is UiStates.Failure -> {
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                    }
                    is UiStates.Initial -> {
                        // Handle initial state if needed
                    }
                }
            }
        }
    }



    private fun handleMediaPlayerObserver() {
        lifecycleScope.launch {
            homeViewModel.isPlaying.collect { playing ->
                updatePlayPauseButtonState(playing)
            }
        }
    }

    private fun handlePlayPause() {

        if (homeViewModel.isPlaying.value) {
            homeViewModel.pauseSong()
        } else {
            homeViewModel.playSong()
        }


    }

    private fun updatePlayPauseButtonState(isPlaying: Boolean) {
        if (isPlaying) {

            this.binding.mpPlayBtn.setImageResource(R.drawable.ic_play)


        } else {
            this.binding.mpPlayBtn.setImageResource(R.drawable.ic_play_button_green)


        }
    }


    private fun initUi(song: Song) {
        // Load image asynchronously with Glide
        glide.load(song.imageUrl)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.default_image) // Default image resource
                    .error(R.drawable.default_image) // Shown when an error occurs loading the image
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Caches both original and resized images
            )
            .into(binding.mpCurrentSongPoster)

        // Set song metadata
        binding.mpSongTitle.text = song.title
        binding.mpArtist.text = song.description
        binding.mpTitleSong.text = song.title
        binding.mpLyricsPreview.lpLyrics.text = song.lyrics

        // Calculate audio duration asynchronously
        lifecycleScope.launch(Dispatchers.IO) {
            val audioDuration = AudioDuration.getAudioFileDuration(song.audioFile)

            homeViewModel.isLikedSong(songId = song.songId, userId = userId?:"")

            withContext(Dispatchers.Main) {
                // Update seek bar and duration on the main thread
                binding.mpSeekBar.max = audioDuration.toInt()
                binding.mpEndProgressText.text = AudioDuration.formatDuration(audioDuration.toLong())
            }
        }
    }



    private fun updateLikedButtonStateSong(isLiked: Boolean) {
        if (isLiked) {

            this.binding.mpLikeBtn.setImageResource(R.drawable.liked_button)


        } else {
            this.binding.mpLikeBtn.setImageResource(R.drawable.ic_unlike)


        }

    }


    private fun handlePlaybackPositionOrSeekBar() {
        binding.mpSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // Update the playback position in the ViewModel when user drags the SeekBar
                    homeViewModel.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional: Handle user start tracking
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Optional: Handle user stop tracking
            }
        })

        lifecycleScope.launch {
            homeViewModel.playbackPosition.collect { seekBarPosition ->

                binding.mpSeekBar.progress = seekBarPosition
                val seekBar = seekBarPosition
                binding.mpInitialProgressText.text = AudioDuration.formatDuration(seekBar.toLong())

            }

        }
    }


    private fun initializeArtistCard(artist: Artist) {
        binding.mpArtistCard.abaArtistName.text = artist.name
        //binding.mpArtistCard.
        glide
            //.with(binding.psiImage)
            .load(artist.imageUrl)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.default_image) // Replace with your default image resource
                    .error(R.drawable.default_image) // Shown when there is an error loading the image
            )
            .into(binding.mpArtistCard.abaImage)

        binding.mpArtistCard.abaDescription.text = artist.name

        binding.mpArtistCard.abaListenerNo.text = "${artist.followers} Monthly Listner"

    }


    private fun handleMediaPlayerFunctionality() {
        binding.mpPreviousBtn.setOnClickListener {
            Log.d("CLickOnNext","PREVIOUS")

                lifecycleScope.launch {   homeViewModel.onClickPrevious() }


        }


        binding.mpPlayBtn.setOnClickListener {

            handlePlayPause()
        }

        binding.mpNextBtn.setOnClickListener {
            Log.d("CLickOnNext","NEXT")

            lifecycleScope.launch {
                homeViewModel.onClickNext()
            }



        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Avoid memory leaks
    }
}
