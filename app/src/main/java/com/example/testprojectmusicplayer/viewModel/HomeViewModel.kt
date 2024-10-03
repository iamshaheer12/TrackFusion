package com.example.testprojectmusicplayer.viewModel

import com.example.testprojectmusicplayer.utils.AudioPlaybackServiceProvider
import android.util.Log
import android.view.View
import com.example.testprojectmusicplayer.utils.MediaItem
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testprojectmusicplayer.model.Album
import com.example.testprojectmusicplayer.model.Artist
import com.example.testprojectmusicplayer.model.Song
import com.example.testprojectmusicplayer.repositories.AlbumRepository
import com.example.testprojectmusicplayer.repositories.ArtistRepository
import com.example.testprojectmusicplayer.repositories.SongRepository
import com.example.testprojectmusicplayer.utils.AudioPlaybackService
import com.example.testprojectmusicplayer.utils.UiStates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val audioPlaybackServiceProvider: AudioPlaybackServiceProvider

) : ViewModel() {



    private var isSongListSent = false

    private var isServiceSet = false



    private val _songListState = MutableStateFlow<UiStates<List<Song>>>(UiStates.Initial)
    val songListState: StateFlow<UiStates<List<Song>>> = _songListState


    private val _likedSong = MutableStateFlow<UiStates<String>>(UiStates.Loading)
    val likedSong: StateFlow<UiStates<String>> = _likedSong

    private val _unLikedSong = MutableStateFlow<UiStates<String>>(UiStates.Loading)
    val unLikedSong: StateFlow<UiStates<String>> = _unLikedSong

    private val _isLikedSong = MutableStateFlow<Boolean>(false)
    val isLikedSong: StateFlow<Boolean> = _isLikedSong

    private val _removeSongFromPlaylist = MutableStateFlow<UiStates<String>>(UiStates.Initial)
    val removeSongFromPlaylist: StateFlow<UiStates<String>> = _removeSongFromPlaylist


//// Managing like strategy for Albums

    private val _currentAlbum = MutableStateFlow<UiStates<Album?>>(UiStates.Initial)
    val currentAlbum: StateFlow<UiStates<Album?>> = _currentAlbum

    private val _currentAlbumId = MutableStateFlow<String?>(null)
    val currentAlbumId: StateFlow<String?> = _currentAlbumId

    private val _likedAlbum = MutableStateFlow<UiStates<String>>(UiStates.Loading)
    val likedAlbum: StateFlow<UiStates<String>> = _likedAlbum

    private val _unLikedAlbum = MutableStateFlow<UiStates<String>>(UiStates.Loading)
    val unLikedAlbum: StateFlow<UiStates<String>> = _unLikedAlbum

    private val _isLikedAlbum = MutableStateFlow<Boolean>(false)
    val isLikedAlbum: StateFlow<Boolean> = _isLikedAlbum


    private val _getRecentPlayedAlbum = MutableStateFlow<UiStates<List<Album>>>(UiStates.Loading)
    val getRecentPlayedAlbum: StateFlow<UiStates<List<Album>>> = _getRecentPlayedAlbum

    private val _allAlbumsState = MutableStateFlow<UiStates<List<Album>>>(UiStates.Loading)
    val allAlbumsState: StateFlow<UiStates<List<Album>>> = _allAlbumsState

    private val _deleteAlbum = MutableStateFlow<UiStates<String>>(UiStates.Initial)
    val deleteAlbum: StateFlow<UiStates<String>> = _deleteAlbum


    private val _allArtistsState = MutableStateFlow<UiStates<List<Artist>>>(UiStates.Loading)
    val allArtistsState: StateFlow<UiStates<List<Artist>>> = _allArtistsState
    private val _currentArtist = MutableStateFlow<UiStates<Artist?>>(UiStates.Initial)
    val currentArtist: StateFlow<UiStates<Artist?>> = _currentArtist

    private val _currentSongArtist = MutableStateFlow<Artist?>(null)
    val currentSongArtist: StateFlow<Artist?> = _currentSongArtist

    private val _currentArtistId = MutableStateFlow<String?>(null)
    val currentArtistId: StateFlow<String?> = _currentArtistId


    private val _likedArtist = MutableStateFlow<UiStates<String>>(UiStates.Loading)
    val likedArtist: StateFlow<UiStates<String>> = _likedArtist

    private val _unLikedArtist = MutableStateFlow<UiStates<String>>(UiStates.Loading)
    val unLikedArtist: StateFlow<UiStates<String>> = _unLikedArtist

    private val _isLikedArtist = MutableStateFlow<Boolean>(false)
    val isLikedArtist: StateFlow<Boolean> = _isLikedArtist


    private val _combinedMediaItems = MutableStateFlow<UiStates<List<MediaItem>>>(UiStates.Loading)
    val combinedMediaItems: StateFlow<UiStates<List<MediaItem>>> = _combinedMediaItems
    //// service function


    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> get() = _currentSong


    private val _currentSongIndex = MutableStateFlow(0)
    val currentSongIndex: StateFlow<Int> get() = _currentSongIndex


    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> get() = _isPlaying

    private val _playbackPosition = MutableStateFlow(0)
    val playbackPosition: StateFlow<Int> get() = _playbackPosition





    init {
      //  setupPlaybackCallback()
    }


    fun onLikedSong(songId: String, userId: String, albumId: String) {
        viewModelScope.launch {
            songRepository.onLikedSong(songId = songId, userId = userId, albumId = albumId) {
                _likedSong.value = it

            }
        }

    }

    fun onUnLikedSong(songId: String, userId: String, albumId: String) {
        viewModelScope.launch {
            songRepository.unLikedSong(songId = songId, userId = userId, albumId = albumId) {
                _unLikedSong.update { it }

            }
        }

    }

      suspend fun  isLikedSong(songId: String, userId: String) {
        viewModelScope.launch {

            songRepository.isSongLikedByUser(songId = songId, userId = userId) { state ->
                _isLikedSong.value = state
            }

        }
    }

    fun resetLikeArtistState(){
        _unLikedArtist.value = UiStates.Initial
    }
    fun resetLikeAlbumState(){
        _unLikedAlbum.value = UiStates.Initial
    }


  suspend  fun removeSongFromAlbum(songId: String, albumId: String) {
        viewModelScope.launch {
            _removeSongFromPlaylist.update { UiStates.Loading }

            albumRepository.removeSongFromAlbum(songId = songId, albumId = albumId) {
                state ->
                _removeSongFromPlaylist.update {
                    state
                }
                Log.d("removeSongFromAlbum", "removeSongFromAlbum: ")

            }

        }
    }


    fun addIntoRecentPlay(userId: String, album: Album) {
        viewModelScope.launch {
            albumRepository.addAlbumToRecentlyPlayed(userId = userId, album = album)
        }
    }


    fun onLikedAlbum(albumId: String, userId: String) {
        viewModelScope.launch {
            albumRepository.onLikedAlbum(albumId = albumId, id = userId) {
                _likedAlbum.update { it }

            }
        }
    }

    fun onUnLikedAlbum(albumId: String, userId: String) {
        viewModelScope.launch {
            albumRepository.unLikedAlbum(albumId = albumId, id = userId) {
                _unLikedAlbum.update { it }

            }
        }

    }


    fun isLikedAlbum(albumId: String, userId: String) {
        viewModelScope.launch {
            albumRepository.isAlbumLikedByUser(albumId = albumId, userId = userId) { state ->
                _isLikedAlbum.value = state

            }
        }
    }

    fun deleteAlbum(id: String) {
        viewModelScope.launch {
            _deleteAlbum.value = UiStates.Loading

            albumRepository.deleteAlbum(id) { uiStates ->
                _deleteAlbum.update {
                    uiStates
                }
            }
        }
    }
    fun resetDeleteAlbumState(){
        _deleteAlbum.value = UiStates.Initial
    }


    fun onLikedArtist(artistId: String, userId: String) {
        viewModelScope.launch {
            artistRepository.onLikedArtist(artistId = artistId, userId) { state ->
                _likedArtist.update {
                    state
                }

            }
        }
    }

    fun onUnlikedLikedArtist(artistId: String, userId: String) {
        viewModelScope.launch {
            artistRepository.unLikedArtist(artistId = artistId, userId) { state ->
                _unLikedArtist.update {
                    state
                }

            }
        }
    }

    fun isArtistLiked(artistId: String, userId: String) {
        viewModelScope.launch {
            artistRepository.isArtistLikedByUser(artistId = artistId, userId = userId) { state ->
                _isLikedArtist.value = state
            }

        }
    }


    fun getStartedAlbums() {
        viewModelScope.launch {
            albumRepository.getAlbums { states ->
                _allAlbumsState.update {
                    states
                }
                // getMediaItems()

            }

        }
    }

    fun getCurrentAlbum(albumId: String) {
        viewModelScope.launch {
            _currentAlbum.value = UiStates.Loading

            albumRepository.getAlbumById(albumId) { state ->
                _currentAlbum.update {
                    state
                }
            }
        }

    }

    fun getRecentPlayedAlbum(userId: String) {
        viewModelScope.launch {
            albumRepository.getRecentlyPlayedAlbums(userId) { state ->
                _getRecentPlayedAlbum.update {
                    state
                }
            }
        }
    }

    fun getCurrentArtist(artistId: String) {

        viewModelScope.launch {
            _currentArtist.value = UiStates.Loading

            artistRepository.getArtistById(artistId) { state ->
                _currentArtist.update {
                    state
                }
            }
        }


    }

    fun songListFunc(songs: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            _songListState.value = UiStates.Loading
            songRepository.getSongsByIds(songs) { state ->
                _songListState.update {
                    state
                }


            }

        }


    }




    fun getArtists() {
        viewModelScope.launch {
            artistRepository.getArtists { state ->
                _allArtistsState.update {
                    state

                }
                //getMediaItems()
            }
        }
    }


    fun setCurrentSongArtist() {
        viewModelScope.launch {
            val artistId = _currentSong.value?.artistId
            if (artistId != null) {
                val currentState = _allArtistsState.value
                if (currentState is UiStates.Success) {
                    val allArtist = currentState.data

                    val currentSongArtist = allArtist.filter { artist ->
                        artist.id == artistId
                    }

                    _currentSongArtist.value = currentSongArtist.first()

                } else {
                    // Handle the case where artist data is not yet available or still loading
                    Log.e("HomeViewModel", "Artist data is not available yet.")
                }


            }

        }
    }

