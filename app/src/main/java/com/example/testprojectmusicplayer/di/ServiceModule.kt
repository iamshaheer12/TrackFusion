package com.example.testprojectmusicplayer.di

import com.example.testprojectmusicplayer.utils.AudioPlaybackServiceProvider
import android.content.Context
import androidx.core.app.ServiceCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped
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
