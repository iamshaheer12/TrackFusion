package com.example.testprojectmusicplayer.utils

import android.content.SharedPreferences
import com.example.testprojectmusicplayer.model.User
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Ensure it's a singleton if required
class UserObject @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson,
) {
    fun getUser(): User? {
        val userJson = sharedPreferences.getString(SharedPrefConstants.STORE_SESSION, null)
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else {
            null
        }

        // TODO handle exception
    }
}
