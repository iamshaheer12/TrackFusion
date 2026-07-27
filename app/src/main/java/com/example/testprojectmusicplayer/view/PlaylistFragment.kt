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
import com.example.testprojectmusicplayer.utils.ShareIntent
import com.example.testprojectmusicplayer.utils.UserObject
import com.example.testprojectmusicplayer.viewModel.HomeViewModel
import com.example.yourappname.utils.PlaylistUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class PlaylistFragment : Fragment() {

    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var userObject: UserObject
    private val args: PlaylistFragmentArgs by navArgs()

    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var binding: FragmentPlaylistBinding
    private lateinit var bottomBinding: MoreBottomSheetLayoutBinding


    private var isLikedAlbum = false
    private var isAlbumLiked = false
    private var isSongLiked = false
    private var isArtistLiked = false
    private var userId: String? = null
    private var albumId: String? = null
    private var currentSongId = ""
    private var currentAlbumId: String? = null
    private var currentArtistId: String? = null
    private var albumDuration = ""
    private var album: Album? = null
    private val adapter by lazy {
        PlaylistItemsRecyclerView(glide = glide,
            onMoreClicked = { song, pos ->
                settingUpBottomSheet(song)
                currentSongId = song.songId

            },

            onItemClicked = { pos, song ->
                Log.d("CurrentIndex", song.toString())
                // playSong(song) // Handles song item click


                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {


                        if (homeViewModel.currentArtistId.value != args.artistId && homeViewModel.currentAlbumId.value != args.albumId) {
                            homeViewModel.resetSongListSent()
                            //homeViewModel.updatePlayPauseState(false)
                        }

                        homeViewModel.updateCurrentSong(song)
                        homeViewModel.updateIndex(pos)
                        homeViewModel.handlePlayPause(args.albumId, args.artistId, song.songId)
                    }
                }

                // homeViewModel.updatePlayPauseState(true)


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
        requestPermissionResult()

        // Fetch user data asynchronously to avoid blocking the main thread
        lifecycleScope.launch(Dispatchers.IO) {
            val user = userObject.getUser()
            userId = user?.userId
            albumId = user?.likedAlbums?.firstOrNull() ?: ""

            val artist = args.artistId
            val albumId = args.albumId

            withContext(Dispatchers.Main) {
                // Set up bindings and UI components on the main thread
                bottomBinding = MoreBottomSheetLayoutBinding.inflate(layoutInflater)
                setRecyclerView()
            }

            // Trigger data fetching and observe the results
            if (albumId != null) {
                // Fetch album data and handle clicks on the album
                homeViewModel.getCurrentAlbum(albumId = albumId)
                homeViewModel.isLikedAlbum(albumId = albumId, user?.userId ?: "1234")
                handleClickOnLikedAlbum(albumId = albumId, userId = user?.userId ?: "1234")
                albumObservers()

                Log.d("AlbumIdArg", albumId.toString())

            } else {
                // Fetch artist data and handle clicks on the artist
                homeViewModel.getCurrentArtist(artistId = artist ?: "")
                homeViewModel.isArtistLiked(artistId = artist ?: "", userId = userId ?: "")
                handleClickOnLikedArtist(artistId = artist ?: "", userId = userId ?: "")
                artistObserver()
            }

            // Observe other LiveData and handle UI updates for playing state
            Log.d("PlayingState2", homeViewModel.isPlaying.value.toString())

            // Trigger any click handlers and observers for songs and other data
            onClick()
            songObserver()
            observer()
        }
    }


    private fun observer() {
        lifecycleScope.launch {

            homeViewModel.songListState.collect { state ->
                Log.d("SongListState", state.toString()) // Log state changes
                when (state) {
                    is UiStates.Loading -> {
                        binding.playlistLoadingScreen.loading.visibility = View.VISIBLE


                        //  Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()
                    }

                    is UiStates.Success -> {


                        Log.d("SongListSuccess", state.data.toString())
                        if (state.data.toMutableList().isEmpty()) {
                            binding.playlistPlayBtn.visibility = View.GONE
                            adapter.updateList(emptyList())
                            binding.playlistLoadingScreen.loading.visibility = View.GONE

                        } else {
                            binding.playlistPlayBtn.visibility = View.VISIBLE
                            songs = state.data.toMutableList()
                            PlaylistUtils.displayPlaylistDuration(
                                songs = state.data, // Your song list
                                formatDuration = { duration -> formatDuration(duration) }, // Provide the format duration function
                                onDurationCalculated = { formattedDuration ->
                                    binding.playlistDuration.text =
                                        formattedDuration // Update the UI with the total playlist duration
                                }
                            )
                            adapter.updateList(state.data.toMutableList())
                            binding.playlistLoadingScreen.loading.visibility = View.GONE
                        }


                    }

                    is UiStates.Failure -> {
                        binding.playlistLoadingScreen.loading.visibility = View.GONE

                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                    }

                    is UiStates.Initial -> {

                    }
                }
            }
        }


        if (args.albumId != null && homeViewModel.currentAlbumId.value == args.albumId || args.artistId != null && homeViewModel.currentArtistId.value == args.artistId){
            Log.d("AlbumId and arg", homeViewModel.currentAlbumId.value.toString()+"  "+args.albumId.toString())
            lifecycleScope.launch {
                homeViewModel.currentSong.collect { currentSong ->
                    Log.d("CurrentSong", currentSong.toString())
                    if (currentSong != null) {
                        adapter.updateSelection(currentSong.songId)

                    } else {

                    }
                }
            }


            lifecycleScope.launch {
                homeViewModel.isPlaying.collect { isPlaying ->
                    isPlayingAll = isPlaying
                    Log.d("PlayingState", isPlaying.toString())
                    val playButtonIcon =
                        if (isPlaying) R.drawable.ic_play else R.drawable.ic_play_button_green
                    binding.playlistPlayBtn.setImageResource(playButtonIcon)
                }
            }
        }



//        if (homeViewModel.currentArtistId.value == args.artistId || homeViewModel.currentAlbumId.value == args.albumId) {
//
//        } else {
//
//
//        }


    }


    //  private fun

    private fun albumObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                homeViewModel.currentAlbum.collect { state ->
                    when (state) {
                        is UiStates.Loading -> {

                            binding.playlistLoadingScreen.loading.visibility = View.VISIBLE
                            //  Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()

                        }

                        is UiStates.Success -> {
//                                album = state.data
                            Log.d("AlbumData", state.data.toString())
                            album = state.data
                            //   homeViewModel.songListFunc(state.data.songs)
                            initUi(album = state.data, artist = null)

                            homeViewModel.songListFunc(state.data?.songs ?: emptyList())


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







        lifecycleScope.launch {
            homeViewModel.isLikedAlbum.collect { state ->
                updateLikeButtonStateAlbum(state)

                isLikedAlbum = state
            }

        }

        lifecycleScope.launch {
            homeViewModel.likedAlbum.collect { state ->
                when (state) {
                    is UiStates.Loading -> {
                        // Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()


                    }

                    is UiStates.Success -> {
                        Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()


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
            homeViewModel.unLikedAlbum.collect { state ->
                when (state) {
                    is UiStates.Loading -> {
                        //   Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()


                    }

                    is UiStates.Success -> {
                        Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()


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
            homeViewModel.currentAlbumId.collect { state ->
                currentAlbumId = state

                if (state == args.albumId) {
                    observer()
                }
            }
        }

    }

    private fun songObserver() {
        lifecycleScope.launch {
            homeViewModel.isLikedSong.collect { state ->

                Log.d("LikedSong343", state.toString())
                updateLikedButtonStateSong(state)


            }

        }

        lifecycleScope.launch {
            homeViewModel.likedSong.collect { state ->
                when (state) {
                    is UiStates.Loading -> {
                        //  Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()


                    }

                    is UiStates.Success -> {
                        //Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()


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
            homeViewModel.unLikedSong.collect { state ->
                when (state) {
                    is UiStates.Loading -> {
                        //  Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()


                    }

                    is UiStates.Success -> {
                        //   Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()


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

    private fun artistObserver() {
        lifecycleScope.launch {
            homeViewModel.currentArtist.collect { state ->
                when (state) {
                    is UiStates.Loading -> {
                        // Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()
                        binding.playlistLoadingScreen.loading.visibility = View.VISIBLE


                    }

                    is UiStates.Success -> {
//                        Toast.makeText(requireContext(), state.data.toString(), Toast.LENGTH_SHORT)
//                            .show()
                        initUi(null, state.data)

                        homeViewModel.songListFunc(state.data?.songs ?: emptyList())

                        //homeViewModel.songListFunc(state.data?.songs)


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
            homeViewModel.likedArtist.collect { state ->
                when (state) {
                    is UiStates.Loading -> {
                        //   Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()


                    }

                    is UiStates.Success -> {
//                        Toast.makeText(requireContext(), state.data.toString(), Toast.LENGTH_SHORT)
//                            .show()
                        //initUi(null,state.data)

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
            homeViewModel.unLikedArtist.collect { state ->
                when (state) {
                    is UiStates.Loading -> {
                        //  Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()


                    }

                    is UiStates.Success -> {
//                        Toast.makeText(requireContext(), state.data.toString(), Toast.LENGTH_SHORT)
//                            .show()
                        //  initUi(null,state.data)

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
            homeViewModel.isLikedArtist.collect { state ->

                updateLikeButtonStateAlbum(state)


            }

        }
        lifecycleScope.launch {
            homeViewModel.currentArtistId.collect { state ->
                currentArtistId = state
                if (state == args.artistId) {
                    observer()
                }
            }
        }

    }


    private fun initUi(album: Album?, artist: Artist?) {
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
                glide
                    //.with(binding.psiImage)
                    .load(album.imageUrl)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.default_image) // Replace with your default image resource
                            .error(R.drawable.default_image) // Shown when there is an error loading the image
                    )
                    .into(binding.playlistGifImage)

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
                glide
                    //.with(binding.psiImage)
                    .load(artist.imageUrl)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.default_image) // Replace with your default image resource
                            .error(R.drawable.default_image) // Shown when there is an error loading the image
                    )
                    .into(binding.playlistGifImage)

                binding.playlistDescription.text = artist.name
                binding.playlistDuration.text = albumDuration
            }
        }


    }

    private fun onClick() {
        binding.playlistPlayBtn.setOnClickListener {

            if (homeViewModel.currentArtistId.value != args.artistId && homeViewModel.currentAlbumId.value != args.albumId) {
                homeViewModel.resetSongListSent()
                if (args.albumId != null){
                    lifecycleScope.launch(Dispatchers.IO) {
                    withContext(Dispatchers.IO) {
                       homeViewModel.addIntoRecentPlay(userId?:"",album?:Album())
                    }
                }
                }

//



                checkAndRequestPermissions()
                //  observer()
            } else {
                checkAndRequestPermissions()
                //observer()
            }

            //  observer()


            Log.d("Pressed onn ", "PlayBtn")


        }
        binding.playlistArrowBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.playlistShareBtn.setOnClickListener {

            Log.d("Click on Share button", "Share button clicked")
            if (args.albumId != null) {

                startActivity(
                    ShareIntent.createSharePlaylistIntent(args.albumId.toString())
                )

            } else {
                startActivity(
                    ShareIntent.createSharePlaylistIntent(args.artistId.toString())
                )

            }
        }


    }

    private fun setRecyclerView() {
        binding.playlistListItems.adapter = adapter
    }

    private fun handleClickOnLikedSong() {
        this.bottomBinding.moreBottomLinearlayoutLike
            .setOnClickListener {
                val isLiked = !homeViewModel.isLikedSong.value
                updateLikedButtonStateSong(isLiked)
                //Log.d("LikedSong343", isLikedSong.toString())

                if (homeViewModel.isLikedSong.value) {
                    homeViewModel.onUnLikedSong(
                        currentSongId,
                        userId ?: "",
                        albumId = albumId ?: ""
                    )
                } else {
                    homeViewModel.onLikedSong(currentSongId, userId ?: "", albumId = albumId ?: "")
                }
            }

    }

    private fun handleClickOnLikedArtist(artistId: String, userId: String) {
        binding.playlistLikedBtn.setOnClickListener {

            val isLiked = !homeViewModel.isLikedArtist.value

            updateLikeButtonStateAlbum(isLiked)

            if (homeViewModel.isLikedArtist.value) {
                homeViewModel.onUnlikedLikedArtist(artistId = artistId, userId = userId)
            } else {
                homeViewModel.onLikedArtist(artistId = artistId, userId = userId)
            }
        }
    }

    private fun handleClickOnLikedAlbum(albumId: String, userId: String) {
        binding.playlistLikedBtn.setOnClickListener {

            val isLiked = !homeViewModel.isLikedAlbum.value
            updateLikeButtonStateAlbum(isLiked)

            Log.d("LikedSong343", isLikedAlbum.toString())
            if (homeViewModel.isLikedAlbum.value) {
                homeViewModel.onUnLikedAlbum(albumId, userId)
            } else {
                homeViewModel.onLikedAlbum(albumId, userId)
            }

        }


    }

    private fun handlePlayPause() {
        lifecycleScope.launch {
            homeViewModel.handlePlayPause(args.albumId, args.artistId, null)
        }
    }


    private fun foregroundNotification() {

        handlePlayPause()

    }


    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    permissionsToRequest.add(android.Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK)
                }
            }
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.FOREGROUND_SERVICE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                permissionsToRequest.add(android.Manifest.permission.FOREGROUND_SERVICE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            foregroundNotification() // Permissions already granted
        }
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
//            if (allPermissionsGranted) {
//                // All required permissions are granted, proceed with functionality
//                //foregroundNotification()
//            } else {
//                // Handle the case where permissions are denied
//                showPermissionDeniedDialog()
//            }
        }

    }


    private fun updateLikeButtonStateAlbum(isLiked: Boolean) {
        if (isLiked) {
            binding.playlistLikedBtn.setImageResource(R.drawable.liked_button)
        } else {
            binding.playlistLikedBtn.setImageResource(R.drawable.ic_unlike)
        }
    }

    private fun updateLikedButtonStateSong(isLiked: Boolean) {
        if (isLiked) {
            this.bottomBinding.moreBottomSheetLikeText.text = "Remove from liked Song"
            this.bottomBinding.moreBottomSheetLikeIcn.setImageResource(R.drawable.liked_button)


        } else {
            this.bottomBinding.moreBottomSheetLikeText.text = "Add to Liked Songs"
            this.bottomBinding.moreBottomSheetLikeIcn.setImageResource(R.drawable.ic_unlike)


        }

    }


    @SuppressLint("InflateParams")
    private fun settingUpBottomSheet(song: Song) {
        val dialog = BottomSheetDialog(requireContext())
        // Check if the binding is already initialized and remove it from its parent
        if (::bottomBinding.isInitialized && bottomBinding.root.parent != null) {
            (bottomBinding.root.parent as? ViewGroup)?.removeView(bottomBinding.root)
        } else {
            // Initialize the binding only if it's not already initialized
            bottomBinding = MoreBottomSheetLayoutBinding.inflate(layoutInflater)
        }

        lifecycleScope.launch {
            homeViewModel.isLikedSong(songId = song.songId, userId = userId ?: "1234")

        }


        bottomBinding.moreBottomLinearlayoutAddToPlaylist.setOnClickListener {
            val action =
                PlaylistFragmentDirections.actionPlaylistFragment2ToAddSongFragment(song.songId)
            findNavController().navigate(action)
            dialog.dismiss()
        }

        bottomBinding.moreBottomLinearlayoutShare.setOnClickListener {
            startActivity(
                ShareIntent.createSharePlaylistIntent(song.songId)
            )
            dialog.dismiss()
        }


        // on below line we are inflating a layout file which we have created.
        bottomBinding.moreBottomLinearlayoutDeletePlaylist.visibility = View.GONE
        bottomBinding.moreBottomLinearlayoutEditPlaylist.visibility = View.GONE
        bottomBinding.moreBottomLinearlayoutRemoveFromPlaylist.visibility = View.GONE

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
