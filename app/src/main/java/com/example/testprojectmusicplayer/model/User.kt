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
    val likedAlbums: List<String> = emptyList(),
    val recentlyPlayedSongs: List<String> = emptyList()
) : Parcelable

//{
//
//    constructor(parcel: Parcel) : this(
//        name = parcel.readString() ?: "",
//        dob = parcel.readString() ?: "",
//        gender = parcel.readString() ?: "",
//        email = parcel.readString() ?: "",
//        userId = parcel.readString() ?: "",
//        likedSong = parcel.createStringArrayList() ?: emptyList(),
//        likedArtist = parcel.createStringArrayList() ?: emptyList(),
//        likedAlbums = parcel.createStringArrayList() ?: emptyList(),
//        recentlyPlayedSongs = parcel.createStringArrayList() ?: emptyList()
//    )
//
//    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeString(name)
//        parcel.writeString(dob)
//        parcel.writeString(gender)
//        parcel.writeString(email)
//        parcel.writeString(userId)
//        parcel.writeStringList(likedSong)
//        parcel.writeStringList(likedArtist)
//        parcel.writeStringList(likedAlbums)
//        parcel.writeStringList(recentlyPlayedSongs)
//    }
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    companion object CREATOR : Parcelable.Creator<User> {
//        override fun createFromParcel(parcel: Parcel): User {
//            return User(parcel)
//        }
//
//        override fun newArray(size: Int): Array<User?> {
//            return arrayOfNulls(size)
//        }
//    }
//}
