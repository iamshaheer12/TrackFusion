package com.example.testprojectmusicplayer.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners() // Set up text listeners for real-time validation
        onClick()
        observers()
    }

    private fun setupListeners() {
        // Real-time validation for email input
        binding.lgEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Clear the error when the user starts typing
                binding.userEmailLayout.error = null
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Real-time validation for password input
        binding.lgPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Clear the error when the user starts typing
                binding.passwordLayout.error = null
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun onClick() {
        binding.siButton.setOnClickListener {
            val email = binding.lgEmail.text.toString().trim()
            val password = binding.lgPassword.text.toString().trim()

            // Validate email and password
            if (!isInputValid(email, password)) return@setOnClickListener

            // Trigger login action
            authViewModel.loginWithEmailPassword(email, password)
        }

        // Navigate to passwordless login screen
        binding.logWithoutPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_emailLoginFragment)
        }

        // Handle back navigation
        binding.arrowBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun isInputValid(email: String, password: String): Boolean {
        var isValid = true

        // Validate email
        val emailValidation = Validator.emailValidator(email)
        if (!emailValidation.first) {
            binding.userEmailLayout.error = emailValidation.second
            isValid = false
        }

        // Validate password
        val passwordValidation = Validator.passwordValidator(password)
        if (!passwordValidation.first) {
            binding.passwordLayout.error = passwordValidation.second
            isValid = false
        }

        return isValid
    }

    private fun observers() {
        lifecycleScope.launch {
            authViewModel.loginState.collect { state ->
                when (state) {
                    is UiStates.Loading -> {
                        binding.lgProgressBar.progressBar.visibility = View.VISIBLE
                        binding.siButton.text = ""
                    }
                    is UiStates.Success -> {
                        binding.lgProgressBar.progressBar.visibility = View.GONE
                        binding.siButton.text = "LOG IN"
                        Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()

                        findNavController().popBackStack(R.id.mainFragment, true)
                        findNavController().navigate(R.id.mainFragment)
                    }
                    is UiStates.Failure -> {
                        binding.lgProgressBar.progressBar.visibility = View.GONE
                        binding.siButton.text = "LOG IN"
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // Handle other states if needed
                    }
                }
            }
        }
    }
}
