package com.example.testprojectmusicplayer.utils

import android.os.Parcelable
import com.example.testprojectmusicplayer.model.Album
import com.example.testprojectmusicplayer.model.Artist
import com.example.testprojectmusicplayer.model.Song
import kotlinx.parcelize.Parcelize

sealed class MediaItem : Parcelable {
    @Parcelize
    data class AlbumItem(val album: Album) : MediaItem()

    @Parcelize
    data class ArtistItem(val artist: Artist) : MediaItem()

    @Parcelize
    data class SongItem(val song: Song) : MediaItem()
}
