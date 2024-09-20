package com.example.testprojectmusicplayer.model

import android.os.Parcelable
import androidx.transition.Visibility
import kotlinx.parcelize.Parcelize

@Parcelize
data class Album(
    var id : String = "",
    val title:String = "",
    val descriptions:String = "",
    val imageUrl: String = "",
    val likes: Int = 0,
    val visibility: Boolean = false,
    val createdBy : String = "",
    val likedBy : List<String> = emptyList(),
    var songs:List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable
