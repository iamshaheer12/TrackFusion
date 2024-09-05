package com.example.testprojectmusicplayer.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class Song(
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
//{
//    constructor(parcel: Parcel) : this(
//        parcel.readString() ?: "",
//        parcel.readString() ?: "",
//        parcel.readString() ?: "",
//        parcel.readString() ?: "",
//        parcel.readString() ?: "",
//        parcel.readString() ?: "",
//        parcel.readString() ?: "",
//        parcel.readInt()
//    )
//
//    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeString(title)
//        parcel.writeString(description)
//        parcel.writeString(genres)
//        parcel.writeString(imageUrl)
//        parcel.writeString(audioFile)
//        parcel.writeString(lyrics)
//        parcel.writeString(artistId)
//        parcel.writeInt(likes)
//    }
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    companion object CREATOR : Parcelable.Creator<Song> {
//        override fun createFromParcel(parcel: Parcel): Song {
//            return Song(parcel)
//        }
//
//        override fun newArray(size: Int): Array<Song?> {
//            return arrayOfNulls(size)
//        }
//    }
//}
