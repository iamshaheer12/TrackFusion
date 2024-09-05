package com.example.testprojectmusicplayer.viewModel

import MediaItem
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testprojectmusicplayer.model.Album
import com.example.testprojectmusicplayer.model.Artist
import com.example.testprojectmusicplayer.model.Song
import com.example.testprojectmusicplayer.repositories.AlbumRepository
import com.example.testprojectmusicplayer.repositories.ArtistRepository
import com.example.testprojectmusicplayer.repositories.SongRepository
import com.example.testprojectmusicplayer.utils.UiStates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,

    ) :ViewModel() {



    private val _allSongsState  = MutableStateFlow<UiStates<List<Song>>>(UiStates.Loading)
    val allSongsState :StateFlow<UiStates<List<Song>>> = _allSongsState

    private val _allAlbumsState  = MutableStateFlow<UiStates<List<Album>>>(UiStates.Loading)
    val allAlbumsState :StateFlow<UiStates<List<Album>>> = _allAlbumsState


    private val _allArtistsState  = MutableStateFlow<UiStates<List<Artist>>>(UiStates.Loading)
    val allArtistsState :StateFlow<UiStates<List<Artist>>> = _allArtistsState

    private val _combinedMediaItems = MutableStateFlow<UiStates<List<MediaItem>>>(UiStates.Loading)
    val combinedMediaItems: StateFlow<UiStates<List<MediaItem>>> = _combinedMediaItems





    init {
        // Combine the states of songs, albums, and artists
        viewModelScope.launch {
            combine(_allSongsState, _allAlbumsState, _allArtistsState) { songsState, albumsState, artistsState ->
                // Combine all media items into a single list
                val currentSongs = (songsState as? UiStates.Success)?.data ?: emptyList()
                val currentAlbums = (albumsState as? UiStates.Success)?.data ?: emptyList()
                val currentArtists = (artistsState as? UiStates.Success)?.data ?: emptyList()

                // Map each data type to MediaItem
                val combinedList = mutableListOf<MediaItem>().apply {
                    addAll(currentSongs.map { MediaItem.SongItem(it) })
                    addAll(currentAlbums.map { MediaItem.AlbumItem(it) })
                    addAll(currentArtists.map { MediaItem.ArtistItem(it) })
                }

                // Return the combined list
                UiStates.Success(combinedList)
            }.collect { combinedListState ->
                _combinedMediaItems.value = combinedListState
            }
        }
    }




    fun getStartedAlbums(){
        viewModelScope.launch {
            albumRepository.getAlbums { states ->
                _allAlbumsState.update {
                    states
                }
                // getMediaItems()

            }

        }
    }

    fun getRecentPlayedSongs(){
        viewModelScope.launch {
            songRepository.getAllSongs { state ->
                _allSongsState.update {
                    state
                }
                // getMediaItems()
            }
        }

    }

    fun getArtists(){
        viewModelScope.launch {
            artistRepository.getArtists { state->
                _allArtistsState.update {
                    state

                }
                //getMediaItems()
            }
        }
    }

//    fun getMediaItems(){
//
//            val currentSongs = (_allSongsState.value as? UiStates.Success)?.data ?: emptyList()
//            val currentAlbums = (_allAlbumsState.value as? UiStates.Success)?.data ?: emptyList()
//            val currentArtists = (_allArtistsState.value as? UiStates.Success)?.data ?: emptyList()
//
//            // Combine into a single list of MediaItem
//            val combinedList = mutableListOf<MediaItem>().apply {
//                addAll(currentSongs.map { MediaItem.SongItem(it) })
//                addAll(currentAlbums.map { MediaItem.AlbumItem(it) })
//                addAll(currentArtists.map { MediaItem.ArtistItem(it) })
//            }
//
//            // Update the combined state
//            _combinedMediaItems.update { UiStates.Success(combinedList) }
//        }





}