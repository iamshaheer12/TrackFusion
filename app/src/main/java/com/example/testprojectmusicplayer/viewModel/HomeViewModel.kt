package com.example.testprojectmusicplayer.viewModel

import com.example.testprojectmusicplayer.utils.AudioPlaybackServiceProvider
import android.util.Log
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

)  :ViewModel() {

    private val _allSongsState  = MutableStateFlow<UiStates<List<Song>>>(UiStates.Loading)
    val allSongsState :StateFlow<UiStates<List<Song>>> = _allSongsState

    private val _songListState  = MutableStateFlow<UiStates<List<Song>>>(UiStates.Loading)
    val songListState :StateFlow<UiStates<List<Song>>> = _songListState
//// Managing like strategy for song

    private val _likedSong  = MutableStateFlow<UiStates<String>>(UiStates.Loading)
    val likedSong :StateFlow<UiStates<String>> = _likedSong

    private val _unLikedSong  = MutableStateFlow<UiStates<String>>(UiStates.Loading)
    val unLikedSong :StateFlow<UiStates<String>> = _unLikedSong

    private val _isLikedSong  = MutableStateFlow<Boolean>(false)
    val isLikedSong :StateFlow<Boolean> = _isLikedSong
//// Managing like strategy for Albums

    private val _currentAlbum = MutableStateFlow<UiStates<Album>>(UiStates.Loading)
    val currentAlbum : StateFlow<UiStates<Album>> = _currentAlbum

    private val _likedAlbum  = MutableStateFlow<UiStates<String>>(UiStates.Loading)
    val likedAlbum :StateFlow<UiStates<String>> = _likedAlbum

    private val _unLikedAlbum  = MutableStateFlow<UiStates<String>>(UiStates.Loading)
    val unLikedAlbum :StateFlow<UiStates<String>> = _unLikedAlbum

    private val _isLikedAlbum  = MutableStateFlow<Boolean>(false)
    val isLikedAlbum :StateFlow<Boolean> = _isLikedAlbum


    private val _getRecentPlayedAlbum = MutableStateFlow<UiStates<List<Album>>>(UiStates.Loading)
    val  getRecentPlayedAlbum :StateFlow<UiStates<List<Album>>> = _getRecentPlayedAlbum

    private val _allAlbumsState  = MutableStateFlow<UiStates<List<Album>>>(UiStates.Loading)
    val allAlbumsState :StateFlow<UiStates<List<Album>>> = _allAlbumsState


    private val _allArtistsState  = MutableStateFlow<UiStates<List<Artist>>>(UiStates.Loading)
    val allArtistsState :StateFlow<UiStates<List<Artist>>> = _allArtistsState
    private val _currentArtist = MutableStateFlow<UiStates<Artist>>(UiStates.Loading)
    val currentArtist : StateFlow<UiStates<Artist>> = _currentArtist

    private val _currentSongArtist = MutableStateFlow<Artist?>(null)
    val currentSongArtist :StateFlow<Artist?> = _currentSongArtist


    private val _likedArtist  = MutableStateFlow<UiStates<String>>(UiStates.Loading)
    val likedArtist :StateFlow<UiStates<String>> = _likedArtist

    private val _unLikedArtist  = MutableStateFlow<UiStates<String>>(UiStates.Loading)
    val unLikedArtist :StateFlow<UiStates<String>> = _unLikedArtist

    private val _isLikedArtist  = MutableStateFlow<Boolean>(false)
    val isLikedArtist :StateFlow<Boolean> = _isLikedArtist



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



//    private val audioPlaybackService: AudioPlaybackService
//        get() = audioPlaybackServiceProvider.getService()






    init {
        // Combine the states of songs, albums, and artists


        viewModelScope.launch {
            combine(_allSongsState, _allAlbumsState, _allArtistsState) { songsState, albumsState, artistsState ->
                // Combine all media items into a single list
                val currentSongs = (songsState as? UiStates.Success)?.data ?: emptyList()
                val currentAlbums = (albumsState as? UiStates.Success)?.data ?: emptyList()
                val currentArtists = (artistsState as? UiStates.Success)?.data ?: emptyList()

                // Map each data type to com.example.testprojectmusicplayer.utils.MediaItem
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
        updateSongList()

        ///
        try {
            val service = audioPlaybackServiceProvider.getService {service ->
                service.setPlaybackCallback(object : AudioPlaybackService.PlaybackCallback {
                    override fun onIndexChanged(index: Int) {
                        _currentSongIndex.value = index
                    }
                    override fun onPlaybackPositionChanged(position: Int) {
                        _playbackPosition.value = position
                    }

                    override fun onPlaybackCompleted() {
                        _isPlaying.value = false
                    }

                    override fun onPlaybackStopped() {
                        _isPlaying.value = false
                    }

                    override fun onPlaybackError(error: String) {
                        // Handle playback error (e.g., show a message to the user)
                    }

                    override fun onSongChanged(song: Song) {
                        _currentSong.value = song
                    }

                    override fun onPlaybackStateChanged(isPlaying: Boolean) {
                        _isPlaying.value = isPlaying
                    }

                })


            }


        }
       catch (e: IllegalStateException) {
            Log.e("HomeViewModel", "Error initializing service: ${e.message}")
        }
        // Register callbacks with the service

    }


    fun onLikedSong(songId:String,userId:String,albumId: String){
        viewModelScope.launch {
            songRepository.onLikedSong(songId = songId, userId = userId, albumId = albumId){
              _likedSong.value = it

            }
        }

    }
    fun onUnLikedSong(songId:String,userId:String,albumId: String){
        viewModelScope.launch {
            songRepository.unLikedSong(songId = songId, userId = userId, albumId = albumId){
                _unLikedSong.update { it }

            }
        }

    }

    fun isLikedSong(songId: String,userId: String){
        viewModelScope.launch {
        val likeState =     songRepository.isSongLikedByUser(songId = songId, userId = userId){
            state ->
            _isLikedSong.value = state
        }

        }
    }


    fun addIntoRecentPlay(userId: String,album: Album){
        viewModelScope.launch {
            albumRepository.addAlbumToRecentlyPlayed(userId= userId, album = album)
        }
    }


    fun onLikedAlbum(albumId:String,userId:String){
        viewModelScope.launch {
            albumRepository.onLikedAlbum(albumId = albumId, id = userId){
                _likedAlbum.update { it }

            }
        }

    }
    fun onUnLikedAlbum(albumId:String,userId:String){
        viewModelScope.launch {
            albumRepository.unLikedAlbum(albumId = albumId, id = userId){
                _unLikedAlbum.update { it }

            }
        }

    }

    fun isLikedAlbum(albumId: String ,userId: String){
        viewModelScope.launch {
                albumRepository.isAlbumLikedByUser(albumId = albumId, userId = userId){
                    state ->
                    _isLikedAlbum.value = state

            }
        }
    }

    fun onLikedArtist(artistId: String,userId: String){
        viewModelScope.launch {
            artistRepository.onLikedArtist(artistId = artistId,userId){
                state ->
                _likedArtist.update {
                    state
                }

            }
        }
    }
    fun onUnlikedLikedArtist(artistId: String,userId: String){
        viewModelScope.launch {
            artistRepository.unLikedArtist(artistId = artistId,userId){
                    state ->
                _unLikedArtist.update {
                    state
                }

            }
        }
    }

    fun isArtistLiked(artistId: String,userId: String){
        viewModelScope.launch {
            artistRepository.isArtistLikedByUser(artistId = artistId, userId = userId){
                state ->
                _isLikedArtist.value = state
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
    fun getCurrentAlbum(albumId: String) {
        viewModelScope.launch {
            when (val currentState = _allAlbumsState.value) {
                is UiStates.Success -> {
                    val filteredAlbum = currentState.data.filter { album ->
                        album.id == albumId // Assuming 'id' is the identifier field in your Album model
                    }

                    if (filteredAlbum.isNotEmpty()) {
                        // Handle the filtered album, update a new state or do something else
                        // Assuming you have another state to hold the current album
                        _currentAlbum.update { UiStates.Success(filteredAlbum.first()) }
                        songListFunc(filteredAlbum.first().songs)
                    } else {
                        // If no album is found, you can update state with an error or empty result
                        _currentAlbum.update { UiStates.Failure("Album not found")  }
                    }
                }
                is UiStates.Loading -> {
                    // Handle loading state if needed
                    _currentAlbum.update {UiStates.Loading  }

                }
                is UiStates.Failure -> {
                    _currentAlbum.update {UiStates.Failure("Album not found")  }

                    // Handle error state if needed
                }

            }
        }
    }

    fun getRecentPlayedAlbum(userId: String){
        viewModelScope.launch {
            albumRepository.getRecentlyPlayedAlbums(userId){
                state ->
                _getRecentPlayedAlbum.update {
                    state
                }
            }
        }
    }

    fun getCurrentArtist(artistId: String) {
        viewModelScope.launch {
            when (val currentState = _allArtistsState.value) {
                is UiStates.Success -> {
                    val filteredArtist = currentState.data.filter { artist ->
                        artist.id == artistId // Assuming 'id' is the identifier field in your Album model
                    }

                    if (filteredArtist.isNotEmpty()) {
                        // Handle the filtered album, update a new state or do something else
                        // Assuming you have another state to hold the current album
                        _currentArtist.value = UiStates.Success(filteredArtist.first())
                        songListFunc(filteredArtist.first().songs)
                    } else {
                        // If no album is found, you can update state with an error or empty result
                        _currentArtist.value = UiStates.Failure("Album not found")
                    }
                }
                is UiStates.Loading -> {
                    // Handle loading state if needed
                    _currentArtist.value = UiStates.Loading

                }
                is UiStates.Failure -> {
                    _currentArtist.value = UiStates.Failure("Artist not found")

                    // Handle error state if needed
                }

            }
        }
    }

     private fun songListFunc(songs:List<String>){
        when (val currentState = _allSongsState.value) {
            is UiStates.Success -> {
                val songList = currentState.data.filter {
                    song ->
                    song.songId in songs

                }

                    _songListState.update { UiStates.Success(songList) }


            }
            is UiStates.Loading ->{
                _songListState.update { UiStates.Loading }

            }
            is UiStates.Failure -> {
                _songListState.update { UiStates.Failure("Unknown Error") }

            }
        }

        }


    fun getRecentPlayedSongs(){
        viewModelScope.launch {
            songRepository.getAllSongs  { state ->
                _allSongsState.update {
                    state
                }
                updateSongList()

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


    fun setCurrentSongArtist(){
        viewModelScope.launch {
          val artistId =   _currentSong.value?.artistId
            if (artistId != null) {
                val currentState = _allArtistsState.value
                if (currentState is UiStates.Success){
                    val allArtist = currentState.data

                    val currentSongArtist = allArtist.filter{ artist ->
                     artist.id == artistId
                    }

                    _currentSongArtist.value = currentSongArtist.first()

                }
                else {
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
                val service = audioPlaybackServiceProvider.getService{service ->
                service.playSong(currentSongIndex.value)
                _isPlaying.value = true}
            } catch (e: IllegalStateException) {
                Log.e("HomeViewModel", "Error getting service: ${e.message}")
            }
        }
    }


    fun pausePlayback() {
        viewModelScope.launch {
            try {
                val service = audioPlaybackServiceProvider.getService{service ->
                service.pausePlayback()}
            }
            catch (e:IllegalStateException){
                Log.e("HomeViewModel", "Service not available: ${e.message}")

            }

        }
    }


    fun stopPlayback() {
        viewModelScope.launch {
            try {
                val service = audioPlaybackServiceProvider.getService{service ->
                service.stopPlayback()}
            } catch (e: IllegalStateException) {
                Log.e("HomeViewModel", "Service not available: ${e.message}")
                // Handle the error or notify the user
            }
        }
    }

    private fun updateSongList() {
        viewModelScope.launch {
            try {
                val songList = (_allSongsState.value as? UiStates.Success)?.data ?: emptyList()
              audioPlaybackServiceProvider.getService{service ->
                  Log.d("SongsList12",songList.toString())
                service.updateSongList(songList)}
            } catch (e: IllegalStateException) {
                Log.e("HomeViewModel", "Service not available: ${e.message}")
                // Handle the error or notify the user
            }
        }
    }

    fun seekTo(position: Int) {
        viewModelScope.launch {
            try {
                val service = audioPlaybackServiceProvider.getService{service ->
                service.seekTo(position)}
                _playbackPosition.update {
                    position
                }
            } catch (e: IllegalStateException) {
                Log.e("HomeViewModel", "Service not available: ${e.message}")
                // Handle the error or notify the user
            }
        }
    }


    fun updateIndex(index:Int){
        viewModelScope.launch {
            _currentSongIndex.update {
                index
            }
        }

    }
    fun updatePlayPauseState(state:Boolean){
        viewModelScope.launch {
            _isPlaying.value = state
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlaybackServiceProvider.unbindService()
    }

}