package com.example.testprojectmusicplayer.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.databinding.MusciPlayerScreenBinding
import com.example.testprojectmusicplayer.model.Artist
import com.example.testprojectmusicplayer.model.Song
import com.example.testprojectmusicplayer.utils.UiStates
import com.example.testprojectmusicplayer.utils.UserObject
import com.example.testprojectmusicplayer.viewModel.HomeViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MusicPlayerBottomSheet : BottomSheetDialogFragment() {

    private val homeViewModel: HomeViewModel by activityViewModels()

    private lateinit var binding: MusciPlayerScreenBinding

    @Inject
    lateinit var glide : RequestManager

    @Inject
    lateinit var userObject: UserObject

    private var userId : String? = null
    private var albumId :String? = null
    private var songId :String? = null









    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this bottom sheet
        binding = MusciPlayerScreenBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = userObject.getUser()
        userId = user?.userId
        albumId = user?.likedAlbums






        handleClickOnLikedSong()
        likedFunctionalityObserver()
        observer()
        onClick()
        handlePlaybackPositionOrSeekBar()

        handleMediaPlayerFunctionality()

        handleMediaPlayerObserver()
        handlePlayPause()







    }



    private fun likedFunctionalityObserver(){
        lifecycleScope.launch {
            homeViewModel.isLikedSong.collect{
                    state ->
                updateLikedButtonStateSong(state)


            }

        }

        lifecycleScope.launch {
            homeViewModel.likedSong.collect{
                    state ->
                when(state){
                    is UiStates.Loading -> {
                     //   Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()


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

        lifecycleScope.launch {
            homeViewModel.unLikedSong.collect{
                    state ->
                when(state){
                    is UiStates.Loading -> {
                   //     Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()


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


    private fun observer() {
        lifecycleScope.launch {
            homeViewModel.currentSong.collect { song ->
                if (song != null) {

                    initUi(song)
                    songId = song.songId

                } else {
                    Toast.makeText(requireContext(),"Current Song is Empty", Toast.LENGTH_SHORT).show()
                }
            }

        }

        lifecycleScope.launch {
            homeViewModel.currentSongArtist.collect{
                artist ->
                if (artist != null){
                    initializeArtistCard(artist = artist)
                }
                else{
                    binding.mpArtist.visibility = View.GONE
                }

            }
        }





    }

    private fun onClick(){
        binding.mpArrowDown.setOnClickListener {
            findNavController().popBackStack()
        }



    }

    private fun handleClickOnLikedSong(){
        this.binding.mpLikedBtn
            .setOnClickListener {
                if (homeViewModel.isLikedSong.value){
                    homeViewModel.onUnLikedSong(songId?:"",userId?:"", albumId = albumId?:"")
                }
                else{
                    homeViewModel.onLikedSong(songId?:"",userId?:"", albumId = albumId?:"")
                }
            }

    }




    private fun handleMediaPlayerObserver(){
        lifecycleScope.launch {
            homeViewModel.isPlaying.collect{
                playing ->
                updatePlayPauseButtonState(playing)
            }
        }
    }

    private fun handlePlayPause(){
        this.binding.mpPlayBtn
            .setOnClickListener {
                if (homeViewModel.isPlaying.value){
                    homeViewModel.updatePlayPauseState(false)
                }
                else{
                    homeViewModel.updatePlayPauseState(true )
                }
            }


    }

    private fun updatePlayPauseButtonState(isPlaying: Boolean){
        if (isPlaying){

            this.binding.mpPlayBtn.setImageResource(R.drawable.ic_play)


        }
        else{
            this.binding.mpPlayBtn.setImageResource(R.drawable.ic_play_button_green)


        }
    }



    private fun initUi(song: Song){
        glide
            .load(song.imageUrl)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.default_image) // Replace with your default image resource
                    .error(R.drawable.default_image) // Shown when there is an error loading the image
            )
            .into(binding.mpCurrentSongPoster)

        binding.mpSongTitle.text = song.title
        binding.mpArtist.text = song.description
        binding.mpTitleSong.text = song.title
        //binding.

        binding.mpLyricsPreview.lpLyrics.text = song.lyrics




    }




    private fun updateLikedButtonStateSong(isLiked: Boolean){
        if (isLiked){

            this.binding.mpLikedBtn.setImageResource(R.drawable.liked_button)


        }
        else{
            this.binding.mpLoopBtn.setImageResource(R.drawable.ic_unlike)


        }

    }


    private fun handlePlaybackPositionOrSeekBar(){
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

            }

        }
    }


    private fun initializeArtistCard(artist: Artist){
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

        binding.mpArtistCard.abaListenerNo.text = "${artist.followers}Monthly Listner"

    }



    private fun handleMediaPlayerFunctionality(){
        binding.mpPreviousBtn.setOnClickListener {  }


        binding.mpPlayBtn.setOnClickListener {

        }

        binding.mpNextBtn.setOnClickListener {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
         // Avoid memory leaks
    }
}
