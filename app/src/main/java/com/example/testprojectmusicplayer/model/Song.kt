package com.example.testprojectmusicplayer.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class Song(
    val songId: String = "",
    val title: String = "",
    val description: String = "",
    val genres: String = "",
    val imageUrl: String = "",
    val audioFile: String = "",
    val lyrics: String = "",
    val artistId: String = "",
    val likedBy: List<String> = emptyList(),
    val likes: Int = 0
) : Parcelable
