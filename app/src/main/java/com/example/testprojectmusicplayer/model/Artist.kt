package com.example.testprojectmusicplayer.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable
@Parcelize
data class Artist(
    val id : String = "",
    val name: String = "",
    val imageUrl:String = "",
    val followers: Int = 0,
    val likedBy :List<String> = emptyList(),
    var songs:List<String> = emptyList(),

    ):Parcelable


