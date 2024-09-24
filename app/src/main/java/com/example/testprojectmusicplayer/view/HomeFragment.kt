package com.example.testprojectmusicplayer.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.adapters.ArtistRecyclerViewAdapter
import com.example.testprojectmusicplayer.adapters.HomeGetStartedRecyclerViewAdapter
import com.example.testprojectmusicplayer.adapters.MusicRecyclerViewAdapter
import com.example.testprojectmusicplayer.adapters.RecentGridAdapter
import com.example.testprojectmusicplayer.adapters.RecentlyPlayedRecyclerViewAdapter
import com.example.testprojectmusicplayer.databinding.FragmentHomeBinding
import com.example.testprojectmusicplayer.model.Album
import com.example.testprojectmusicplayer.model.RecentPlayCard
import com.example.testprojectmusicplayer.model.Song
import com.example.testprojectmusicplayer.utils.MusicItem
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

    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by activityViewModels()

    private lateinit var recentGridAdapter: RecentGridAdapter
    private val getStartedAdapter by lazy {
        HomeGetStartedRecyclerViewAdapter(glide = glide,
            onItemClicked = { _, album->

                val action =HomeFragmentDirections.actionHomeFragmentToPlaylistFragment2(

                    album.id,null
                )
                findNavController().navigate(action)

            }
        )

    }
    private val recentPlayAdapter by lazy {
        RecentlyPlayedRecyclerViewAdapter(glide = glide,onItemClicked = {pos,album ->
            val action =HomeFragmentDirections.actionHomeFragmentToPlaylistFragment2(

                album.id,null
            )
            findNavController().navigate(action)
        })

    }
    private val recommendedAdapter by lazy {
        MusicRecyclerViewAdapter(glide = glide,onItemClicked =  {

        })


    }

    private val artistAdapter by lazy {
        ArtistRecyclerViewAdapter(glide = glide,
            onItemClick = {_,artist ->
                val action =HomeFragmentDirections.actionHomeFragmentToPlaylistFragment2(
                    null,artist.id
                )
                findNavController().navigate(action)

        })

    }
    private var albumList: List<Album> = emptyList()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        homeViewModel.getStartedAlbums()
        homeViewModel.getRecentPlayedSongs()
        homeViewModel.getArtists()
        homeViewModel.getRecentPlayedSongs()

        homeViewModel.getRecentPlayedAlbum(userObject.getUser()?.userId?:"")




        settingAdapters()
        observers()



    }

    private fun settingAdapters() {

        binding.hmGetStartedRecyclerview.adapter = getStartedAdapter
        binding.hmRecentPlayedRecyclerview.adapter = recentPlayAdapter
        binding.hmRecommendedRecyclerview.adapter = recommendedAdapter
        binding.hmArtistRecyclerview.adapter = artistAdapter

        recentPlayGrid =binding.hmRecentPlayGrid
        recentPlayGrid.numColumns = 2

       recentGridAdapter = RecentGridAdapter(glide = glide, context = requireContext(), recentPlayList = emptyList())
        recentPlayGrid.adapter = recentGridAdapter


    }

    private fun observers(){
        lifecycleScope.launch {
           repeatOnLifecycle(Lifecycle.State.STARTED){
                homeViewModel.allAlbumsState.collect { state ->
                    when (state) {
                        is UiStates.Loading -> {
                            // Show loading indicator
                        }
                        is UiStates.Success -> {
                            Log.e("SongsData",state.data.toMutableList().toString())
                            binding.loadingScreen.loading.visibility = View.GONE
                            getStartedAdapter.updateList(state.data.toMutableList())

                        }
                        is UiStates.Failure -> {
                            // Show error message
                            Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }

        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                homeViewModel.getRecentPlayedAlbum.collect { state ->
                    when (state) {
                        is UiStates.Loading -> {
                            // Show loading indicator
                        }
                        is UiStates.Success -> {
                                    Log.d("RecentPlayedSong",state.data.toString())
                            if (state.data.toMutableList().isEmpty()){
                                binding.hmRecentlyPlayedTxt.visibility = View.GONE
                            }


                            recentPlayAdapter.updateList(state.data.toMutableList())
                            recentGridAdapter.updateList(state.data.toMutableList())

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





//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED){
//                homeViewModel.allAlbumsState.collect { state ->
//                    when (state) {
//                        is UiStates.Loading -> {
//                            // Show loading indicator
//                        }
//                        is UiStates.Success -> {
//                            albumList = state.data.toMutableList()
//                            Log.e("AlbumData",state.data.toMutableList().toString())
//                            binding.loadingScreen.loading.visibility = View.GONE
//
//
//                            // Navigate to the next screen or show success message
//                        }
//                        is UiStates.Failure -> {
//                            // Show error message
//                            Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
//                        }
//
//                    }
//                }
//
//            }
//
//        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                homeViewModel.allArtistsState.collect { state ->
                    when (state) {
                        is UiStates.Loading -> {
                            // Show loading indicator
                            Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()

                        }
                        is UiStates.Success -> {
                            Log.e("ArtistData",state.data.toMutableList().toString())
                            Toast.makeText(requireContext(), "Successfully"+state.data.toMutableList().toString(), Toast.LENGTH_SHORT).show()

                            binding.loadingScreen.loading.visibility = View.GONE

                            artistAdapter.updateList(state.data.toMutableList())
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








}