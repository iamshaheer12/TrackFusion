package com.example.testprojectmusicplayer.di

import android.content.Context
import com.example.testprojectmusicplayer.utils.MediaControllerProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AudioPlaybackModule {


    @Singleton
    @Provides
    fun provideMediaControllerProvider(context: Context): MediaControllerProvider {
        return MediaControllerProvider(context)
    }
}
