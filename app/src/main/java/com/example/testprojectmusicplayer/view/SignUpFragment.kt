package com.example.testprojectmusicplayer.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
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
        onClick()
        observers()
        setAdapterForAutoCompleteText()
    }


    private fun onClick() {
        binding.suButton.setOnClickListener {
            val email = binding.suEmail.text.toString()
            val password = binding.suPassword.text.toString()
            val name = binding.suUserName.text.toString()
            val dob = binding.suDobName.text.toString()
            val gender = binding.suGender.text.toString()

            Log.d("GenderTes",binding.suGender.text.toString(),)


            // Validate email
            val emailValidation = Validator.emailValidator(email)
            if (!emailValidation.first) {
                binding.suEmailLayout.error = emailValidation.second
                return@setOnClickListener
            } else {
                binding.suEmailLayout.error = null
            }

            // Validate password
            val passwordValidation = Validator.passwordValidator(password)
            if (!passwordValidation.first) {
                binding.suUserPasswordLayout.error = passwordValidation.second
                return@setOnClickListener
            } else {
                binding.suUserPasswordLayout.error = null
            }

            // Validate name
            if (name.isEmpty()) {
                binding.suUserNameLayout.error = "Field is Empty"
                return@setOnClickListener
            } else {
                binding.suUserNameLayout.error = null
            }

            // Validate date of birth
            if (dob.isEmpty()) {
                binding.suUserDobLayout.error = "Field is Empty"
                return@setOnClickListener
            } else {
                binding.suUserDobLayout.error = null
            }
            if (gender.isEmpty()) {
                binding.suGenderLayout.error = "Please select gender"
                return@setOnClickListener
            } else {
                binding.suGenderLayout.error = null
            }

            // Proceed with account creation
            authViewModel.createAccountWithEmailPassword(
                email = email,
                password = password,
                user = User(
                    name = name,
                    dob = dob,
                    gender = binding.suGender.text.toString(),
                    email = email,

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
                            findNavController().popBackStack(R.id.mainFragment,true)

                            findNavController().navigate(R.id.mainFragment)                        }
                        is UiStates.Failure -> {
                            // Handle failure
                            Toast.makeText(context, uiState.error, Toast.LENGTH_SHORT).show()
                        }
                        is UiStates.Loading -> {
                            // Handle loading state
                        }

                    }
                }
            }
        }


    }
    private fun showDatePicker() {
        // Create an instance of the calendar
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create and show the DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Update the UI with the selected date
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.suDobName.text = Editable.Factory.getInstance().newEditable(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }


    private fun setAdapterForAutoCompleteText(){
        val genderArray = resources.getStringArray(R.array.gender)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, genderArray)
        binding.suGender.setAdapter(adapter)

    }

    }


