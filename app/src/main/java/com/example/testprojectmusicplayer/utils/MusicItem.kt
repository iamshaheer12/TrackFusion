package com.example.testprojectmusicplayer.utils

import com.example.testprojectmusicplayer.model.Album
import com.example.testprojectmusicplayer.model.Song

sealed class MusicItem {
    data class AlbumItem(val album: Album) : MusicItem()
    data class SongItem(val song: Song) : MusicItem()
}
