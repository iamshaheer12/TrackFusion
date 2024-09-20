package com.example.testprojectmusicplayer.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.adapters.LibraryRecyclerViewAdapter
import com.example.testprojectmusicplayer.adapters.OnItemClickListener1
import com.example.testprojectmusicplayer.databinding.FragmentLibraryBinding
import com.example.testprojectmusicplayer.utils.AlbumArtist
import com.example.testprojectmusicplayer.utils.MediaItem
import com.example.testprojectmusicplayer.utils.UiStates
import com.example.testprojectmusicplayer.utils.UserObject
import com.example.testprojectmusicplayer.viewModel.FilterType
import com.example.testprojectmusicplayer.viewModel.LibraryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LibraryFragment : Fragment(),OnItemClickListener1 {
   private lateinit var binding: FragmentLibraryBinding
   private val viewModel: LibraryViewModel by viewModels()
    @Inject
    lateinit var glide: RequestManager
    @Inject
    lateinit var userObject: UserObject

    private lateinit var adapter: LibraryRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLibraryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       val user =  userObject.getUser()



        viewModel.getLikedAlbums(user?.userId?:"1234")
        viewModel.getFollowedArtist(user?.userId?:"1234")
        observers()
        settingAdapter()
        setFilter()
        onClick()

    }


    private fun settingAdapter() {

        adapter = LibraryRecyclerViewAdapter(emptyList(),this, glide = glide)
       binding.libRecyclerView.adapter  = adapter

    }


    private fun onClick(){
        binding.libPlusIcon.setOnClickListener {
            val action =
                LibraryFragmentDirections.actionLibraryFragmentToCreatePlaylistFragment2(
                null
            )

            findNavController().navigate(action)
        }

        binding.libSearchIcon.setOnClickListener {
            findNavController().navigate(R.id.action_libraryFragment_to_libSearchFragment2)
        }
    }


    private fun setFilter(){


        binding.libRadio.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId){
        binding.customRadioAll.id ->{
                 viewModel.setFilterType(filterType = FilterType.ALL)

              }
           binding.customRadioPlaylist.id ->{
                 viewModel.setFilterType(filterType = FilterType.ALBUM)

             }
           binding.customRadioButtonArtist.id ->{
                  viewModel.setFilterType(filterType = FilterType.ARTIST)

              }



            }
        }

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
                            binding.libraryProgressBar.visibility = View.GONE

                        }

                        is UiStates.Failure ->{


                        }
                    }
                }


            }


        }

    }

    override fun onItemClick(item: AlbumArtist) {

            when (item) {
                is AlbumArtist.AlbumItem -> {
                    Log.d("SongsData",item.album.toString())
                    val action = LibraryFragmentDirections.actionLibraryFragmentToLibPlaylistFragment(item.album.id,null)
                    findNavController().navigate(action)
                    // Handle album item click
                }
                is AlbumArtist.ArtistItem -> {
                    Log.d("SongsData",item.artist.toString())
                    val action = LibraryFragmentDirections.actionLibraryFragmentToLibPlaylistFragment(null,item.artist.id)
                    findNavController().navigate(action)


                    // Handle artist item click
                }

            }
    }




}