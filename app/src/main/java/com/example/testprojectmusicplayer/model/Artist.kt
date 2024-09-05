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


//    ) : Parcelable {
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
//        parcel.writeString(id)
//        parcel.writeString(name)
//        parcel.writeString(imageUrl)
//        parcel.writeInt(followers)
//        parcel.writeStringList(songs)
//    }
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    companion object CREATOR : Parcelable.Creator<Artist> {
//        override fun createFromParcel(parcel: Parcel): Artist {
//            return Artist(parcel)
//        }
//
//        override fun newArray(size: Int): Array<Artist?> {
//            return arrayOfNulls(size)
//        }
//    }
//}