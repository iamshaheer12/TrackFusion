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
    val albumRepository: AlbumRepository

) :ViewModel(){

    private val _updateAlbum = MutableStateFlow<UiStates<String>>(UiStates.Loading)
    val updateAlbum: StateFlow<UiStates<String>> = _updateAlbum

    private val _deleteAlbum = MutableStateFlow<UiStates<String>>(UiStates.Loading)
    val deleteAlbum: StateFlow<UiStates<String>> = _deleteAlbum


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