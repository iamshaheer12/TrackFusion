package com.example.testprojectmusicplayer.repositories

import android.net.Uri
import com.example.testprojectmusicplayer.model.User
import com.example.testprojectmusicplayer.utils.UiStates

interface AuthRepository {

  suspend fun loginWithEmailPassword(email:String,password:String,result: (UiStates<String>)->Unit)
  suspend  fun createAccountWithEmailPassword(email: String,password: String,user: User,result: (UiStates<String>) -> Unit)
  suspend  fun sendLoginEmail(email: String, result: (UiStates<String>) -> Unit)
  suspend  fun signUpWithGoogle(idToken:String,result: (UiStates<String>) -> Unit)
  fun storeSession(idToken: String,result: (User?) -> Unit)
  fun updateUser(user: User,result: (UiStates<String>) -> Unit)
  suspend fun uploadingUserImage(imageUrl : Uri, result: (UiStates<String>) -> Unit)
  suspend fun signOut(result: (UiStates<String>) -> Unit)
  suspend fun forgotPassword(email: String,result: (UiStates<String>) -> Unit)

}