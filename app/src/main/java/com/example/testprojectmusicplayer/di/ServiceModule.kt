package com.example.testprojectmusicplayer.di

import com.example.testprojectmusicplayer.utils.AudioPlaybackServiceProvider
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.InstallIn
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AudioPlaybackModule {

    @Provides
    @Singleton
    fun provideAudioPlaybackServiceProvider(context: Context): AudioPlaybackServiceProvider {
        return AudioPlaybackServiceProvider(context)
    }
}
