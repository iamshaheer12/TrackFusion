package com.example.testprojectmusicplayer.repositories

import android.content.Intent
import android.content.SharedPreferences
import com.example.testprojectmusicplayer.model.Album
import com.example.testprojectmusicplayer.model.User
import com.example.testprojectmusicplayer.utils.FireStoreCons
import com.example.testprojectmusicplayer.utils.SharedPrefConstants
import com.example.testprojectmusicplayer.utils.UiStates
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthRepoImplementation(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val sharedPref:SharedPreferences,
    private val gson: Gson,
    private val albumRepository: AlbumRepository

    ):AuthRepository {


   override suspend fun sendLoginEmail(email: String, result: (UiStates<String>) -> Unit) {
        // Configure the ActionCodeSettings
        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setUrl(FireStoreCons.LOGGING_EMAIL) // The link the user clicks
            .setHandleCodeInApp(true) // Handle the link in your app
            .setAndroidPackageName(
                "com.example.testprojectmusicplayer",
                true, /* installIfNotAvailable */
                "8" /* minimumVersion */
            )
            .build()

        // Send the email link
        firebaseAuth.sendSignInLinkToEmail(email, actionCodeSettings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    sharedPref.edit().putString(SharedPrefConstants.LOGGING_EMAIL,email).apply()
                    result.invoke(UiStates.Success("Sign-in link sent to $email. Please check your inbox."))
                } else {
                    result.invoke(UiStates.Failure(task.exception?.localizedMessage ?: "Failed to send sign-in link."))
                }
            }
            .addOnFailureListener { exception ->
                result.invoke(UiStates.Failure(exception.localizedMessage ?: "An error occurred while sending the sign-in link."))
            }
    }
    private fun handleSignInLink(intent: Intent?,result: (UiStates<String>) -> Unit) {
        val emailLink = intent?.data.toString()
        val pendingEmail = sharedPref.getString(SharedPrefConstants.LOGGING_EMAIL,null)

        if (emailLink.isNotEmpty() && firebaseAuth.isSignInWithEmailLink(emailLink)) {
            val email = pendingEmail ?: return

            firebaseAuth.signInWithEmailLink(email, emailLink)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        if (user != null) {
                            // Store additional user data if needed
                            sharedPref.edit().putString(SharedPrefConstants.STORE_SESSION,gson.toJson(user)).apply()
                        }
                        // Clear the pending email
                        sharedPref.edit().putString(SharedPrefConstants.LOGGING_EMAIL,null).apply()
                        result.invoke(UiStates.Success("Logged In Successfully"))
                        // Navigate to the desired screen
                    } else {
                        // Handle sign-in error
                        result.invoke(UiStates.Failure("Failure"))
                    }
                }
                .addOnFailureListener { exception ->
                    result.invoke(UiStates.Failure(exception.localizedMessage))
                    // Handle failure
                }
        }
    }



    override suspend fun signUpWithGoogle (idToken: String, result: (UiStates<String>) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = firebaseAuth.currentUser
                    val user = User(
                        userId = firebaseUser?.uid?:"" ,
                        name = firebaseUser?.displayName?:"",
                        email = firebaseUser?.email?:"",


                    )
                    updateUser(user){state ->
                        when(state){
                            is UiStates.Success ->{
                                //      result.invoke(UiStates.Success("User Created Successfully"))
                                storeSession(idToken = firebaseUser?.uid?:"" ){
                                    if (firebaseUser == null){
                                        result.invoke(UiStates.Failure("Logged In Successfully but session is created"))

                                    }
                                    result.invoke(UiStates.Success("User Created Successfully"))

                                }
                            }
                            is UiStates.Failure ->{
                                result.invoke(UiStates.Failure(state.error))
                            }

                            else -> {
                                result.invoke(UiStates.Failure("Authentication failed check email and password"))


                            }
                        }

                    }




                    result(UiStates.Success("Sign-in successful"))
                } else {
                    result(UiStates.Failure(task.exception?.localizedMessage ?: "Unknown error"))
                }
            }
    }

    override suspend fun createAccountWithEmailPassword(
        email: String,
        password: String,
        user: User,
        result: (UiStates<String>) -> Unit
    ) {
        try {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        user.userId = task.result?.user?.uid ?: ""
                        updateUser(user) { uiStates ->
                            when (uiStates) {
                                is UiStates.Success -> {
                                    // Switch to the IO dispatcher for the repository operations
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            // Create an album after user creation
                                            albumRepository.createAlbum(
                                                album = Album(
                                                    createdBy = user.userId,
                                                    title = "Liked Songs",
                                                    descriptions = "",
                                                    visibility = false
                                                )
                                            ) {
                                                result.invoke(UiStates.Success("User created successfully."))
                                            }
                                        } catch (e: Exception) {
                                            result.invoke(UiStates.Failure("Failed to create album: ${e.message}"))
                                        }
                                    }
                                }
                                is UiStates.Failure -> {
                                    result.invoke(UiStates.Failure(uiStates.error))
                                }
                                else -> {
                                    result.invoke(UiStates.Failure("Failed to update user data."))
                                }
                            }
                        }
                    } else {
                        try {
                            throw task.exception ?: Exception("Invalid authentication")
                        } catch (e: FirebaseAuthWeakPasswordException) {
                            result.invoke(UiStates.Failure("Authentication failed: Password should be at least 6 characters."))
                        } catch (e: FirebaseAuthInvalidCredentialsException) {
                            result.invoke(UiStates.Failure("Authentication failed: Invalid email entered."))
                        } catch (e: FirebaseAuthUserCollisionException) {
                            result.invoke(UiStates.Failure("Authentication failed: Email already registered."))
                        } catch (e: Exception) {
                            result.invoke(UiStates.Failure(e.message.toString()))
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    result.invoke(UiStates.Failure(exception.localizedMessage ?: "An error occurred."))
                }
        } catch (e: Exception) {
            result.invoke(UiStates.Failure(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }


    override suspend fun loginWithEmailPassword(
        email: String,
        password: String,
        result: (UiStates<String>) -> Unit
    ) {
        try {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val id = task.result?.user?.uid ?: ""
                        storeSession(idToken = id) {
                            if (it == null) {
                                result.invoke(UiStates.Failure("Logged in successfully, but user data could not be saved."))
                            } else {
                                result.invoke(UiStates.Success("Logged in successfully."))
                            }
                        }
                    } else {
                        try {
                            throw task.exception ?: Exception("Authentication failed")
                        } catch (e: FirebaseAuthInvalidCredentialsException) {
                            result.invoke(UiStates.Failure("Authentication failed: Invalid credentials entered."))
                        } catch (e: FirebaseAuthInvalidUserException) {
                            result.invoke(UiStates.Failure("Authentication failed: No account found with this email."))
                        } catch (e: Exception) {
                            result.invoke(UiStates.Failure(e.message.toString()))
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    result.invoke(UiStates.Failure(exception.localizedMessage ?: "An error occurred during login."))
                }
        } catch (e: Exception) {
            result.invoke(UiStates.Failure(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }


    override fun storeSession(idToken: String, result: (User?) -> Unit) {
        firestore.collection(FireStoreCons.USER).document(idToken)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    val user = it.result.toObject(User::class.java)
                    result.invoke(user)
                    sharedPref.edit().putString(SharedPrefConstants.STORE_SESSION,gson.toJson(user)).apply()

                }
                else{
                    result.invoke(null)
                }

            }
            .addOnFailureListener {
                result.invoke(null)
            }


    }

    override fun updateUser(user: User, result: (UiStates<String>) -> Unit) {
        try {
            val document = firestore.collection(FireStoreCons.USER).document(user.userId)
            document.set(user) // Assuming you meant to set the `user` object, not `document`
                .addOnCompleteListener {
                    sharedPref.edit().putString(SharedPrefConstants.STORE_SESSION, gson.toJson(user)).apply()
                    result.invoke(UiStates.Success("User Updated Successfully"))
                }
                .addOnFailureListener {
                    result.invoke(UiStates.Failure(it.localizedMessage.toString()))
                }
        } catch (e: Exception) {
            result.invoke(UiStates.Failure(e.localizedMessage.toString()))
        }
    }


}