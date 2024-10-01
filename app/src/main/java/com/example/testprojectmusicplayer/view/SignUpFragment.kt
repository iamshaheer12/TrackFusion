package com.example.testprojectmusicplayer.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
import com.example.testprojectmusicplayer.utils.Validator
import com.example.testprojectmusicplayer.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

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
        setUpListeners()  // Set up real-time validation
        onClick()
        setAdapterForAutoCompleteText()
        observeViewModel()
    }

    // Function to handle button clicks
    private fun onClick() {
        binding.suButton.setOnClickListener {
            val email = binding.suEmail.text.toString()
            val password = binding.suPassword.text.toString()
            val name = binding.suUserName.text.toString()
            val dob = binding.suDobName.text.toString()
            val gender = binding.suGender.text.toString()

            Log.d("GenderTest", gender)

            // Validate fields before proceeding
            if (!validateFields(name, email, password, dob, gender)) {
                return@setOnClickListener
            }

            // Proceed with account creation
            authViewModel.createAccountWithEmailPassword(
                email = email,
                password = password,
                user = User(
                    name = name,
                    dob = dob,
                    gender = gender,
                    email = email
                )
            )
        }

        binding.arrowBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.suDobName.setOnClickListener {
            showDatePicker()
        }
    }

    // Real-time validation using TextWatcher
    private fun setUpListeners() {
        binding.suUserName.addTextChangedListener(createTextWatcher { binding.suUserNameLayout.error = null })
        binding.suEmail.addTextChangedListener(createTextWatcher { binding.suEmailLayout.error = null })
        binding.suPassword.addTextChangedListener(createTextWatcher { binding.suUserPasswordLayout.error = null })
    }

    private fun createTextWatcher(onTextChanged: () -> Unit): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onTextChanged()
            }
        }
    }

    // Validation function for all fields
    private fun validateFields(name: String, email: String, password: String, dob: String, gender: String): Boolean {
        var isValid = true

        // Validate email
        val emailValidation = Validator.emailValidator(email)
        if (!emailValidation.first) {
            binding.suEmailLayout.error = emailValidation.second
            isValid = false
        }

        // Validate password
        val passwordValidation = Validator.passwordValidator(password)
        if (!passwordValidation.first) {
            binding.suUserPasswordLayout.error = passwordValidation.second
            isValid = false
        }

        // Validate name
        if (name.isEmpty()) {
            binding.suUserNameLayout.error = "Field is Empty"
            isValid = false
        }

        // Validate date of birth
        if (dob.isEmpty()) {
            binding.suUserDobLayout.error = "Field is Empty"
            isValid = false
        }

        // Validate gender
        if (gender.isEmpty()) {
            binding.suGenderLayout.error = "Please select gender"
            isValid = false
        }

        return isValid
    }

    // Observe ViewModel state changes
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.authState.collect { uiState ->
                    when (uiState) {
                        is UiStates.Success -> {
                            binding.suProgressBar.progressBar.visibility = View.GONE
                            binding.suButton.text = "CREATE"
                            Toast.makeText(context, uiState.data, Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.mainFragment)
                        }
                        is UiStates.Failure -> {
                            binding.suProgressBar.progressBar.visibility = View.GONE
                            binding.suButton.text = "CREATE"
                            Toast.makeText(context, uiState.error, Toast.LENGTH_SHORT).show()
                        }
                        is UiStates.Loading -> {
                            binding.suProgressBar.progressBar.visibility = View.VISIBLE
                            binding.suButton.text = ""
                        }
                        is UiStates.Initial -> {}
                    }
                }
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.SpotifyDatePickerTheme,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.suDobName.text = Editable.Factory.getInstance().newEditable(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun setAdapterForAutoCompleteText() {
        val genderArray = resources.getStringArray(R.array.gender)
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            genderArray
        )
        binding.suGender.setAdapter(adapter)
    }
}
