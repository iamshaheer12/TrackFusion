package com.example.testprojectmusicplayer.view

import android.app.Application
import com.example.testprojectmusicplayer.utils.ThemeManager
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FusionApplication: Application() {


    override fun onCreate() {
        super.onCreate()

        ThemeManager.apply(this)
        FirebaseApp.initializeApp(this)
    }
}