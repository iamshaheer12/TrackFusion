package com.example.testprojectmusicplayer.viewModel

import com.example.testprojectmusicplayer.utils.MediaItem
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
    private val artistRepository: ArtistRepository
) : ViewModel() {

    private val _allSongsState = MutableStateFlow<UiStates<List<Song>>>(UiStates.Loading)
    val allSongsState: StateFlow<UiStates<List<Song>>> = _allSongsState

    private val _allAlbumsState = MutableStateFlow<UiStates<List<Album>>>(UiStates.Loading)
    val allAlbumsState: StateFlow<UiStates<List<Album>>> = _allAlbumsState

    private val _allArtistsState = MutableStateFlow<UiStates<List<Artist>>>(UiStates.Loading)
    val allArtistsState: StateFlow<UiStates<List<Artist>>> = _allArtistsState

//    private val _combinedMediaItems = MutableStateFlow<UiStates<List<MediaItem>>>(UiStates.Loading)
//    val combinedMediaItems: StateFlow<UiStates<List<MediaItem>>> = _combinedMediaItems

    private val _filteredMediaItems =
        MutableStateFlow<UiStates<List<MediaItem>>>(UiStates.Success(emptyList()))
    val filteredMediaItems: StateFlow<UiStates<List<MediaItem>>> = _filteredMediaItems

    private val allMediaItems = mutableListOf<MediaItem>() // Holds all media items initially loaded

    init {
        viewModelScope.launch {
            combine(
                _allSongsState,
                _allAlbumsState,
                _allArtistsState
            ) { songsState, albumsState, artistsState ->
                val currentSongs = (songsState as? UiStates.Success)?.data ?: emptyList()
                val currentAlbums = (albumsState as? UiStates.Success)?.data ?: emptyList()
                val currentArtists = (artistsState as? UiStates.Success)?.data ?: emptyList()

                val combinedList = mutableListOf<MediaItem>().apply {
                    addAll(currentSongs.map { MediaItem.SongItem(it) })
                    addAll(currentAlbums.map { MediaItem.AlbumItem(it) })
                    addAll(currentArtists.map { MediaItem.ArtistItem(it) })
                }

                UiStates.Success(combinedList)
            }.collect { combinedListState ->
                //_combinedMediaItems.value = combinedListState

                // If data is loaded successfully, update the allMediaItems list and filtered items
                allMediaItems.clear()
                allMediaItems.addAll(combinedListState.data)
                _filteredMediaItems.value = combinedListState // Show all items by default
            }
        }
    }

    fun getStartedAlbums() {
        viewModelScope.launch {
            albumRepository.getAlbums { states ->
                _allAlbumsState.update { states }
            }
        }
    }

    fun getRecentPlayedSongs() {
        viewModelScope.launch {
            songRepository.getAllSongs { state ->
                _allSongsState.update { state }
            }
        }
    }

    fun getArtists() {
        viewModelScope.launch {
            artistRepository.getArtists { state ->
                _allArtistsState.update { state }
            }
        }
    }

    // Function to filter media items based on search query
    fun filterMediaItems(query: String) {
        viewModelScope.launch {
            val filteredItems = if (query.isEmpty()) {
                allMediaItems // Show all items when the search query is empty
            } else {
                allMediaItems.filter { item ->
                    when (item) {
                        is MediaItem.ArtistItem -> item.artist.name.contains(
                            query,
                            ignoreCase = true
                        )

                        is MediaItem.AlbumItem -> item.album.title.contains(
                            query,
                            ignoreCase = true
                        )

                        is MediaItem.SongItem -> item.song.title.contains(query, ignoreCase = true)

                    }
                }
            }
            _filteredMediaItems.value = UiStates.Success(filteredItems)
        }
    }
}
