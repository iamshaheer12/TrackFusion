package com.example.testprojectmusicplayer.repositories

import android.annotation.SuppressLint
import com.example.testprojectmusicplayer.model.Song
import com.example.testprojectmusicplayer.utils.FireStoreCons
import com.example.testprojectmusicplayer.utils.UiStates
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SongRepoImplementation(
    private val firestore: FirebaseFirestore,
): SongRepository {


    override suspend fun getSongById(id: String, result: (UiStates<Song?>) -> Unit) {
        try {
            firestore.collection(FireStoreCons.songsCollection).document(id)
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
            firestore.collection(FireStoreCons.songsCollection)
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

        firestore.collection(FireStoreCons.songsCollection)
            .whereIn("id",ids)
            .get()
            .addOnSuccessListener {
                    querySnapshot ->
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



    override suspend fun isSongLikedByUser(songId: String, userId: String): Boolean {
        val document = firestore.collection(FireStoreCons.songsCollection).document(songId).get().await()
        val likedBy = document.get("likedBy") as? List<String> ?: emptyList()
        return likedBy.contains(userId)

    }

    override suspend fun onLikedSong(
        songId: String,
        id: String,
        result: (UiStates<String>) -> Unit
    ) {
        try {

            val document = firestore.collection(FireStoreCons.songsCollection).document(songId)
            val runTransaction = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(document)
                val likes = snapshot.getLong("like") ?: 0
                val likedBy = snapshot.get("likedBy") as? List<String> ?: emptyList()
                if (!likedBy.contains(id)){
                    val newLikes = likes + 1
                    val newLikedBy = likedBy+id
                    transaction.update(document,"like",newLikes)
                    transaction.update(document,"likedBy",newLikedBy)
                }


            }.addOnSuccessListener {
                result.invoke(UiStates.Success("Liked Successfully"))
            }
                .addOnFailureListener {
                    result.invoke(UiStates.Failure("Sorry !"))
                }


        }
        catch (e:Exception){
            result.invoke(UiStates.Failure("An unknown error occurred"))
        }


    }

    override suspend fun unLikedSong(
        songId: String,
        id: String,
        result: (UiStates<String>) -> Unit
    ) {
        try {
            val document = firestore.collection(FireStoreCons.songsCollection).document(songId)
            val runTransaction = firestore.runTransaction{transaction ->
                val snapshot = transaction.get(document)
                val like = snapshot.getLong("like")?:0
                val likedBy = snapshot.get("likedBy") as? List<String> ?:emptyList()
                if (likedBy.contains(id)){
                    val newLike = like -1
                    val newLikedBy = likedBy - id
                    transaction.update(document,"like",newLike)
                    transaction.update(document,"likedBy",newLikedBy)



                }
            }.addOnSuccessListener {
                result.invoke(UiStates.Success("Successfully Unliked"))

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
    ) {

            firestore.collection(FireStoreCons.songsCollection)
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
                    result.invoke(UiStates.Failure(exception.localizedMessage))
                }
        }
}