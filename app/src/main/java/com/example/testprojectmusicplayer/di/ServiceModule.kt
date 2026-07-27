package com.example.testprojectmusicplayer.di

import android.content.Context
import com.example.testprojectmusicplayer.utils.AudioPlaybackServiceProvider
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
    fun provideAudioPlaybackServiceProvider(context: Context): AudioPlaybackServiceProvider {
        return AudioPlaybackServiceProvider(context)
    }
}
