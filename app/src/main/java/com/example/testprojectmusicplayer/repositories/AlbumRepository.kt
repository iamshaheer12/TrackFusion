package com.example.testprojectmusicplayer.repositories

import com.example.testprojectmusicplayer.model.Album
import com.example.testprojectmusicplayer.utils.UiStates


interface AlbumRepository {

    suspend fun getAlbums(result: (UiStates<List<Album>>) -> Unit)
    suspend fun getAlbumByIds(ids:List<String>,result: (UiStates<List<Album>>) -> Unit)
    suspend fun getAlbumById(id:String,result: (UiStates<Album?>) -> Unit)
    suspend fun onLikedAlbum(albumId: String,id:String,result: (UiStates<String>) -> Unit)
    suspend fun isAlbumLikedByUser(albumId:String,userId:String,onLikeStatusChanged: (Boolean) -> Unit)
    suspend fun unLikedAlbum(albumId: String,id:String,result: (UiStates<String>) -> Unit)
    suspend fun getAlbumsLikedByUser(userId: String,result: (UiStates<List<Album>>)->Unit)
    suspend fun getAlbumCreatedByUser(userId: String,result: (UiStates<List<Album>>)->Unit)
    suspend fun createAlbum(album: Album,result: (UiStates<String>) -> Unit)
    suspend fun updateAlbum(album: Album,result: (UiStates<String>) -> Unit)
    suspend fun deleteAlbum(id: String,result: (UiStates<String>) -> Unit)
    suspend fun addSongToAlbums(songId:String,list: List<String>,result: (UiStates<String>) -> Unit)
    suspend fun removeSongFromAlbum(songId: String,albumId: String,result: (UiStates<String>) -> Unit)
    suspend fun addAlbumToRecentlyPlayed(userId: String, album: Album)
    suspend fun removeOldAlbumsIfNeeded(userId: String)
    suspend fun getRecentlyPlayedAlbums(userId: String, callback: (UiStates<List<Album>>) -> Unit)

}