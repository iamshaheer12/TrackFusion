package com.example.testprojectmusicplayer.utils

import android.os.Parcelable
import com.example.testprojectmusicplayer.model.Album
import com.example.testprojectmusicplayer.model.Artist
import kotlinx.parcelize.Parcelize

sealed class AlbumArtist : Parcelable {
    @Parcelize
    data class AlbumItem(val album: Album) : AlbumArtist()

    @Parcelize
    data class ArtistItem(val artist: Artist) : AlbumArtist()


}