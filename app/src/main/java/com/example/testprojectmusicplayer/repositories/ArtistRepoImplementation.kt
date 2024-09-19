package com.example.testprojectmusicplayer.repositories

import com.example.testprojectmusicplayer.model.Artist
import com.example.testprojectmusicplayer.utils.FireStoreCons
import com.example.testprojectmusicplayer.utils.UiStates
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class ArtistRepoImplementation(
    private val firestore: FirebaseFirestore
) :ArtistRepository{

    override suspend fun getArtists(result: (UiStates<List<Artist>>) -> Unit) {
            try {
                firestore.collection(FireStoreCons.ARTIST_COLLECTION)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val artists = ArrayList<Artist>()
                        for (document in querySnapshot) {
                            val artist = document.toObject(Artist::class.java)
                            artists.add(artist)
                        }
                        result.invoke(UiStates.Success(artists))
                    }
                    .addOnFailureListener { exception ->
                        result.invoke(UiStates.Failure(exception.localizedMessage ?: "Unknown error occurred"))
                    }
            } catch (e: Exception) {
                // Handle any other unexpected exceptions
                result.invoke(UiStates.Failure(e.localizedMessage ?: "An unexpected error occurred"))
            }



    }



    override suspend fun getArtistById(id: String, result: (UiStates<Artist?>) -> Unit) {
        try {
            firestore.collection(FireStoreCons.ARTIST_COLLECTION).document(id)
                .get()
                .addOnSuccessListener {
                    val artist =  it.toObject<Artist>()
                    result.invoke(UiStates.Success(artist))
                }
                .addOnFailureListener {
                    result.invoke( UiStates.Failure(it.localizedMessage))
                }

        }
        catch (e:Exception){
            result.invoke(UiStates.Failure(e.localizedMessage ?: "An unexpected error occurred"))
        }

    }

    override suspend fun getArtistByIds(ids: List<String>, result: (UiStates<List<Artist>>) -> Unit) {
        try {

            firestore.collection(FireStoreCons.ARTIST_COLLECTION)
                .whereIn("id",ids)
                .get()
                .addOnSuccessListener {
                        querySnapshot ->
                    val artists = ArrayList<Artist>()
                    for (document in querySnapshot) {
                        val artist = document.toObject(Artist::class.java)
                        artists.add(artist)
                    }
                    result.invoke(UiStates.Success(artists))
                }
                .addOnFailureListener { exception ->
                    result.invoke(UiStates.Failure(exception.localizedMessage ?: "Unknown error occurred"))
                }
        } catch (e: Exception) {
            // Handle any other unexpected exceptions
            result.invoke(UiStates.Failure(e.localizedMessage ?: "An unexpected error occurred"))
        }

    }

    override suspend fun isArtistLikedByUser(artistId: String, userId: String): Boolean {
        val document = firestore.collection(FireStoreCons.ARTIST_COLLECTION).document(artistId).get().await()
        val likedBy = document.get("likedBy") as? List<String> ?: emptyList()
        return likedBy.contains(userId)
    }

    override suspend fun onLikedArtist(
        artistId: String,
        id: String,
        result: (UiStates<String>) -> Unit
    ) {
        try {
            val document = firestore.collection(FireStoreCons.ARTIST_COLLECTION).document(artistId)
            firestore.runTransaction {transaction ->
                val snapshot = transaction.get(document)
                val like = snapshot.getLong("like")?:0
                val likedBy = snapshot.get("likedBy") as? List<String> ?:emptyList()
                if (!likedBy.contains(id)){
                    val newLike = like + 1
                    val newLikedBy = likedBy + id
                    transaction.update(document,"like",newLike)
                    transaction.update(document,"likedBy",newLikedBy)
                }

            }.addOnSuccessListener {
                result.invoke(UiStates.Success("Liked Successfully"))
            }
                .addOnFailureListener {
                    result.invoke(UiStates.Failure("Error"))
                }


        }
        catch (e:Exception){
            result.invoke(UiStates.Failure("Error"))

        }

    }

    override suspend fun unLikedArtist(
        artistId: String,
        id: String,
        result: (UiStates<String>) -> Unit
    ) {
        try {
            val document = firestore.collection(FireStoreCons.ARTIST_COLLECTION).document(artistId)
            firestore.runTransaction {transaction ->
                val snapshot = transaction.get(document)
                val like = snapshot.getLong("like")?:0
                val likedBy = snapshot.get("likedBy") as? List<String> ?:emptyList()
                if (likedBy.contains(id)){
                    val newLike = like - 1
                    val newLikedBy = likedBy - id
                    transaction.update(document,"like",newLike)
                    transaction.update(document,"likedBy",newLikedBy)
                }

            }.addOnSuccessListener {
                result.invoke(UiStates.Success("Liked Successfully"))
            }
                .addOnFailureListener {
                    result.invoke(UiStates.Failure("Error"))
                }


        }
        catch (e:Exception){
            result.invoke(UiStates.Failure("Error"))

        }
    }

    override suspend fun getArtistsLikedByUser(
        userId: String,
        result: (UiStates<List<Artist>>) -> Unit
    ) {
        firestore.collection(FireStoreCons.ARTIST_COLLECTION)
            .whereArrayContains("likedBy", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val artists = querySnapshot?.documents?.mapNotNull { document ->
                    document.toObject(Artist::class.java)
                } ?: emptyList()
                result.invoke(UiStates.Success(artists))
            }
            .addOnFailureListener { exception ->
                // Handle any errors
                // exception.printStackTrace()
                result.invoke(UiStates.Failure(exception.localizedMessage))
            }
    }
}