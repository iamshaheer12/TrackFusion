package com.example.testprojectmusicplayer.repositories

import com.example.testprojectmusicplayer.model.User
import com.example.testprojectmusicplayer.utils.UiStates

interface AuthRepository {

  suspend fun loginWithEmailPassword(email:String,password:String,result: (UiStates<String>)->Unit)
  suspend  fun createAccountWithEmailPassword(email: String,password: String,user: User,result: (UiStates<String>) -> Unit)
  suspend  fun sendLoginEmail(email: String, result: (UiStates<String>) -> Unit)
  suspend  fun signUpWithGoogle(idToken:String,result: (UiStates<String>) -> Unit)
  fun storeSession(idToken: String,result: (User?) -> Unit)
  fun updateUser(user: User,result: (UiStates<String>) -> Unit)
}