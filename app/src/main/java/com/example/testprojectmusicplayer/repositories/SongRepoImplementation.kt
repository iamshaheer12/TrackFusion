package com.example.testprojectmusicplayer.repositories

import android.util.Log
import com.example.testprojectmusicplayer.model.Song
import com.example.testprojectmusicplayer.utils.FireStoreCons
import com.example.testprojectmusicplayer.utils.UiStates
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

class SongRepoImplementation(
    private val firestore: FirebaseFirestore,
): SongRepository {


    override suspend fun getSongById(id: String, result: (UiStates<Song?>) -> Unit) {
        try {
            firestore.collection(FireStoreCons.SONG_COLLECTION).document(id)
                .get()
                .addOnSuccessListener {
                    val song =  it.toObject<Song>()
                  result.invoke(UiStates.Success(song))
                }
                .addOnFailureListener {
                   result.invoke( UiStates.Failure(it.localizedMessage))
                }

        }
        catch (e:Exception){
            result.invoke(UiStates.Failure(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    override suspend fun getAllSongs(result: (UiStates<List<Song>>) -> Unit) {
        try {
            firestore.collection(FireStoreCons.SONG_COLLECTION)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val songs = ArrayList<Song>()
                    for (document in querySnapshot) {
                        val song = document.toObject(Song::class.java)
                        songs.add(song)
                    }
                    result.invoke(UiStates.Success(songs))
                }
                .addOnFailureListener { exception ->
                    result.invoke(UiStates.Failure(exception.localizedMessage ?: "Unknown error occurred"))
                }
        } catch (e: Exception) {
            // Handle any other unexpected exceptions
            result.invoke(UiStates.Failure(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }


    override suspend fun getSongsByIds(ids: List<String>, result: (UiStates<List<Song>>) -> Unit) {
        try {
            if (ids.isEmpty()) {
                // Return an empty list of songs if the ids list is empty
                result.invoke(UiStates.Success(emptyList()))
                return
            } else {
                // Listen for real-time updates
                firestore.collection(FireStoreCons.SONG_COLLECTION)
                    .whereIn("id", ids)
                    .addSnapshotListener { querySnapshot, exception ->
                        if (exception != null) {
                            result.invoke(UiStates.Failure(exception.localizedMessage ?: "Unknown error occurred"))
                            return@addSnapshotListener
                        }

                        if (querySnapshot != null && !querySnapshot.isEmpty) {
                            val songs = ArrayList<Song>()
                            for (document in querySnapshot) {
                                val song = document.toObject(Song::class.java)
                                songs.add(song)
                            }
                            // Return the updated list of songs
                            result.invoke(UiStates.Success(songs))
                        } else {
                            // If no documents are found
                            result.invoke(UiStates.Success(emptyList()))
                        }
                    }
            }

        } catch (e: Exception) {
            // Handle any other unexpected exceptions
            result.invoke(UiStates.Failure(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }





    override suspend fun isSongLikedByUser(songId: String, userId: String,onLikeStatusChanged: (Boolean) -> Unit){
        val albumDocumentRef = firestore.collection(FireStoreCons.SONG_COLLECTION).document(songId)

        albumDocumentRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("FirestoreListener", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val likedBy = snapshot.get("likedBy") as? List<String> ?: emptyList()
                val isLikedByUser = likedBy.contains(userId)
                onLikeStatusChanged(isLikedByUser)
            } else {
                Log.d("Firestore Listener", "Current data: null")
            }
        }

    }

    override suspend fun onLikedSong(
        songId: String,
        userId: String,
        albumId: String,

        result: (UiStates<String>) -> Unit
    ) {
        try {
            val document = firestore.collection(FireStoreCons.SONG_COLLECTION).document(songId)

            // Run the transaction to update the song document
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(document)
                val likes = snapshot.getLong("like") ?: 0
                val likedBy = snapshot.get("likedBy") as? List<String> ?: emptyList()

                if (!likedBy.contains(userId)) {
                    val newLikes = likes + 1
                    val newLikedBy = likedBy + userId
                    transaction.update(document, "like", newLikes)
                    transaction.update(document, "likedBy", newLikedBy)
                }
            }.addOnSuccessListener {
                // On successful transaction, proceed to update the album
                addSongInRepositoryOnLike(id = albumId, songId = songId) { albumResult ->
                    when (albumResult) {
                        is UiStates.Success -> result.invoke(UiStates.Success("Successfully Liked and Added to Album"))
                        is UiStates.Failure -> result.invoke(UiStates.Failure("Liked Successfully but Failed to Update Album"))
                        else -> {}
                    }
                }
            }.addOnFailureListener {
                // Handle failure in liking the song
                result.invoke(UiStates.Failure("Failed to Like Song"))
            }
        } catch (e: Exception) {
            result.invoke(UiStates.Failure("An unknown error occurred"))
        }
    }

    private fun addSongInRepositoryOnLike(id: String, songId: String, result: (UiStates<String>) -> Unit) {
        if (id.isBlank()) {
            result.invoke(UiStates.Success("Successfully Liked Song"))
            return
        }

        val document = firestore.collection(FireStoreCons.ALBUM_COLLECTION).document(id)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(document)
            val songs = snapshot.get("songs") as? List<String> ?: emptyList()

            if (!songs.contains(songId)) {
                val newSongs = songs + songId
                transaction.update(document, "songs", newSongs)
            }
        }.addOnSuccessListener {
            // Successfully updated the album
            result.invoke(UiStates.Success("Successfully Added Song to Album"))
        }.addOnFailureListener {
            // Handle failure in updating the album
            result.invoke(UiStates.Failure("Failed to Add Song to Album"))
        }
    }

    private fun removeSongInRepositoryOnUnLike(id: String, songId: String, result: (UiStates<String>) -> Unit){
        if (id.isBlank()) {
            result.invoke(UiStates.Success("Successfully Unliked Song"))
            return
        }

        val document = firestore.collection(FireStoreCons.ALBUM_COLLECTION).document(id)
        val transaction = firestore.runTransaction {
                transaction ->
            val snapshot = transaction.get(document)
            val songs = snapshot.get("songs")as? List<String>?: emptyList()
            if (songs.contains(songId)){
                val newSong = songs - songId
                transaction.update(document,"songs",newSong)

            }

        }.addOnSuccessListener {
            result.invoke(UiStates.Success("Successfully Removed Song from Album"))
        }.addOnFailureListener {
            result.invoke(UiStates.Failure("Failed to Remove Song from Album"))
        }
    }

    override suspend fun unLikedSong(
        songId: String,
        userId: String,
        albumId: String,
        result: (UiStates<String>) -> Unit
    ) {
        try {
            val document = firestore.collection(FireStoreCons.SONG_COLLECTION).document(songId)
            val runTransaction = firestore.runTransaction{transaction ->
                val snapshot = transaction.get(document)
                val like = snapshot.getLong("like")?:0
                val likedBy = snapshot.get("likedBy") as? List<String> ?:emptyList()
                if (likedBy.contains(userId)){
                    val newLike = like -1
                    val newLikedBy = likedBy - userId
                    transaction.update(document,"like",newLike)
                    transaction.update(document,"likedBy",newLikedBy)



                }
            }.addOnSuccessListener {
                removeSongInRepositoryOnUnLike(albumId,songId){
                        albumResult ->
                    when (albumResult) {
                        is UiStates.Success -> result.invoke(UiStates.Success("Successfully UnLiked and remove from Album"))
                        is UiStates.Failure -> result.invoke(UiStates.Failure("Unliked Successfully but Failed to Update Album"))
                        else -> {}
                    }
                }

            }
                .addOnFailureListener {
                    result.invoke(UiStates.Failure("Unknown error occurred"))
                }

        }
        catch (e:Exception){
            result.invoke(UiStates.Failure("Unknown error occurred"))
        }




    }

    override suspend fun getSongsLikedByUser(
        userId: String,
        result: (UiStates<List<Song>?>) -> Unit
    ) {try {

        firestore.collection(FireStoreCons.SONG_COLLECTION)
            .whereArrayContains("likedBy", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val songs = querySnapshot?.documents?.mapNotNull { document ->
                    document.toObject(Song::class.java)
                }
                result.invoke(UiStates.Success(songs))
            }
            .addOnFailureListener { exception ->
                // Handle any errors
                // exception.printStackTrace()
                result.invoke(UiStates.Failure(exception.localizedMessage.toString()))
            }
    }
    catch (e:Exception){
        result.invoke(UiStates.Failure(e.localizedMessage.toString()))

    }

        }


    override suspend fun getSongByGenres(genres: String, result: (UiStates<List<Song>>) -> Unit) {
        try {
            val document = firestore.collection(FireStoreCons.SONG_COLLECTION).whereEqualTo("genres",genres)
                .get()
                .addOnSuccessListener {
                        querySnapshot ->
                    val songs = querySnapshot.documents.mapNotNull { document ->
                        document.toObject(Song::class.java)
                    }
                    result.invoke(UiStates.Success(songs))
                }
                .addOnFailureListener {
                    result.invoke(UiStates.Failure(it.localizedMessage))
                }
        }
        catch (e:Exception){
            result.invoke(UiStates.Failure(e.localizedMessage))

        }

    }
}