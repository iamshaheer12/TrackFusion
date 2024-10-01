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
import com.example.testprojectmusicplayer.databinding.FragmentEmailLoginBinding
import com.example.testprojectmusicplayer.utils.UiStates
import com.example.testprojectmusicplayer.utils.Validator
import com.example.testprojectmusicplayer.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EmailLoginFragment : Fragment() {
    private lateinit var binding: FragmentEmailLoginBinding
    private val authViewModel: AuthViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEmailLoginBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClick()
        observers()
    }

    private fun onClick() {
        binding.elButton.setOnClickListener {
            val email = binding.elEmail.text.toString()

            val emailValidation = Validator.emailValidator(email)
            if (!emailValidation.first) {
                binding.elUserEmailLayout.error = emailValidation.second
                return@setOnClickListener
            } else {
                binding.elUserEmailLayout.error = null
            }


            authViewModel.forgotPasswordLink(email)


        }

        binding.arrowBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.forgotPassword.collect { state ->
                    when (state) {
                        is UiStates.Loading -> {
                            // Show loading indicator
                        }

                        is UiStates.Success -> {
                            // Navigate to the next screen or show success message
                            findNavController().popBackStack()
                            Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()
                        }

                        is UiStates.Failure -> {
                            // Show error message
                            Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                        }

                        is UiStates.Initial -> {

                        }

                    }
                }

            }

        }
    }


}