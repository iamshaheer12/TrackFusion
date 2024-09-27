package com.example.testprojectmusicplayer.view

import com.example.testprojectmusicplayer.adapters.MediaAdapter
import com.example.testprojectmusicplayer.utils.MediaItem
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.adapters.OnItemClickListener
import com.example.testprojectmusicplayer.databinding.FragmentSearchScreen2Binding
import com.example.testprojectmusicplayer.utils.UiStates
import com.example.testprojectmusicplayer.viewModel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchScreen2Fragment : Fragment(), OnItemClickListener {
    private lateinit var binding: FragmentSearchScreen2Binding
    private lateinit var mediaAdapter: MediaAdapter
    @Inject
    lateinit var glide: RequestManager


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
        setupLiveSearch()
        onclick()

    }

    private fun setUpAdapter() {
        // Initialize the adapter with the listener and empty list
        mediaAdapter = MediaAdapter(emptyList(), this, glide = glide)
        binding.recyclerViewSearchBar.adapter = mediaAdapter
    }

    private fun observers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredMediaItems.collect { state ->
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
                        is UiStates.Initial ->{

                        }
                    }
                }
            }
        }
    }
    private fun setupLiveSearch() {
        binding.searchText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // No action needed here
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed here
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                viewModel.filterMediaItems(query) // Trigger filtering in the ViewModel
            }
        })
    }

    private fun onclick(){
        binding.searchArrowBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
    override fun onItemClick(item: MediaItem) {
        when (item) {
            is MediaItem.AlbumItem -> {
                val action = SearchScreen2FragmentDirections.actionSearchScreen2FragmentToPlaylistFragment2(item.album.id,null)

              findNavController().navigate(action)
                // Handle album item click
            }
            is MediaItem.ArtistItem -> {
                Log.d("SongsData",item.artist.toString())
                val action = SearchScreen2FragmentDirections.actionSearchScreen2FragmentToPlaylistFragment2(null,item.artist.id)

                findNavController().navigate(action)

                // Handle artist item click
            }
            is MediaItem.SongItem -> {
                // Handle song item click
                Log.d("SongsData",item.song.toString())

            }

        }
    }
}
