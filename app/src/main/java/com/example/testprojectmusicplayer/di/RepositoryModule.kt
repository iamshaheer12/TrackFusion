package com.example.testprojectmusicplayer.di

import android.content.SharedPreferences
import com.example.testprojectmusicplayer.repositories.AlbumRepoImplementation
import com.example.testprojectmusicplayer.repositories.AlbumRepository
import com.example.testprojectmusicplayer.repositories.ArtistRepoImplementation
import com.example.testprojectmusicplayer.repositories.ArtistRepository
import com.example.testprojectmusicplayer.repositories.AuthRepoImplementation
import com.example.testprojectmusicplayer.repositories.AuthRepository
import com.example.testprojectmusicplayer.repositories.SongRepoImplementation
import com.example.testprojectmusicplayer.repositories.SongRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {


    @Singleton
    @Provides
    fun provideSongRepo(
         firestore: FirebaseFirestore
    ):SongRepository {
        return SongRepoImplementation(firestore)
    }
    @Singleton
    @Provides
    fun provideArtistRepo(
        firestore: FirebaseFirestore
    ):ArtistRepository {
        return ArtistRepoImplementation(firestore)
    }

    @Singleton
    @Provides
    fun provideAlbumRepo(
        firestore: FirebaseFirestore
    ):AlbumRepository{
        return AlbumRepoImplementation(firestore)
    }

    @Provides
    @Singleton
    fun provideAuthRepo(
        firestore: FirebaseFirestore,
        sharedPreferences: SharedPreferences,
        gson: Gson,
        firebaseAuth: FirebaseAuth,
        albumRepository: AlbumRepository,
        firebaseStorage: FirebaseStorage

    ):AuthRepository{
        return  AuthRepoImplementation(
            firestore = firestore,
            firebaseAuth = firebaseAuth,
            sharedPref = sharedPreferences,
            gson = gson,
            albumRepository = albumRepository,
            firebaseStorage = firebaseStorage

        )
    }
}