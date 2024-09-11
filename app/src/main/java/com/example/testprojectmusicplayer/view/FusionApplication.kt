package com.example.testprojectmusicplayer.view

import android.app.Application
import com.example.testprojectmusicplayer.utils.AudioPlaybackServiceProvider
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FusionApplication: Application() {


    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
    }
}