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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.adapters.PlaylistItemsRecyclerView
import com.example.testprojectmusicplayer.databinding.FragmentPlaylistBinding
import com.example.testprojectmusicplayer.databinding.MoreBottomSheetLayoutBinding
import com.example.testprojectmusicplayer.model.Album
import com.example.testprojectmusicplayer.model.Artist
import com.example.testprojectmusicplayer.model.Song
import com.example.testprojectmusicplayer.utils.UiStates
import com.example.testprojectmusicplayer.utils.FormatDuration.formatDuration
import com.example.testprojectmusicplayer.utils.UserObject
import com.example.testprojectmusicplayer.viewModel.HomeViewModel
import com.example.yourappname.utils.PlaylistUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlaylistFragment : Fragment() {

    @Inject
    lateinit var glide: RequestManager
    @Inject
    lateinit var userObject: UserObject
    private val args: PlaylistFragmentArgs by navArgs()

    private val permissionCode = 100
    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var binding: FragmentPlaylistBinding
    private lateinit var bottomBinding: MoreBottomSheetLayoutBinding

    private var isLikedAlbum = false

    private var userId : String? = null
    private var albumId :String? = null
    private var currentSongId = ""

    private var albumDuration = ""
    private val adapter by lazy {
        PlaylistItemsRecyclerView(glide = glide,
            onMoreClicked = { song, pos ->
                settingUpBottomSheet(song)
                currentSongId = song.songId

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
        requestPermissionsLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allPermissionsGranted = permissions.all { it.value }
            if (allPermissionsGranted) {
                foregroundNotification()
            } else {
                PlaylistUtils.showPermissionDeniedDialog(requireContext())
            }
        }



        binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = userObject.getUser()
        userId = user?.userId
        albumId = user?.likedAlbums






        val artist = args.artistId
        val albumId = args.albumId

            if (albumId != null ){

            homeViewModel.getCurrentAlbum(albumId = albumId)
            homeViewModel.isLikedAlbum(albumId =albumId,user?.userId?:"1234")
                handleClickOnLikedAlbum(albumId= albumId, userId = user?.userId?:"1234")
                albumObservers()


                Log.d("AlbumIdArg",albumId.toString())


                 }
        else {
            homeViewModel.getCurrentArtist(artistId = artist?:"")
                homeViewModel.isArtistLiked(artistId = artist?:"", userId = userId?:"")
                handleClickOnLikedArtist(artistId = artist?:"", userId = userId?:"")
                artistObserver()


            }


        bottomBinding = MoreBottomSheetLayoutBinding.inflate(layoutInflater)


        setRecyclerView()


        // Display playlist duration (if you need this as well)


        onClick()
        songObserver()
        observer()

    }

    private fun observer(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.songListState.collect { state ->
                    Log.d("SongListState", state.toString()) // Log state changes
                    when (state) {
                        is UiStates.Loading -> {
                            Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()
                        }
                        is UiStates.Success -> {
                            Log.d("SongListSuccess", state.data.toString())
                            if (state.data.toMutableList().isEmpty()){
                                binding.playlistPlayBtn.visibility = View.GONE
                            }// Debug the data
                            songs = state.data.toMutableList()
                            PlaylistUtils.displayPlaylistDuration(
                                songs = state.data, // Your song list
                                formatDuration = { duration -> formatDuration(duration) }, // Provide the format duration function
                                onDurationCalculated = { formattedDuration ->
                                    binding.playlistDuration.text = formattedDuration // Update the UI with the total playlist duration
                                }
                            )
                            adapter.updateList(state.data.toMutableList())
                        }
                        is UiStates.Failure -> {
                            Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }}
        lifecycleScope.launch {
            homeViewModel.currentSong.collect { currentSong ->
                Log.d("CurrentIndex",currentSong.toString())
                if (currentSong != null){
                    adapter.updateSelection(currentSong.songId)

                }
                else{

                }
            }
        }


        lifecycleScope.launch {
            homeViewModel.isPlaying.collect { isPlaying ->
                isPlayingAll = isPlaying
                val playButtonIcon = if (isPlaying) R.drawable.ic_play else R.drawable.ic_play_button_green
                binding.playlistPlayBtn.setImageResource(playButtonIcon)
            }
        }
    }

    private fun albumObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                homeViewModel.currentAlbum.collect{
                        state ->
                    when(state){
                        is UiStates.Loading -> {
                            Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()

                        }
                        is UiStates.Success -> {
//                                album = state.data
                            Log.d("AlbumData",state.data.toString())
                            //   homeViewModel.songListFunc(state.data.songs)
                            initUi(album = state.data, artist = null)



                        }
                        is UiStates.Failure -> {
                            Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()

                        }
                    }

                }

            }}







        lifecycleScope.launch {
            homeViewModel.isLikedAlbum.collect{
                    state ->
                updateLikeButtonStateAlbum(state)

                isLikedAlbum = state
            }

        }

        lifecycleScope.launch {
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
        lifecycleScope.launch {
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

    }

    private fun songObserver(){
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

        lifecycleScope.launch {
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

    private fun artistObserver(){
        lifecycleScope.launch {
            homeViewModel.currentArtist.collect{
                    state ->
                when(state){
                    is UiStates.Loading -> {
                        Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()


                    }
                    is UiStates.Success -> {
                        Toast.makeText(requireContext(), state.data.toString(), Toast.LENGTH_SHORT).show()
                        initUi(null,state.data)

                    }
                    is UiStates.Failure -> {
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()


                    }
                }
            }

        }
        lifecycleScope.launch {
            homeViewModel.likedArtist.collect{
                    state ->
                when(state){
                    is UiStates.Loading -> {
                        Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()


                    }
                    is UiStates.Success -> {
                        Toast.makeText(requireContext(), state.data.toString(), Toast.LENGTH_SHORT).show()
                        //initUi(null,state.data)

                    }
                    is UiStates.Failure -> {
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()


                    }
                }
            }

        }
        lifecycleScope.launch {
            homeViewModel.unLikedArtist.collect{
                    state ->
                when(state){
                    is UiStates.Loading -> {
                        Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()


                    }
                    is UiStates.Success -> {
                        Toast.makeText(requireContext(), state.data.toString(), Toast.LENGTH_SHORT).show()
                        //  initUi(null,state.data)

                    }
                    is UiStates.Failure -> {
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()


                    }
                }
            }

        }
        lifecycleScope.launch {
            homeViewModel.isLikedArtist.collect{
                    state ->

                updateLikeButtonStateAlbum(state)


            }

        }

    }


    private fun initUi(album: Album?,artist: Artist?){
        when {
            album != null -> {
                glide
                    //.with(binding.psiImage)
                    .load(album.imageUrl)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.default_image) // Replace with your default image resource
                            .error(R.drawable.default_image) // Shown when there is an error loading the image
                    )
                    .into(binding.playlistImage)

                binding.playlistDescription.text = album.descriptions
                binding.playlistDuration.text = albumDuration
            }
            artist != null -> {
                glide
                    //.with(binding.psiImage)
                    .load(artist.imageUrl)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.default_image) // Replace with your default image resource
                            .error(R.drawable.default_image) // Shown when there is an error loading the image
                    )
                    .into(binding.playlistImage)

                binding.playlistDescription.text = artist.name
                binding.playlistDuration.text = albumDuration
            }
        }


    }

    private fun onClick() {
        binding.playlistPlayBtn.setOnClickListener {
            PlaylistUtils.checkAndRequestPermissions(
                context = requireContext(),
                requestPermissionsLauncher = requestPermissionsLauncher,
                onPermissionsGranted = { foregroundNotification() }
            )

        }
        binding.playlistArrowBack.setOnClickListener {
            findNavController().popBackStack()
        }



    }

    private fun setRecyclerView() {
        binding.playlistListItems.adapter = adapter
    }
  private fun handleClickOnLikedSong(){
      this.bottomBinding.moreBottomLinearlayoutLike
          .setOnClickListener {
              if (homeViewModel.isLikedSong.value){
                  homeViewModel.onUnLikedSong(currentSongId,userId?:"", albumId = albumId?:"")
              }
              else{
                  homeViewModel.onLikedSong(currentSongId,userId?:"", albumId = albumId?:"")
              }
          }

  }

    private fun handleClickOnLikedArtist(artistId:String,userId: String){
        binding.playlistLikedBtn.setOnClickListener {
            if (homeViewModel.isLikedArtist.value){
                homeViewModel.onUnlikedLikedArtist(artistId = artistId, userId = userId)
            }
            else{
                homeViewModel.onLikedArtist(artistId = artistId, userId = userId)
            }
        }
    }

    private fun handleClickOnLikedAlbum(albumId: String, userId:String){
        binding.playlistLikedBtn.setOnClickListener {
            Log.d("LikedSong343", isLikedAlbum.toString())
            if (homeViewModel.isLikedAlbum.value){
                homeViewModel.onUnLikedAlbum(albumId,userId)
            }
            else{
                homeViewModel.onLikedAlbum(albumId,userId)
            }

        }

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







    private fun foregroundNotification() {
        if (isPlayingAll) {
            pauseAllSongs()
        } else {
            playAllSongs()
        }
    }




    private fun playSong(pos: Int) {
        lifecycleScope.launch {
            homeViewModel.updateIndex(pos)
            homeViewModel.playSong()
           // adapter.updateSelection(pos)
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


        homeViewModel.isLikedSong(songId = song.songId,userId = userId?:"1234")
        bottomBinding.moreBottomLinearlayoutAddToPlaylist.setOnClickListener {
            val action = PlaylistFragmentDirections.actionPlaylistFragment2ToAddSongFragment(song.songId)
            findNavController().navigate(action)
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

        handleClickOnLikedSong()


        dialog.setCancelable(true)

        // on below line we are setting
        // content view to our view.
        dialog.setContentView(this.bottomBinding.root)

        // on below line we are calling
        // a show method to display a dialog.
        dialog.show()
    }
}
