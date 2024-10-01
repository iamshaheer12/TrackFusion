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
import com.example.testprojectmusicplayer.databinding.FragmentSplashScreenBinding
import com.example.testprojectmusicplayer.databinding.FragmentStartingBinding
import com.example.testprojectmusicplayer.utils.UiStates
import com.example.testprojectmusicplayer.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StartingFragment : Fragment() {
    private lateinit var binding: FragmentStartingBinding
    private val authViewModel: AuthViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStartingBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClick()
        observer()
    }

    private fun onClick() {
        binding.stSignUpBtn.setOnClickListener {
            findNavController().navigate(R.id.action_startingFragment_to_signUpFragment)
        }
        binding.stSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_startingFragment_to_loginFragment)
        }

        binding.stGoogleSignBtn.setOnClickListener {
            authViewModel.signWithGoogle(R.string.default_web_client_id.toString())
        }

    }


    private fun observer() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.googleLoginState.collect { uiState ->
                    // Handle the collected UI state
                    when (uiState) {
                        is UiStates.Success -> {
                            // Handle success
                            Toast.makeText(context, uiState.data, Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_startingFragment_to_mainFragment)
                        }

                        is UiStates.Failure -> {
                            // Handle failure
                            Toast.makeText(context, uiState.error, Toast.LENGTH_SHORT).show()
                        }

                        is UiStates.Loading -> {
                            // Handle loading state
                        }

                        is UiStates.Initial -> {

                        }

                    }
                }

            }
        }


    }


}