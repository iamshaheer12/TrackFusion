package com.example.testprojectmusicplayer.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController

import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.databinding.FragmentSignUpBinding
import com.example.testprojectmusicplayer.model.User
import com.example.testprojectmusicplayer.utils.UiStates
import com.example.testprojectmusicplayer.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpFragment : Fragment() {
   private lateinit var binding: FragmentSignUpBinding
    private val authViewModel: AuthViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    private fun onClick() {
        binding.suButton.setOnClickListener {

            authViewModel.createAccountWithEmailPassword(email = binding.suEmail.text.toString(), password = binding.suPassword.text.toString(), user = User(
                name = binding.suUserName.text.toString(), dob = binding.suDobName.text.toString(), gender = binding.suGender.toString(), email = binding.suEmail.text.toString()))

        }

        binding.arrowBack.setOnClickListener {
            findNavController().popBackStack()
        }

    }

    private  fun observers(){
        lifecycleScope.launch {
            // Use repeatOnLifecycle with the desired state
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Place the code that you want to execute when the lifecycle is in the STARTED state
                // For example, collecting from a Flow or handling UI updates
                authViewModel.authState.collect { uiState ->
                    // Handle the collected UI state
                    when (uiState) {
                        is UiStates.Success -> {
                            // Handle success
                            Toast.makeText(context, uiState.data, Toast.LENGTH_SHORT).show()
                        }
                        is UiStates.Failure -> {
                            // Handle failure
                            Toast.makeText(context, uiState.error, Toast.LENGTH_SHORT).show()
                        }
                        is UiStates.Loading -> {
                            // Handle loading state
                        }
                        else -> {
                            // Handle any other state
                        }
                    }
                }
            }
        }


    }
    }


