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
class AddSongViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
) : ViewModel() {

    private val _allAlbums = MutableStateFlow<UiStates<List<Album>>>(UiStates.Loading)
    val allAlbums: StateFlow<UiStates<List<Album>>> = _allAlbums

    private val _addSongState = MutableStateFlow<UiStates<String>>(UiStates.Initial)
    val addSongState: StateFlow<UiStates<String>> = _addSongState


    fun getAllAlbum(userId: String) {
        viewModelScope.launch {

            albumRepository.getAlbumCreatedByUser(userId) { state ->
                _allAlbums.update {
                    state
                }

            }

        }

    }

    fun addSongs(songId: String, albumIds: List<String>) {
        viewModelScope.launch {
            _addSongState.update {
                UiStates.Loading
            }
            albumRepository.addSongToAlbums(songId = songId, list = albumIds) {
                state ->
                _addSongState.update {
                    state
                }
            }
        }

    }

}