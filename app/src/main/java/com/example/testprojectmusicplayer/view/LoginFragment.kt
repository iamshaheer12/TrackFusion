package com.example.testprojectmusicplayer.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.testprojectmusicplayer.R

import com.example.testprojectmusicplayer.databinding.FragmentLoginBinding
import com.example.testprojectmusicplayer.utils.UiStates
import com.example.testprojectmusicplayer.utils.Validator
import com.example.testprojectmusicplayer.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
   private lateinit var binding: FragmentLoginBinding
    private val authViewModel: AuthViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClick()
        observers()
    }


    private fun onClick() {
        binding.siButton.setOnClickListener {
            // Get the email and password from input fields
            val email = binding.lgEmail.text.toString()
            val password = binding.lgPassword.text.toString()

            // Validate email and password
            // Validate email
            val emailValidation = Validator.emailValidator(email)
            if (!emailValidation.first) {
                binding.userEmailLayout.error = emailValidation.second
                return@setOnClickListener
            } else {
                binding.userEmailLayout.error = null
            }

            // Validate password
            val passwordValidation = Validator.passwordValidator(password)
            if (!passwordValidation.first) {
                binding.passwordLayout.error = passwordValidation.second
                return@setOnClickListener
            } else {
                binding.passwordLayout.error = null
            }

            authViewModel.loginWithEmailPassword(email,password)
                // Show the error message to the user
            }


        binding.logWithoutPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_emailLoginFragment)
        }

        binding.arrowBack.setOnClickListener {
            findNavController().popBackStack()
        }




    }

    private fun observers() {
        lifecycleScope.launch {
            authViewModel.loginState.collect { state ->
                when (state) {
                    is UiStates.Loading -> {
                        // Show loading indicator
                    }
                    is UiStates.Success -> {
                        // Navigate to the next screen or show success message
                        Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()
                    }
                    is UiStates.Failure -> {
                        // Show error message
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // Handle idle state if needed
                    }
                }
            }
        }

    }



}