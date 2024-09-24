package com.example.testprojectmusicplayer.viewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testprojectmusicplayer.model.User
import com.example.testprojectmusicplayer.repositories.AuthRepository
import com.example.testprojectmusicplayer.utils.UiStates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(

    private val  authRepository: AuthRepository

): ViewModel() {



    private val _signOutState = MutableStateFlow<UiStates<String>>(UiStates.Loading)
    val signOutState : StateFlow<UiStates<String>> = _signOutState

    private val _uploadUserImage = MutableStateFlow<UiStates<String>>(UiStates.Loading)
    val uploadUserImage : StateFlow<UiStates<String>> = _uploadUserImage

    private val _updateUser = MutableStateFlow<UiStates<String>>(UiStates.Loading)
    val updateUser : StateFlow<UiStates<String>> = _updateUser



     fun singOut(){
        viewModelScope.launch {
            authRepository.signOut {
                uiStates ->
                _signOutState.update {
                    uiStates
                }
            }
        }
    }

    fun uploadUserImage(imageUrl : Uri){
        viewModelScope.launch {
            authRepository.uploadingUserImage(imageUrl){
                state ->
                _uploadUserImage.update {
                    state
                }
            }
        }
    }

    fun updateUser(user: User){
        viewModelScope.launch {
            authRepository.updateUser(user){
                state ->
                _updateUser.update {
                    state
                }
            }
        }
    }





}