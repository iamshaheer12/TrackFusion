package com.example.testprojectmusicplayer.viewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem as Media3MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.example.testprojectmusicplayer.model.Album
import com.example.testprojectmusicplayer.model.Artist
import com.example.testprojectmusicplayer.model.Song
import com.example.testprojectmusicplayer.repositories.AlbumRepository
import com.example.testprojectmusicplayer.repositories.ArtistRepository
import com.example.testprojectmusicplayer.repositories.SongRepository
import com.example.testprojectmusicplayer.utils.MediaControllerProvider
import com.example.testprojectmusicplayer.utils.MediaItem
import com.example.testprojectmusicplayer.utils.UiStates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val mediaControllerProvider: MediaControllerProvider

) : ViewModel() {



    private var isSongListSent = false

    private var isServiceSet = false

    private var listenerRegistered = false

    private var positionJob: Job? = null



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
      //  setupControllerListener()
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
            val songList = (_songListState.value as? UiStates.Success)?.data ?: emptyList()
            if (songList.isEmpty()) {
                _currentSong.value = null
                return@launch
            }
            _currentSong.value = songList[currentSongIndex.value]
            _isPlaying.value = true

            mediaControllerProvider.getController { controller ->
                val targetSong = songList.getOrNull(currentSongIndex.value)
                val playingSameSong =
                    targetSong != null && controller.currentMediaItem?.mediaId == targetSong.songId

                if (controller.mediaItemCount == 0) {
                    controller.setMediaItems(
                        songList.map { song -> song.toPlayableMediaItem() },
                        currentSongIndex.value,
                        0L
                    )
                    controller.prepare()
                } else if (!playingSameSong) {
                    controller.seekTo(currentSongIndex.value, 0L)
                    if (controller.playbackState == Player.STATE_IDLE) {
                        controller.prepare()
                    }
                }
                controller.play()
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
                            setupControllerListener()
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
                            setupControllerListener()
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
        mediaControllerProvider.getController { controller ->
            controller.pause()
        }
        _isPlaying.value = false
    }


    fun stopPlayback() {
        viewModelScope.launch {
            mediaControllerProvider.getController { controller ->
                controller.stop()
            }
        }
    }

    private fun updateSongList() {
        viewModelScope.launch {
            val songList = (_songListState.value as? UiStates.Success)?.data ?: emptyList()
            if (songList.isEmpty()) return@launch

            mediaControllerProvider.getController { controller ->
                controller.setMediaItems(
                    songList.map { song -> song.toPlayableMediaItem() },
                    currentSongIndex.value.coerceIn(0, songList.lastIndex),
                    0L
                )
            }
        }
    }

    fun seekTo(position: Int) {
        viewModelScope.launch {
            mediaControllerProvider.getController { controller ->
                controller.seekTo(position.toLong())
            }
            _playbackPosition.value = position
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
            mediaControllerProvider.getController { controller ->
                controller.seekToNext()
            }
        }
    }

    fun onClickPrevious() {
        viewModelScope.launch {
            mediaControllerProvider.getController { controller ->
                controller.seekToPrevious()
            }
        }
    }

    private fun setupControllerListener() {
        mediaControllerProvider.getController { controller ->
            if (!listenerRegistered) {
                controller.addListener(playerListener)
                listenerRegistered = true
            }
            syncStateFromController(controller)
            startPositionUpdates()
        }
    }

    private fun syncStateFromController(controller: MediaController) {
        val index = controller.currentMediaItemIndex
        val songList = (_songListState.value as? UiStates.Success)?.data ?: emptyList()
        if (index != C.INDEX_UNSET && index in songList.indices) {
            _currentSongIndex.value = index
            _currentSong.value = songList[index]
        }
        _isPlaying.value = controller.isPlaying
        if (controller.currentPosition > 0L) {
            _playbackPosition.value = controller.currentPosition.toInt()
        }
    }

    private fun startPositionUpdates() {
        positionJob?.cancel()
        positionJob = viewModelScope.launch {
            while (isActive) {
                if (mediaControllerProvider.isConnected()) {
                    mediaControllerProvider.getController { controller ->
                        _playbackPosition.value = controller.currentPosition.toInt()
                    }
                }
                delay(1000L)
            }
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                _isPlaying.value = false
            }
        }

        override fun onMediaItemTransition(mediaItem: Media3MediaItem?, reason: Int) {
            mediaControllerProvider.getController { controller ->
                val index = controller.currentMediaItemIndex
                _currentSongIndex.value = index
                val songList = (_songListState.value as? UiStates.Success)?.data ?: emptyList()
                _currentSong.value = songList.getOrNull(index)
            }
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            _playbackPosition.value = newPosition.positionMs.toInt()
        }

        override fun onPlayerError(error: PlaybackException) {
            _isPlaying.value = false
            Log.e("HomeViewModel", "Playback error: ${error.message}")
        }
    }

    private fun Song.toPlayableMediaItem(): Media3MediaItem {
        return Media3MediaItem.Builder()
            .setMediaId(songId)
            .setUri(audioFile)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(description)
                    .setArtworkUri(Uri.parse(imageUrl))
                    .build()
            )
            .build()
    }

    override fun onCleared() {
        positionJob?.cancel()
        mediaControllerProvider.releaseController()
        super.onCleared()
    }

}