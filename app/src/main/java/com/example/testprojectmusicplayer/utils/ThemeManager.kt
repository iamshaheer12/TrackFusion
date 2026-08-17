package com.example.testprojectmusicplayer.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

object ThemeManager {

    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_DARK_MODE = "dark_mode"
    private const val VALUE_UNSET = -1
    private const val VALUE_LIGHT = 0
    private const val VALUE_DARK = 1

    fun apply(context: Context) {
        AppCompatDelegate.setDefaultNightMode(
            when (getMode(context)) {
                VALUE_DARK -> AppCompatDelegate.MODE_NIGHT_YES
                VALUE_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }

    fun isDark(context: Context): Boolean {
        return when (getMode(context)) {
            VALUE_DARK -> true
            VALUE_LIGHT -> false
            else -> {
                val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                nightMode == Configuration.UI_MODE_NIGHT_YES
            }
        }
    }

    fun setDark(context: Context, dark: Boolean) {
        prefs(context).edit { putInt(KEY_DARK_MODE, if (dark) VALUE_DARK else VALUE_LIGHT) }
        AppCompatDelegate.setDefaultNightMode(
            if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun getMode(context: Context): Int = prefs(context).getInt(KEY_DARK_MODE, VALUE_UNSET)

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
