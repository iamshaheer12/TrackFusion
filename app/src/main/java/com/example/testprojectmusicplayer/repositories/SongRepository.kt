package com.example.testprojectmusicplayer.repositories

import com.example.testprojectmusicplayer.model.Song
import com.example.testprojectmusicplayer.utils.UiStates

interface SongRepository {

    suspend fun getAllSongs(result: (UiStates<List<Song>>)->Unit)
    suspend fun getSongsByIds(ids: List<String>,result: (UiStates<List<Song>>) -> Unit)
    suspend fun getSongById(id: String,result: (UiStates<Song?>) -> Unit)
    suspend fun onLikedSong(songId: String,userId: String,albumId:String,result: (UiStates<String>) -> Unit)
    suspend fun isSongLikedByUser(songId:String,userId:String,onLikeStatusChanged: (Boolean) -> Unit)
    suspend fun unLikedSong(songId: String,userId: String,albumId: String,result: (UiStates<String>) -> Unit)
    suspend fun getSongsLikedByUser(userId: String,result: (UiStates<List<Song>?>)->Unit)
    suspend fun getSongByGenres(genres:String, result: (UiStates<List<Song>>) -> Unit)


}