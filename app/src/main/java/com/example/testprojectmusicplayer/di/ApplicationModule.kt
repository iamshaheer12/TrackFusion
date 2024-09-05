package com.example.testprojectmusicplayer.di

import android.app.Service
import android.content.Context
import android.content.SharedPreferences
import com.example.testprojectmusicplayer.utils.AudioPlaybackService
import com.example.testprojectmusicplayer.utils.SharedPrefCons
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ApplicationModule{



    @Provides
    @Singleton
    fun provideGsonInstance():Gson{
        return Gson()
    }
    @Provides
    @Singleton
    fun provideSharedPreference(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(SharedPrefCons.localPref,Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun providesService():Service{
        return AudioPlaybackService()
    }


}