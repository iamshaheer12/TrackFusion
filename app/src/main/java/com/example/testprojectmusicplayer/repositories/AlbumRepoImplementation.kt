package com.example.testprojectmusicplayer.repositories

import android.annotation.SuppressLint
import com.example.testprojectmusicplayer.model.Album
import com.example.testprojectmusicplayer.model.Song
import com.example.testprojectmusicplayer.utils.FireStoreCons
import com.example.testprojectmusicplayer.utils.UiStates
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AlbumRepoImplementation(
    private val firestore: FirebaseFirestore
):AlbumRepository {

    override suspend fun getAlbums(result: (UiStates<List<Album>>) -> Unit) {
        try {
            firestore.collection(FireStoreCons.albumCollection)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val albums = ArrayList<Album>()
                    for (document in querySnapshot) {
                        val album = document.toObject(Album::class.java)
                        albums.add(album)
                    }
                    result.invoke(UiStates.Success(albums))
                }
                .addOnFailureListener { exception ->
                    result.invoke(UiStates.Failure(exception.localizedMessage ?: "Unknown error occurred"))
                }
        } catch (e: Exception) {
            // Handle any other unexpected exceptions
            result.invoke(UiStates.Failure(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }



    override suspend fun getAlbumById(id: String, result: (UiStates<Album?>) -> Unit) {
        try {
            firestore.collection(FireStoreCons.albumCollection).document(id)
                .get()
                .addOnSuccessListener {
                    val album =  it.toObject<Album>()
                    result.invoke(UiStates.Success(album))
                }
                .addOnFailureListener {
                    result.invoke( UiStates.Failure(it.localizedMessage))
                }

        }
        catch (e:Exception){
            result.invoke(UiStates.Failure(e.localizedMessage ?: "An unexpected error occurred"))
        }

    }

    override suspend fun getAlbumByIds(ids: List<String>, result: (UiStates<List<Album>>) -> Unit) {
        try {

            firestore.collection(FireStoreCons.albumCollection)
                .whereIn("id",ids)
                .get()
                .addOnSuccessListener {
                        querySnapshot ->
                    val albums = ArrayList<Album>()
                    for (document in querySnapshot) {
                        val album = document.toObject(Album::class.java)
                        albums.add(album)
                    }
                    result.invoke(UiStates.Success(albums))
                }
                .addOnFailureListener { exception ->
                    result.invoke(UiStates.Failure(exception.localizedMessage ?: "Unknown error occurred"))
                }
        } catch (e: Exception) {
            // Handle any other unexpected exceptions
            result.invoke(UiStates.Failure(e.localizedMessage ?: "An unexpected error occurred"))
        }

    }

    override suspend fun isAlbumLikedByUser(albumId: String, userId: String): Boolean {
        val document = firestore.collection(FireStoreCons.albumCollection).document(albumId).get().await()
        val likedBy = document.get("likedBy") as? List<String> ?: emptyList()
        return likedBy.contains(userId)

    }

    override suspend fun onLikedAlbum(
        albumId: String,
        id: String,
        result: (UiStates<String>) -> Unit
    ) {
        try {
            val document = firestore.collection(FireStoreCons.albumCollection).document(albumId)
            val runTransaction = firestore.runTransaction{
                    transaction ->
                val snapshot = transaction.get(document)
                val like = snapshot.getLong("like")?:0
                val likedBy = snapshot.get("likedBy")as? List<String>?: emptyList()
                if (!likedBy.contains(id)){
                    val newLike = like + 1
                    val newLikedBy = likedBy+id
                    transaction.update(document,"like",newLike)
                    transaction.update(document,"likedBy",newLikedBy)

                }
            }
                .addOnSuccessListener {
                    result.invoke(UiStates.Success("Successfully liked Album"))

                }
                .addOnFailureListener {
                    result.invoke(UiStates.Failure("An unknown error"))
                }

        }
        catch (e:Exception){
            result.invoke(UiStates.Failure("An unknown error  "))
        }

    }

    override suspend fun unLikedAlbum(
        albumId: String,
        id: String,
        result: (UiStates<String>) -> Unit
    ) {
        try {
            val document = firestore.collection(FireStoreCons.albumCollection).document(albumId)
            val runTransaction = firestore.runTransaction{
                    transaction ->
                val snapshot = transaction.get(document)
                val like = snapshot.getLong("like")?:0
                val likedBy = snapshot.get("likedBy")as? List<String>?: emptyList()
                if (likedBy.contains(id)){
                    val newLike = like - 1
                    val newLikedBy = likedBy-id
                    transaction.update(document,"like",newLike)
                    transaction.update(document,"likedBy",newLikedBy)

                }
            }
                .addOnSuccessListener {
                    result.invoke(UiStates.Success("Successfully Unliked Album"))

                }
                .addOnFailureListener {
                    result.invoke(UiStates.Failure("An unknown error"))
                }

        }
        catch (e:Exception){
            result.invoke(UiStates.Failure("An unknown error  "))
        }
    }




    override suspend fun getAlbumsLikedByUser(
        userId: String,
        result: (UiStates<List<Album>?>) -> Unit
    ) {
        firestore.collection(FireStoreCons.albumCollection)
            .whereArrayContains("likedBy", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val albums = querySnapshot?.documents?.mapNotNull { document ->
                    document.toObject(Album::class.java)
                }
                result.invoke(UiStates.Success(albums))
            }
            .addOnFailureListener { exception ->
                // Handle any errors
                // exception.printStackTrace()
                result.invoke(UiStates.Failure(exception.localizedMessage))
            }
    }

    @SuppressLint("SuspiciousIndentation")
    override suspend fun createAlbum(
        album: Album,
        result: (UiStates<String>) -> Unit
    ) {
        val document = firestore.collection(FireStoreCons.albumCollection).document()
        album.id = document.id
          document.set(album)
              .addOnSuccessListener {
                  result.invoke(UiStates.Success("Album Created Successfully"))
              }
              .addOnFailureListener {
                  result.invoke(UiStates.Failure("Sorry Album is not created"))
              }

    }

    override suspend fun deleteAlbum(id: String,result: (UiStates<String>) -> Unit) {
        firestore.collection(FireStoreCons.albumCollection).document(id)
            .delete()
            .addOnSuccessListener {
                result.invoke(UiStates.Success("Deleted Successfully"))
            }
            .addOnFailureListener {
                result.invoke(UiStates.Failure("Sorry something going wrong"))
            }


    }

    override suspend fun updateAlbum(album: Album, result: (UiStates<String>) -> Unit) {
        val document = firestore.collection(FireStoreCons.albumCollection).document()
        album.id = document.id
        document.set(album)
            .addOnSuccessListener {
                result.invoke(UiStates.Success("Album Updated  Successfully"))
            }
            .addOnFailureListener {
                result.invoke(UiStates.Failure("Sorry Album is not created"))
            }

    }

    override suspend fun addSongToAlbums(
        songId: String,
        list: List<String>,
        result: (UiStates<String>) -> Unit
    ) {

        val albumsCollection = firestore.collection(FireStoreCons.albumCollection) // Collection name for albums

        try {
            // Iterate through each album ID
            list.forEach { albumId ->
                firestore.runTransaction { transaction ->
                    // Get the reference to the album document
                    val albumRef = albumsCollection.document(albumId)

                    // Get the current snapshot of the album document
                    val albumSnapshot = transaction.get(albumRef)

                    // Retrieve the current list of songs
                    val currentSongs = albumSnapshot.get("songs") as? List<String> ?: emptyList()

                    // Check if the song ID is already in the album's song list
                    if (!currentSongs.contains(songId)) {
                        // If not, add the song ID to the list
                        transaction.update(albumRef, "songs", FieldValue.arrayUnion(songId))
                    }
                }.addOnSuccessListener {
                    // Handle success for each album update
                    result(UiStates.Success("Song added to album: $albumId"))
                }.addOnFailureListener { e ->
                    // Handle failure for each album update
                    result(UiStates.Failure("Failed to add song to album: $albumId", ))
                }
            }
        } catch (e: Exception) {
            // Handle any exceptions that occur during the operation
            result(UiStates.Failure("An error occurred while adding the song to albums", ))
        }
    }


    override suspend fun removeSongFromAlbum(songId: String,albumId: String, result: (UiStates<String>) -> Unit) {

        try {
            val document = firestore.collection(FireStoreCons.albumCollection).document(albumId)
        val transaction =     firestore.runTransaction {transaction->

            val snapshot = transaction.get(document)
            val songs = snapshot.get("songs")as? List<String>?: emptyList()
            if (songs.contains(songId)){
               val  newSongs  = songs - songId
                transaction.update(document,"songs",newSongs)


            }

        }
            .addOnSuccessListener {
                result.invoke(UiStates.Success("Successfully removed from  Album"))

            }
            .addOnFailureListener{
                result.invoke(UiStates.Failure("Unknown error"))

            }

        }
        catch (e:Exception){
            result.invoke(UiStates.Failure("Unknown error"))
        }



    }

}