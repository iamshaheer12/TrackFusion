package com.example.testprojectmusicplayer.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testprojectmusicplayer.model.Album
import com.example.testprojectmusicplayer.repositories.AlbumRepository
import com.example.testprojectmusicplayer.utils.UiStates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CreatePlaylistViewModel @Inject constructor(
    private val albumRepository: AlbumRepository

) : ViewModel() {

    private val _createAlbum = MutableStateFlow<UiStates<String>>(UiStates.Initial)
    val createAlbum: StateFlow<UiStates<String>> = _createAlbum
    private val _updateAlbum = MutableStateFlow<UiStates<String>>(UiStates.Initial)
    val updateAlbum: StateFlow<UiStates<String>> = _updateAlbum


    fun createAlbum(album: Album) {
        viewModelScope.launch {
            _createAlbum.update {
                UiStates.Loading
            }
            albumRepository.createAlbum(album) { uiStates ->
                _createAlbum.update {
                    uiStates
                }
            }
        }
    }

    fun updateAlbum(album: Album) {
        viewModelScope.launch {
            _updateAlbum.update {
                UiStates.Loading
            }
            albumRepository.updateAlbum(album) { uiStates ->
                _updateAlbum.update {
                    uiStates
                }
            }
        }
    }

}