package com.example.testprojectmusicplayer.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Collections.emptyList

@Parcelize
data class User(
    val name: String = "",
    val dob: String = "",
    val gender: String = "",
    val email: String = "",
    var userId: String = "",
    var imageUrl : String = "",
    val likedSong: List<String> = emptyList(),
    val likedArtist: List<String> = emptyList(),
    var likedAlbums: MutableList<String> = emptyList(),
    val recentlyPlayedSongs: List<String> = emptyList()
) : Parcelable
