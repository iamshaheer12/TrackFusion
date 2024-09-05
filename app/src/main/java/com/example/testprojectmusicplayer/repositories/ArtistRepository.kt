package com.example.testprojectmusicplayer.repositories

import com.example.testprojectmusicplayer.model.Artist
import com.example.testprojectmusicplayer.model.Song
import com.example.testprojectmusicplayer.utils.UiStates

interface ArtistRepository {

    suspend fun getArtists(result: (UiStates<List<Artist>>) -> Unit)
    suspend fun getArtistByIds(ids:List<String>,result: (UiStates<List<Artist>>) -> Unit)
    suspend fun getArtistById(id:String,result: (UiStates<Artist?>) -> Unit)
    suspend fun onLikedArtist(artistId: String,id:String,result: (UiStates<String>) -> Unit)
    suspend fun isArtistLikedByUser(artistId:String,userId:String):Boolean
    suspend fun unLikedArtist(artistId: String,id:String,result: (UiStates<String>) -> Unit)
    suspend fun getArtistsLikedByUser(userId: String,result: (UiStates<List<Artist>?>)->Unit)
}