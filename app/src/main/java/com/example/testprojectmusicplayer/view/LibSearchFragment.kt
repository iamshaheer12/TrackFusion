package com.example.testprojectmusicplayer.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.RequestManager
import com.example.testprojectmusicplayer.adapters.LibraryRecyclerViewAdapter
import com.example.testprojectmusicplayer.adapters.OnItemClickListener1
import com.example.testprojectmusicplayer.databinding.FragmentLibSearchBinding
import com.example.testprojectmusicplayer.utils.AlbumArtist
import com.example.testprojectmusicplayer.utils.UiStates
import com.example.testprojectmusicplayer.viewModel.LibraryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LibSearchFragment : Fragment(),OnItemClickListener1 {
    private lateinit var binding: FragmentLibSearchBinding
    private val viewModel: LibraryViewModel by viewModels()
    @Inject
    lateinit var glide: RequestManager

    private lateinit var adapter: LibraryRecyclerViewAdapter




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLibSearchBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingAdapter()
        viewModel.getArtist()
        viewModel.getStartedAlbums()
        observers()
        setupLiveSearch()

    }


    private fun settingAdapter(){
        adapter = LibraryRecyclerViewAdapter(emptyList(),this, glide = glide)
        binding.recyclerViewLibSearchBar.adapter = adapter

    }

    private fun observers(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.filteredAlbumArtist.collect{
                        state ->
                    when(state){
                        is UiStates.Loading ->{
                        }
                        is UiStates.Success ->{
                            adapter.updateItems(state.data.toMutableList())
                            binding.libSearchProgressBar.visibility = View.GONE

                        }

                        is UiStates.Failure ->{


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

    override fun onItemClick(item: AlbumArtist) {

        when (item) {
            is AlbumArtist.AlbumItem -> {
                Log.d("",item.album.toString())
                // Handle album item click
            }
            is AlbumArtist.ArtistItem -> {
                Log.d("",item.artist.toString())

                // Handle artist item click
            }

        }
    }
}