package com.example.testprojectmusicplayer.view

import MediaAdapter
import MediaItem
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.adapters.OnItemClickListener
import com.example.testprojectmusicplayer.databinding.FragmentSearchScreen2Binding
import com.example.testprojectmusicplayer.utils.UiStates
import com.example.testprojectmusicplayer.viewModel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
@AndroidEntryPoint
class SearchScreen2Fragment : Fragment(), OnItemClickListener {
    private lateinit var binding: FragmentSearchScreen2Binding
    private lateinit var mediaAdapter: MediaAdapter

    private val viewModel: SearchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchScreen2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getArtists()
        viewModel.getStartedAlbums()
        viewModel.getRecentPlayedSongs()
        setUpAdapter()
        observers()
    }

    private fun setUpAdapter() {
        // Initialize the adapter with the listener and empty list
        mediaAdapter = MediaAdapter(emptyList(), this)
        binding.recyclerViewSearchBar.adapter = mediaAdapter
    }

    private fun observers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.combinedMediaItems.collect { state ->
                    when (state) {
                        is UiStates.Loading -> {
                            // Show loading indicator
                            binding.searchProgressBar.visibility = View.VISIBLE
                        }
                        is UiStates.Success -> {
                            binding.searchProgressBar.visibility = View.GONE
                            mediaAdapter.updateItems(state.data.toMutableList())
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

    override fun onItemClick(item: MediaItem) {
        when (item) {
            is MediaItem.AlbumItem -> {
                Log.d("SongsData",item.album.toString())
                // Handle album item click
            }
            is MediaItem.ArtistItem -> {
                Log.d("SongsData",item.artist.toString())

                // Handle artist item click
            }
            is MediaItem.SongItem -> {
                // Handle song item click
                Log.d("SongsData",item.song.toString())

            }

            else -> {}
        }
    }
}
