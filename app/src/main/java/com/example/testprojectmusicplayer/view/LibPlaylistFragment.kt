package com.example.testprojectmusicplayer.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
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
import com.example.testprojectmusicplayer.databinding.FragmentLibPlaylistBinding
import com.example.testprojectmusicplayer.databinding.MoreBottomSheetLayoutBinding
import com.example.testprojectmusicplayer.model.Album
import com.example.testprojectmusicplayer.model.Artist
import com.example.testprojectmusicplayer.model.Song
import com.example.testprojectmusicplayer.utils.FormatDuration.formatDuration
import com.example.testprojectmusicplayer.utils.ShareIntent
import com.example.testprojectmusicplayer.utils.UiStates
import com.example.testprojectmusicplayer.utils.UserObject
import com.example.testprojectmusicplayer.viewModel.HomeViewModel
import com.example.yourappname.utils.PlaylistUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class LibPlaylistFragment : Fragment() {
    private lateinit var binding: FragmentLibPlaylistBinding

    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var userObject: UserObject

    private val args: LibPlaylistFragmentArgs by navArgs()
    private lateinit var bottomBinding: MoreBottomSheetLayoutBinding
    private lateinit var playListBottomSheet: MoreBottomSheetLayoutBinding
    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>


    private var isLikedAlbum = false

    private var userId: String? = null
    private var albumId: String? = null
    private var currentSongId = ""
    private var albumDuration = ""
    private var album: Album? = null

    private var isAlbumDeleted = false
    private var artist: Artist? = null

    private val adapter by lazy {
        PlaylistItemsRecyclerView(glide = glide,
            onMoreClicked = { song, _ ->
                settingUpBottomSheet(song)
                currentSongId = song.songId

            },
            onItemClicked = { pos, song ->


                if (homeViewModel.currentArtistId.value != args.artistId && homeViewModel.currentAlbumId.value != args.albumId) {
                    homeViewModel.resetSongListSent()
                    //homeViewModel.updatePlayPauseState(false)
                }

                homeViewModel.updateCurrentSong(song)
                homeViewModel.updateIndex(pos)

// Run the long-running operation on IO dispatcher
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        homeViewModel.handlePlayPause(args.albumId, args.artistId, song.songId)
                    }
                }

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


        binding = FragmentLibPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("LibPlaylistFragment", "onViewCreated: ")

        // Initialize user data asynchronously to avoid UI blocking
        lifecycleScope.launch(Dispatchers.IO) {
            val user = userObject.getUser()
            userId = user?.userId
            albumId = user?.likedAlbums?.firstOrNull() ?: ""
            // Now switch to Main thread to perform UI operations
            withContext(Dispatchers.Main) {
                // Initialize bottomBinding and playListBottomSheet after loading user data
                bottomBinding = MoreBottomSheetLayoutBinding.inflate(layoutInflater)
                playListBottomSheet = MoreBottomSheetLayoutBinding.inflate(layoutInflater)
                setRecyclerView()
                onMoreClick()
            }


            val artist = args.artistId
            val albumId = args.albumId

            // Set up observers before making data fetch calls (on the main thread)


            // Trigger ViewModel data fetches based on whether albumId or artistId is available
            if (albumId != null) {
                Log.d("AlbumIdArg", albumId.toString())

                // Fetch album data asynchronously
                homeViewModel.getCurrentAlbum(albumId = albumId)
                homeViewModel.isLikedAlbum(albumId = albumId, user?.userId ?: "1234")

                // Set up album observers after triggering fetch
                albumObservers()
                removeSongObserver()

            } else {
                // Fetch artist data asynchronously
                homeViewModel.getCurrentArtist(artistId = artist ?: "")
                homeViewModel.isArtistLiked(artistId = artist ?: "", userId = userId ?: "")

                // Set up artist observers after triggering fetch
                artistObserver()
            }

            observer()
            songObserver()


            // Set up RecyclerView and click listeners on the main thread
            handleClickOnLikedSong()
            // setRecyclerView()
            onClick()
        }

    }


    private fun observer() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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
        }
        if (args.albumId != null && homeViewModel.currentAlbumId.value == args.albumId || args.artistId != null && homeViewModel.currentArtistId.value == args.artistId) {
            lifecycleScope.launch {
                homeViewModel.currentSong.collect { currentSong ->
                    Log.d("CurrentIndex", currentSong.toString())
                    if (currentSong != null) {
                        adapter.updateSelection(currentSong.songId)

                    }

                }
            }


            lifecycleScope.launch {
                homeViewModel.isPlaying.collect { isPlaying ->
                    isPlayingAll = isPlaying
                    val playButtonIcon =
                        if (isPlaying) R.drawable.ic_play else R.drawable.ic_play_button_green
                    binding.playlistPlayBtn.setImageResource(playButtonIcon)
                }
            }
        }
    }

    private fun albumObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                homeViewModel.currentAlbum.collect { state ->
                    when (state) {
                        is UiStates.Loading -> {
                            binding.playlistLoadingScreen.loading.visibility = View.VISIBLE

                            //Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()

                        }

                        is UiStates.Success -> {
//                                album = state.data
                            Log.d("AlbumData", state.data.toString())
                            //   homeViewModel.songListFunc(state.data.songs)
                            initUi(album = state.data, artist = null)
                            album = state.data

                            if (state.data?.visibility == false) {
                                binding.playlistShareBtn.visibility = View.GONE
                            }
                            binding.playlistLoadingScreen.loading.visibility = View.GONE
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

            homeViewModel.deleteAlbum.collectLatest { state ->
                when (state) {
                    is UiStates.Loading -> {
                        //Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()
                    }

                    is UiStates.Success -> {
                        Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()

                        homeViewModel.resetDeleteAlbumState()

                        isAlbumDeleted = true
                        if (args.albumId == homeViewModel.currentAlbumId.value) {
                            homeViewModel.resetSongListSent()


                            findNavController().popBackStack()
                        } else {
                            findNavController().popBackStack()
                        }


                    }

                    is UiStates.Failure -> {
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()

                        //  isAlbumDeleted = true
                        homeViewModel.resetDeleteAlbumState()

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
                        // Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()
                        binding.playlistLoadingScreen.loading.visibility = View.VISIBLE


                    }

                    is UiStates.Success -> {
                        binding.playlistLoadingScreen.loading.visibility = View.GONE
                        homeViewModel.resetLikeAlbumState()

                        if (args.albumId == homeViewModel.currentAlbumId.value) {
                            homeViewModel.resetSongListSent()
                            findNavController().popBackStack()
                        } else {
                            findNavController().popBackStack()
                        }


                    }

                    is UiStates.Failure -> {
                        binding.playlistLoadingScreen.loading.visibility = View.GONE
                        homeViewModel.resetLikeAlbumState()


                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()


                    }

                    is UiStates.Initial -> {

                    }
                }
            }


        }


    }

    private fun removeSongObserver() {
        lifecycleScope.launch() {
            homeViewModel.removeSongFromPlaylist.collect { state ->
                when (state) {
                    is UiStates.Loading -> {
                        //withContext(Dispatchers.Main) {
                        binding.playlistLoadingScreen.loading.visibility = View.VISIBLE

                        // }


                    }

                    is UiStates.Success -> {
                        // Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()

                        binding.playlistLoadingScreen.loading.visibility = View.GONE
                        //homeViewModel.getCurrentAlbum(albumId = args.albumId?:"")
                        // homeViewModel.

                        if (args.albumId == homeViewModel.currentAlbumId.value) {


                            homeViewModel.resetSongListSent()

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
    }

    private fun songObserver() {
        lifecycleScope.launch {
            homeViewModel.isLikedSong.collect { state ->
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
                        // Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()


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
                        // Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()


                    }

                    is UiStates.Success -> {
                        // Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()


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
                        binding.playlistLoadingScreen.loading.visibility = View.VISIBLE

                        //Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()


                    }

                    is UiStates.Success -> {
//                        Toast.makeText(requireContext(), state.data.toString(), Toast.LENGTH_SHORT)
//                            .show()
                        initUi(null, state.data)
                        artist = state.data
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

        lifecycleScope.launch {
            homeViewModel.unLikedArtist.collect { state ->
                when (state) {
                    is UiStates.Loading -> {

                        binding.playlistLoadingScreen.loading.visibility = View.VISIBLE
                        // Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()

                    }


                    is UiStates.Success -> {
                        Toast.makeText(requireContext(), state.data.toString(), Toast.LENGTH_SHORT)
                            .show()
                        homeViewModel.resetLikeArtistState()
                        binding.playlistLoadingScreen.loading.visibility = View.GONE


                        if (args.artistId == homeViewModel.currentArtistId.value) {
                            homeViewModel.resetSongListSent()
                            findNavController().popBackStack()
                        } else {
                            findNavController().popBackStack()
                        }


                    }

                    is UiStates.Failure -> {
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                        homeViewModel.resetLikeArtistState()

                        binding.playlistLoadingScreen.loading.visibility = View.GONE

                    }

                    is UiStates.Initial -> {

                    }
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

                binding.playlistShareBtn.setImageResource(R.drawable.ic_more_vert)


                binding.playlistLikedBtn.visibility = View.GONE


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

                binding.playlistShareBtn.setImageResource(R.drawable.ic_more_vert)

                binding.playlistLikedBtn.visibility = View.GONE
            }
        }


    }


    private fun onClick() {
        binding.playlistPlayBtn.setOnClickListener {


            if (homeViewModel.currentArtistId.value != args.artistId && homeViewModel.currentAlbumId.value != args.albumId) {
                homeViewModel.resetSongListSent()
//                lifecycleScope.launch(Dispatchers.IO) {
//                    withContext(Dispatchers.IO){
//                        if (args.albumId != null){
//                            homeViewModel.addIntoRecentPlay(userId?:"",args.albumId)
//                        }
//                    }
//                }

                checkAndRequestPermissions()
                // observer()
            } else {
                checkAndRequestPermissions()
                //observer()
            }

            Log.d("Pressed onn ", "PlayBtn")

        }
        binding.playlistArrowBack.setOnClickListener {
            findNavController().popBackStack()
        }


    }

    private fun setRecyclerView() {
        binding.playlistListItems.adapter = adapter
    }

    private fun handleClickOnLikedSong() {
        this.bottomBinding.moreBottomLinearlayoutLike
            .setOnClickListener {
                val value = !homeViewModel.isLikedSong.value
                updateLikedButtonStateSong(value)


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


    private fun updateLikedButtonStateSong(isLiked: Boolean) {
        if (isLiked) {
            this.bottomBinding.moreBottomSheetLikeText.text = "Remove from liked Song"
            this.bottomBinding.moreBottomSheetLikeIcn.setImageResource(R.drawable.liked_button)


        } else {
            this.bottomBinding.moreBottomSheetLikeText.text = "Add to Liked Songs"
            this.bottomBinding.moreBottomSheetLikeIcn.setImageResource(R.drawable.ic_unlike)


        }

    }

    private fun onMoreClick() {


        binding.playlistShareBtn.setOnClickListener {
            if (args.albumId != null) {
                handleBottomSheetForPlayList(album = album ?: Album())
            } else {
                handleBottomSheetForArtist(artist = artist ?: Artist())
            }
            //  album?.let { handleBottomSheetForPlayList(it) }
        }
    }

    private fun handleBottomSheetForArtist(artist: Artist) {
        val dialog = BottomSheetDialog(requireContext())

        if (::playListBottomSheet.isInitialized && playListBottomSheet.root.parent != null) {
            (playListBottomSheet.root.parent as? ViewGroup)?.removeView(playListBottomSheet.root)
        } else {
            // Initialize the binding only if it's not already initialized
            playListBottomSheet = MoreBottomSheetLayoutBinding.inflate(layoutInflater)
        }

        playListBottomSheet.moreBottomLinearlayoutAddToPlaylist.visibility = View.GONE
        playListBottomSheet.moreBottomLinearlayoutLike.visibility = View.GONE
        playListBottomSheet.moreBottomLinearlayoutRemoveFromPlaylist.visibility = View.GONE

        playListBottomSheet.moreBottomLinearlayoutEditPlaylist.visibility = View.GONE
        playListBottomSheet.moreBottomLinearlayoutDeletePlaylist.setOnClickListener {


            homeViewModel.onUnlikedLikedArtist(artistId = artist.id, userId = userId ?: "")
            dialog.dismiss()
        }

        playListBottomSheet.moreBottomLinearlayoutShare.setOnClickListener {
            startActivity(
                ShareIntent.createSharePlaylistIntent(artist.id.toString())
            )
            dialog.dismiss()

        }





        glide
            .load(artist.imageUrl)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.default_image) // Replace with your default image resource
                    .error(R.drawable.default_image) // Shown when there is an error loading the image
            )
            .into(playListBottomSheet.moreBottomSheetImage)

        playListBottomSheet.moreBottomSheetSongTitle.text = artist.name

        playListBottomSheet.moreBottomSheetSongDescription.text = "Artist"

        dialog.setContentView(playListBottomSheet.root)
        dialog.show()

    }


    private fun handleBottomSheetForPlayList(album: Album) {
        val dialog = BottomSheetDialog(requireContext())

        if (::playListBottomSheet.isInitialized && playListBottomSheet.root.parent != null) {
            (playListBottomSheet.root.parent as? ViewGroup)?.removeView(playListBottomSheet.root)
        } else {
            // Initialize the binding only if it's not already initialized
            playListBottomSheet = MoreBottomSheetLayoutBinding.inflate(layoutInflater)
        }

        playListBottomSheet.moreBottomLinearlayoutAddToPlaylist.visibility = View.GONE
        playListBottomSheet.moreBottomLinearlayoutLike.visibility = View.GONE
        playListBottomSheet.moreBottomLinearlayoutRemoveFromPlaylist.visibility = View.GONE

        if (album.createdBy != (userId ?: "")) {
            playListBottomSheet.moreBottomLinearlayoutEditPlaylist.visibility = View.GONE
        } else {
            playListBottomSheet.moreBottomLinearlayoutEditPlaylist.setOnClickListener {

                val action =
                    LibPlaylistFragmentDirections.actionLibPlaylistFragmentToCreatePlaylistFragment2(
                        album
                    )
                findNavController().navigate(action)
                dialog.dismiss()


            }
        }



        playListBottomSheet.moreBottomLinearlayoutDeletePlaylist.setOnClickListener {
            isAlbumDeleted = false

            if (album.createdBy == userObject.getUser()?.userId) {
                homeViewModel.deleteAlbum(id = album.id)
                dialog.dismiss()

            } else {
                homeViewModel.onUnLikedAlbum(albumId = album.id, userId = userId ?: "")
                dialog.dismiss()


                //   homeViewModel.
            }


        }

        playListBottomSheet.moreBottomLinearlayoutShare.setOnClickListener {
            startActivity(
                ShareIntent.createSharePlaylistIntent(args.albumId.toString())
            )
            dialog.dismiss()

        }





        glide
            .load(album.imageUrl)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.default_image) // Replace with your default image resource
                    .error(R.drawable.default_image) // Shown when there is an error loading the image
            )
            .into(playListBottomSheet.moreBottomSheetImage)

        playListBottomSheet.moreBottomSheetSongTitle.text = album.title

        playListBottomSheet.moreBottomSheetSongDescription.text = album.descriptions

        dialog.setContentView(playListBottomSheet.root)
        dialog.show()

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


        // on below line we are inflating a layout file which we have created.
        bottomBinding.moreBottomLinearlayoutDeletePlaylist.visibility = View.GONE
        bottomBinding.moreBottomLinearlayoutEditPlaylist.visibility = View.GONE

        if (album?.visibility == false
        //||
        //album?.createdBy != userId
        ) {
            bottomBinding.moreBottomLinearlayoutLike.visibility = View.GONE


        } else if (album?.createdBy == userId) {
            bottomBinding.moreBottomLinearlayoutRemoveFromPlaylist.setOnClickListener {
                lifecycleScope.launch {
                    homeViewModel.removeSongFromAlbum(
                        albumId = args.albumId ?: "",
                        songId = song.songId
                    )

                }
                dialog.dismiss()
            }
        }



        if (args.artistId != null) {
            bottomBinding.moreBottomLinearlayoutRemoveFromPlaylist.visibility = View.GONE

        }



        bottomBinding.moreBottomLinearlayoutAddToPlaylist.setOnClickListener {
            val action =
                LibPlaylistFragmentDirections.actionLibPlaylistFragmentToAddSongFragment(song.songId)
            findNavController().navigate(action)
            dialog.dismiss()

        }

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