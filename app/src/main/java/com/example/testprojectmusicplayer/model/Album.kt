package com.example.testprojectmusicplayer.model

import android.icu.text.CaseMap.Title
import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable
@Parcelize
data class Album(
    val title:String = "",
    val description:String = "",
    val imageUrl: String = "",
    val likes: Int = 0,
   // val createdBy : String = "",
    val likedBy : List<String> = emptyList(),
    var songs:List<String> = emptyList(),
) : Parcelable
//{
//    constructor(parcel: Parcel) : this(
//        parcel.readString()?:"",
//        parcel.readString()?:"",
//        parcel.readString()?:"",
//        parcel.readInt(),
//        parcel.createStringArrayList()?: emptyList()
//    ) {
//    }
//
//    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeString(title)
//        parcel.writeString(description)
//        parcel.writeString(imageUrl)
//        parcel.writeInt(likes)
//        parcel.writeStringList(songs)
//    }
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    companion object CREATOR : Parcelable.Creator<Album> {
//        override fun createFromParcel(parcel: Parcel): Album {
//            return Album(parcel)
//        }
//
//        override fun newArray(size: Int): Array<Album?> {
//            return arrayOfNulls(size)
//        }
//    }
//}