//

    fun playSong() {
        viewModelScope.launch {
            try {
                val songList = (_songListState.value as? UiStates.Success)?.data ?: emptyList()
                if (songList.isNotEmpty()){
                    _currentSong.value =
                        songList[currentSongIndex.value]
                    _isPlaying.value = true

                }
                else{
                    _currentSong.value = null
                }


                audioPlaybackServiceProvider.getService { service ->
                    service.playSong(currentSongIndex.value)

                }
            } catch (e: IllegalStateException) {
                Log.e("HomeViewModel", "Error getting service: ${e.message}")
            }
        }
    }

    fun resetSongListSent() {
        isSongListSent = false
        _currentAlbumId.value = null
        _currentArtistId.value = null
        _currentSong.value = null
        _isPlaying.value = false
        _currentSongIndex.value = 0

    }

    fun handlePlayPause(albumId: String?, artistId: String?, songId: String?) {
        viewModelScope.launch {
            try {
                if (albumId != null) {
                    if (!isSongListSent && albumId != _currentAlbumId.value) {
                        updateSongList()
                        if (!isServiceSet){
                            isServiceSet = true
                            setupPlaybackCallback()
                        }

                        isSongListSent = true

                        _currentAlbumId.update {
                            albumId
                        }
                        if (songId != null) {
                            playSong()
                        } else {
                            if (!_isPlaying.value) {


                                playSong()

                            } else {
                                pauseSong()
                            }
                        }

                    } else {


                        if (songId != null) {
                            playSong()
                        } else {
                            if (!_isPlaying.value) {

                                playSong()
                            } else {
                                pauseSong()
                            }
                        }


                    }
                } else {
                    if (!isSongListSent && artistId != _currentArtistId.value) {
                        updateSongList()
                        if (!isServiceSet){
                            isServiceSet = true
                            setupPlaybackCallback()
                        }
                        isSongListSent = true
                        _currentArtistId.update {
                            artistId
                        }
                        if (songId != null) {
                            playSong()
                        } else {
                            if (!_isPlaying.value) {

                                playSong()
                            } else {
                                pauseSong()
                            }
                        }
                    } else {

                        if (songId != null) {
                            playSong()
                        } else {
                            if (!_isPlaying.value) {

                                playSong()
                            } else {
                                pauseSong()
                            }

                        }

                    }
                }

            } catch (e: IllegalStateException) {
                Log.e("HomeViewModel", "Service not available: ${e.message}")

            }

        }
    }


    fun updateCurrentSong(song: Song?) {
        viewModelScope.launch {
            _currentSong.update {
                song
            }
        }
    }


    fun pauseSong() {
        audioPlaybackServiceProvider.getService { service ->
            service.pausePlayback()
        }
        _isPlaying.value = false
    }


    fun stopPlayback() {
        viewModelScope.launch {
            try {
                val service = audioPlaybackServiceProvider.getService { service ->
                    service.stopPlayback()
                }
            } catch (e: IllegalStateException) {
                Log.e("HomeViewModel", "Service not available: ${e.message}")
                // Handle the error or notify the user
            }
        }
    }

    private fun updateSongList() {
        viewModelScope.launch {
            try {
                val songList = (_songListState.value as? UiStates.Success)?.data ?: emptyList()



                audioPlaybackServiceProvider.getService { service ->
                    service.updateSongList(songList)
                }
            } catch (e: IllegalStateException) {
                Log.e("HomeViewModel", "Service not available: ${e.message}")
                // Handle the error or notify the user
            }
        }
    }

    fun seekTo(position: Int) {
        viewModelScope.launch {
            try {
                 audioPlaybackServiceProvider.getService { service ->
                    service.seekTo(position)
                }
                _playbackPosition.value  = position
            //                update {
//                    position
//                }
            } catch (e: IllegalStateException) {
                Log.e("HomeViewModel", "Service not available: ${e.message}")
                // Handle the error or notify the user
            }
        }
    }


    fun updateIndex(index: Int) {
        viewModelScope.launch {
            _currentSongIndex.update {
                index
            }
        }

    }

    fun updatePlayPauseState(state: Boolean) {
        viewModelScope.launch {
            _isPlaying.value = state
        }
    }

    fun onClickNext() {
        viewModelScope.launch {
            audioPlaybackServiceProvider.getService { service ->
                service.nextSong()
               // setupPlaybackCallback()
            }
        }
    }

    fun onClickPrevious() {

        viewModelScope.launch {
            audioPlaybackServiceProvider.getService { service ->
              service.previousSong()
            }

        }

    }

    private fun setupPlaybackCallback() {
        try {
            audioPlaybackServiceProvider.getService { service ->
                service.setPlaybackCallback(object : AudioPlaybackService.PlaybackCallback {
                    override fun onIndexChanged(index: Int) {


                        _currentSongIndex.value = index
                    // Use postValue if this happens in background thread
                        Log.d("CurrentSongIndex12",index.toString())
                    }

                    override fun onPlaybackPositionChanged(position: Int) {
                        _playbackPosition.value = position
                        Log.d("PlabackPostion12",position.toString())
                    }

                    override fun onPlaybackCompleted() {
                        _isPlaying.value = false
                    }

                    override fun onPlaybackStopped() {
                        _isPlaying.value = false
                        Log.d("PlaybackStopped", "Playback stopped.")
                    }

                    override fun onPlaybackError(error: String) {
                        // Handle playback error (e.g., show a message to the user)
                    }

                    override fun onSongChanged(song: Song) {
                        _currentSong.value = song
                        Log.d("CurrentsongCallback", song.toString())
                    }

                    override fun onPlaybackStateChanged(isPlaying: Boolean) {
                        _isPlaying.value = isPlaying
                    }
                })
            }
        } catch (e: IllegalStateException) {
            Log.e("HomeViewModel", "Error initializing service: ${e.message}")
        }
    }




    override fun onCleared() {
        super.onCleared()
        audioPlaybackServiceProvider.unbindService()
    }

}