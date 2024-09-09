package com.example.testprojectmusicplayer.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testprojectmusicplayer.model.Album
import com.example.testprojectmusicplayer.model.Artist
import com.example.testprojectmusicplayer.repositories.AlbumRepository
import com.example.testprojectmusicplayer.repositories.ArtistRepository
import com.example.testprojectmusicplayer.utils.AlbumArtist
import com.example.testprojectmusicplayer.utils.MediaItem
import com.example.testprojectmusicplayer.utils.UiStates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,

    ):ViewModel() {
    private val _allAlbumsState = MutableStateFlow<UiStates<List<Album>>>(UiStates.Loading)
    val allAlbumsState: StateFlow<UiStates<List<Album>>> = _allAlbumsState

    private val _allArtistsState = MutableStateFlow<UiStates<List<Artist>>>(UiStates.Loading)
    val allArtistsState: StateFlow<UiStates<List<Artist>>> = _allArtistsState


    private val _updateAlbum = MutableStateFlow<UiStates<String>>(UiStates.Loading)
    val updateAlbum: StateFlow<UiStates<String>> = _updateAlbum

    private val _deleteAlbum = MutableStateFlow<UiStates<String>>(UiStates.Loading)
    val deleteAlbum: StateFlow<UiStates<String>> = _deleteAlbum


    private val _filteredAlbumArtist = MutableStateFlow<UiStates<List<AlbumArtist>>>(UiStates.Success(emptyList()))
    val filteredAlbumArtist: StateFlow<UiStates<List<AlbumArtist>>> = _filteredAlbumArtist

    private val allMediaItems = mutableListOf<AlbumArtist>()


    private val _currentFilterType = MutableStateFlow<FilterType>(FilterType.ALL)
    val currentFilterType: StateFlow<FilterType> = _currentFilterType



    init {
        viewModelScope.launch {
            combine(_allAlbumsState,_allArtistsState){allAlbumState,allArtistsState ->
                val currentArtist = (allArtistsState as? UiStates.Success)?.data ?: emptyList()
                val currentAlbum = (allAlbumState as? UiStates.Success)?.data?: emptyList()


                val combineList = mutableListOf<AlbumArtist>().apply {
                    addAll(currentArtist.map { AlbumArtist.ArtistItem(it) })
                    addAll(currentAlbum.map { AlbumArtist.AlbumItem(it) })
                }

                UiStates.Success(combineList)

            }.collect{
                combineListState ->
                allMediaItems.clear()
                allMediaItems.addAll(combineListState.data)
                _filteredAlbumArtist.value = combineListState
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


    fun getArtist(){
        viewModelScope.launch {
            artistRepository.getArtists {
                state -> _allArtistsState.update {
                    state
            }
            }
        }
    }


    // Function to change the filter type
    fun setFilterType(filterType: FilterType) {
        _currentFilterType.value = filterType
        applyCurrentFilter() // Re-apply the filter whenever it changes
    }



    // Apply the current filter based on the filter type
    private fun applyCurrentFilter() {
        val filteredItems = when (_currentFilterType.value) {
            FilterType.ARTIST -> allMediaItems.filterIsInstance<AlbumArtist.ArtistItem>()
            FilterType.ALBUM -> allMediaItems.filterIsInstance<AlbumArtist.AlbumItem>()
            FilterType.ALL -> allMediaItems
        }
        _filteredAlbumArtist.value = UiStates.Success(filteredItems)
    }


    fun filterMediaItems(query: String) {


        viewModelScope.launch {
            val filteredItems = if (query.isEmpty()) {
                allMediaItems // Show all items when the search query is empty
            } else {
                allMediaItems.filter { item ->
                    when (item) {
                        is AlbumArtist.ArtistItem -> item.artist.name.contains(query, ignoreCase = true)
                        is AlbumArtist.AlbumItem -> item.album.title.contains(query, ignoreCase = true)
                    }
                }
            }
            _filteredAlbumArtist.value = UiStates.Success(filteredItems)
        }
    }

    fun deleteAlbum(id:String){
        viewModelScope.launch {
            albumRepository.deleteAlbum(id){
                uiStates ->
                _deleteAlbum.update {
                    uiStates
                }
            }
        }
    }


   fun updateAlbum(album: Album){
        viewModelScope.launch {
            albumRepository.updateAlbum(album){
                    uiStates ->
                _updateAlbum.update {
                    uiStates
                }
            }
        }
    }




}
// Enum class to define filter types
enum class FilterType {
    ARTIST, ALBUM, ALL
}
