package com.example.testprojectmusicplayer.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val name: String = "",
    val dob: String = "",
    val gender: String = "",
    val email: String = "",
    var userId: String = "",
    val likedSong: List<String> = emptyList(),
    val likedArtist: List<String> = emptyList(),
    var likedAlbums: String = "",
    val recentlyPlayedSongs: List<String> = emptyList()
) : Parcelable
