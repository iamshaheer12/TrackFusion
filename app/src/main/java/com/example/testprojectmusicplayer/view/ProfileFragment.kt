package com.example.testprojectmusicplayer.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.databinding.FragmentProfileBinding
import com.example.testprojectmusicplayer.model.User
import com.example.testprojectmusicplayer.utils.UiStates
import com.example.testprojectmusicplayer.utils.UserObject
import com.example.testprojectmusicplayer.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    @Inject
    lateinit var userObject: UserObject

    @Inject
    lateinit var glide: RequestManager
    private lateinit var binding: FragmentProfileBinding


    private val viewModel: UserViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentProfileBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = userObject.getUser()
        if (user != null) {
            initUi(user)
        }
        observer()
        onClick()

    }

    private fun observer() {
        lifecycleScope.launch {
            viewModel.signOutState.collect { state ->
                when (state) {
                    is UiStates.Success -> {
                        // Get the NavController for the startingFragment's NavHostFragment
                        val startingNavController =
                            (requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController

                        // Pop the back stack and navigate to startingFragment
                        startingNavController.popBackStack(R.id.startingFragment, true)
                        startingNavController.navigate(R.id.startingFragment)


                    }

                    is UiStates.Loading -> {


                    }

                    is UiStates.Failure -> {

                    }

                    is UiStates.Initial -> {

                    }
                }
            }
        }
    }


    private fun onClick() {
        binding.profileArrowBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.profileLogout.setOnClickListener {

            viewModel.singOut()

        }

        binding.prEditBtn.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment2_to_editProfileFragment)
        }

    }


    private fun initUi(user: User) {


        binding.profileName.text = user.name
        binding.prName.text = user.name
        binding.prEmail.text = user.email

        if (user.imageUrl.isNotEmpty()) {
            glide
                .load(user.imageUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.default_image) // Replace with your default image resource
                        .error(R.drawable.default_image) // Shown when there is an error loading the image
                )
                .into(binding.circleImageView)
        } else {
            binding.circleImageView.setImageResource(R.drawable.edit_profile_image)
        }


    }


}