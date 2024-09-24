package com.example.testprojectmusicplayer.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testprojectmusicplayer.model.User
import com.example.testprojectmusicplayer.repositories.AuthRepoImplementation
import com.example.testprojectmusicplayer.repositories.AuthRepository
import com.example.testprojectmusicplayer.utils.UiStates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // State to track the login state or registration state
    private val _authState = MutableStateFlow<UiStates<String>>(UiStates.Loading)
    val authState: StateFlow<UiStates<String>> = _authState

    private val _loginState:MutableStateFlow<UiStates<String>> = MutableStateFlow<UiStates<String>>(UiStates.Loading)
    val loginState : StateFlow<UiStates<String>> = _loginState

    private val _forgotPassword = MutableStateFlow<UiStates<String>>(UiStates.Loading)
    val forgotPassword :StateFlow<UiStates<String>> = _forgotPassword


    private val _googleLoginState:MutableStateFlow<UiStates<String>> = MutableStateFlow<UiStates<String>>(UiStates.Loading)
    val googleLoginState : StateFlow<UiStates<String>> = _googleLoginState

    private val _loginEmailSentState:MutableStateFlow<UiStates<String>> = MutableStateFlow<UiStates<String>>(UiStates.Loading)
    val loginEmailSentState : StateFlow<UiStates<String>> = _loginEmailSentState


    // Method to create an account with email and password
    fun createAccountWithEmailPassword(email: String, password: String, user: User) {
        _authState.update { UiStates.Loading } // Set the state to loading

        // Launch a coroutine in the ViewModel's scope
        viewModelScope.launch {
            // Call the repository function to create an account
            authRepository.createAccountWithEmailPassword(email, password, user) { uiState ->
                // Update the state based on the result
                _authState.update { uiState }
            }
        }
    }
    fun loginWithEmailPassword(email: String,password: String){
        _loginState.update { UiStates.Loading }
        viewModelScope.launch {
            authRepository.loginWithEmailPassword(email,password){
                uiState ->
                _loginState.update { uiState }
            }

        }
    }
    fun signWithGoogle(id:String){
        _googleLoginState.update { UiStates.Loading }
        viewModelScope.launch {
            authRepository.signUpWithGoogle(id){
                uiState ->
                _googleLoginState.update { uiState }
            }
        }


    }


    fun forgotPasswordLink(email: String){
        viewModelScope.launch {
            authRepository.forgotPassword(email){
                state ->
                _forgotPassword.update {
                    state
                }
            }
        }
    }

    fun sendEmailLinkForLogging(email: String){
        _loginEmailSentState.update { UiStates.Loading }
        viewModelScope.launch {
            authRepository.sendLoginEmail(email){
                uiStates ->
                _loginEmailSentState.update { uiStates }
            }
        }
    }

}
