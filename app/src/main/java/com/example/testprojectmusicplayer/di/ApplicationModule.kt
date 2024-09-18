package com.example.testprojectmusicplayer.di

import com.example.testprojectmusicplayer.utils.UserObject
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.testprojectmusicplayer.utils.SharedPrefConstants
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
    fun provideContext(application: Application): Context = application.applicationContext



    @Provides
    @Singleton
    fun provideGsonInstance():Gson{
        return Gson()
    }
    @Provides
    @Singleton
    fun provideSharedPreference(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(SharedPrefConstants.LOCAL_PREF,Context.MODE_PRIVATE)
    }

//    @Provides
//    @Singleton
//    fun providesService():Service{
//        return AudioPlaybackService()
//    }


    @Provides
    @Singleton
    fun provideGlideInstance(context: Context): RequestManager {
        return Glide.with(context)
    }
    @Provides
    @Singleton
    fun provideUserObject(
        sharedPreferences: SharedPreferences,
        gson: Gson
    ): UserObject {
        return UserObject(sharedPreferences, gson)
    }

}