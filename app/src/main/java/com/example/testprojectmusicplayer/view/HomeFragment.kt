package com.example.testprojectmusicplayer.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.example.testprojectmusicplayer.adapters.ArtistRecyclerViewAdapter
import com.example.testprojectmusicplayer.adapters.HomeGetStartedRecyclerViewAdapter
import com.example.testprojectmusicplayer.adapters.MusicRecyclerViewAdapter
import com.example.testprojectmusicplayer.adapters.RecentlyPlayedRecyclerViewAdapter
import com.example.testprojectmusicplayer.databinding.FragmentHomeBinding
import com.example.testprojectmusicplayer.model.Album
import com.example.testprojectmusicplayer.model.Artist
import com.example.testprojectmusicplayer.utils.UiStates
import com.example.testprojectmusicplayer.utils.UserObject
import com.example.testprojectmusicplayer.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var recentPlayGrid: GridView

    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var userObject: UserObject
    private var isAlbumLoading = true
    private var isRecentSongsLoading = true
    private var isArtistLoading = true


    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by activityViewModels()


    //private lateinit var recentGridAdapter: RecentGridAdapter
    private val getStartedAdapter by lazy {
        HomeGetStartedRecyclerViewAdapter(glide = glide,
            onItemClicked = { _, album ->

                val action = HomeFragmentDirections.actionHomeFragmentToPlaylistFragment2(

                    album.id, null
                )
                findNavController().navigate(action)

            }
        )

    }
    private val recentPlayAdapter by lazy {
        RecentlyPlayedRecyclerViewAdapter(glide = glide, onItemClicked = { pos, album ->
            val action = HomeFragmentDirections.actionHomeFragmentToPlaylistFragment2(

                album.id, null
            )
            findNavController().navigate(action)
        })

    }
    private val recommendedAdapter by lazy {
        MusicRecyclerViewAdapter(glide = glide, onItemClicked = {

        })


    }

    private val artistAdapter by lazy {
        ArtistRecyclerViewAdapter(glide = glide,
            onItemClick = { _, artist ->
                val action = HomeFragmentDirections.actionHomeFragmentToPlaylistFragment2(
                    null, artist.id
                )
                findNavController().navigate(action)

            })

    }

    private var albumList: List<Album> = emptyList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        homeViewModel.getStartedAlbums()
        // homeViewModel.getRecentPlayedSongs()
        homeViewModel.getArtists()

        homeViewModel.getRecentPlayedAlbum(userObject.getUser()?.userId ?: "")




        settingAdapters()
        observers()


    }

    private fun settingAdapters() {

        binding.hmGetStartedRecyclerview.adapter = getStartedAdapter
        binding.hmRecentPlayedRecyclerview.adapter = recentPlayAdapter
        binding.hmRecommendedRecyclerview.adapter = recommendedAdapter
        binding.hmArtistRecyclerview.adapter = artistAdapter

        recentPlayGrid = binding.hmRecentPlayGrid
        recentPlayGrid.numColumns = 2

//        recentGridAdapter = RecentGridAdapter(
//            glide = glide,
//            context = requireContext(),
//            recentPlayList = emptyList()
//        )
      // recentPlayGrid.adapter = recentGridAdapter


    }

    private fun observers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.allAlbumsState.collect { state ->
                    handleAlbumsState(state)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.getRecentPlayedAlbum.collect { state ->
                    handleRecentAlbumState(state)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.allArtistsState.collect { state ->
                    handleArtistsState(state)
                }
            }
        }
    }

    private fun handleAlbumsState(state: UiStates<List<Album>>) {
        when (state) {
            is UiStates.Loading -> {
                isAlbumLoading = true
                showLoadingIfNeeded()
            }

            is UiStates.Success -> {
                isAlbumLoading = false
                getStartedAdapter.updateList(state.data.toMutableList())
                if (state.data.isEmpty()) {
                    binding.hmGetStartedTxt.visibility = View.GONE
                } else {
                    binding.hmGetStartedTxt.visibility = View.VISIBLE

                }
                hideLoadingIfNeeded()
            }

            is UiStates.Failure -> {
                isAlbumLoading = false
                hideLoadingIfNeeded()
                Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
            }

            is UiStates.Initial -> {

            }
        }
    }

    private fun handleRecentAlbumState(state: UiStates<List<Album>>) {
        when (state) {
            is UiStates.Loading -> {
                isRecentSongsLoading = true
                showLoadingIfNeeded()
            }

            is UiStates.Success -> {
                isRecentSongsLoading = false
                if (state.data.isEmpty()) {
                    binding.hmRecentlyPlayedTxt.visibility = View.GONE
                } else {
                    binding.hmRecentlyPlayedTxt.visibility = View.GONE

                }
              //  recentPlayAdapter.updateList(state.data.toMutableList())
             //   recentGridAdapter.updateList(state.data.toMutableList())
                hideLoadingIfNeeded()
            }

            is UiStates.Failure -> {
                isRecentSongsLoading = false
                hideLoadingIfNeeded()
                Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
            }

            is UiStates.Initial -> {

            }
        }
    }

    private fun handleArtistsState(state: UiStates<List<Artist>>) {
        when (state) {
            is UiStates.Loading -> {
                isArtistLoading = true
                showLoadingIfNeeded()
            }

            is UiStates.Success -> {
                isArtistLoading = false

                artistAdapter.updateList(state.data.toMutableList())

                if (state.data.isEmpty()) {
                    binding.hmArtistsTxt.visibility = View.GONE
                } else {
                    binding.hmArtistsTxt.visibility = View.VISIBLE

                }
                hideLoadingIfNeeded()
            }

            is UiStates.Failure -> {
                isArtistLoading = false
                hideLoadingIfNeeded()
                Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
            }

            is UiStates.Initial -> {

            }
        }
    }

    private fun showLoadingIfNeeded() {
        if (isAlbumLoading || isRecentSongsLoading || isArtistLoading) {
            binding.loadingScreen12.visibility = View.VISIBLE

        }

    }

    private fun hideLoadingIfNeeded() {
        if (!isAlbumLoading && !isRecentSongsLoading && !isArtistLoading) {
            //view?.findViewById<ConstraintLayout>(R.id.loading_screen12)?.visibility = View.GONE
            binding.loadingScreen12.visibility = View.GONE


        }
    }


}