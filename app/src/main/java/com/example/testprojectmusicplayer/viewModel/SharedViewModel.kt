package com.example.testprojectmusicplayer.viewModel

import androidx.lifecycle.ViewModel
import com.example.testprojectmusicplayer.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor () : ViewModel() {

    // StateFlow for the list of songs
    private val _songList = MutableStateFlow<List<Song>>(emptyList())
    val songList: StateFlow<List<Song>> get() = _songList

    // StateFlow for the current song index
    private val _currentSongIndex = MutableStateFlow(0)
    val currentSongIndex: StateFlow<Int> get() = _currentSongIndex

    fun setSongList(songs: List<Song>) {
        _songList.value = songs
    }

    fun setCurrentSongIndex(index: Int) {
        _currentSongIndex.value = index
    }

    fun getCurrentSongIndex(): Int = _currentSongIndex.value

    fun getSongsList():List<Song> {
        return _songList.value
    }
}
