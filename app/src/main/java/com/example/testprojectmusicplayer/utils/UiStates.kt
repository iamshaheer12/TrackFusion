package com.example.testprojectmusicplayer.utils

import java.lang.Error

sealed class UiStates<out T> {
    data object Loading:UiStates<Nothing>()
    data object Initial : UiStates<Nothing>()
    data class Success<out T>(val data :T):UiStates<T>(){
    }
    data class Failure(val error: String):UiStates<Nothing>(){

    }
}