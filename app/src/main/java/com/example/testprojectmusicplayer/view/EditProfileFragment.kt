package com.example.testprojectmusicplayer.view

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions

import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.databinding.FragmentEditProfileBinding
import com.example.testprojectmusicplayer.model.User
import com.example.testprojectmusicplayer.utils.UiStates
import com.example.testprojectmusicplayer.utils.UserObject
import com.example.testprojectmusicplayer.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EditProfileFragment : Fragment() {

    private lateinit var binding: FragmentEditProfileBinding

    private val viewModel : UserViewModel by viewModels()

    @Inject
    lateinit var glide : RequestManager

    @Inject
    lateinit var userObject: UserObject

    private var imageUrl = ""


    private var userData: User? = null




    private val pickMedia = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {

            binding.circleImageView.setImageURI(uri)
           viewModel.uploadUserImage(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentEditProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        userData = userObject.getUser()


        userData?.let { initUi(it) }



        onClick()
        observer()

    }

    private fun onClick(){
        binding.circleImageView.setOnClickListener {
            pickMedia.launch("image/*")
        }


        binding.editProfileArrowBack.setOnClickListener {
            findNavController().popBackStack()


            Log.d("Click on ArrowBack", "Click")
        }

        binding.edProfileUpdate.setOnClickListener {


            if (binding.prName.text.isNotEmpty()){

                Log.d("Click on Update User", "Click")

                viewModel.updateUser(
                    user = User(
                        name = binding.prName.text.toString(),
                        dob = userData?.dob?:"",
                        gender = userData?.gender?:"",
                        email = userData?.email?:"",
                        userId = userData?.userId?:"",
                        imageUrl = imageUrl,
                        likedSong = userData?.likedSong?: emptyList(),
                        likedAlbums = userData?.likedAlbums?:"",
                        likedArtist = userData?.likedArtist?: emptyList(),
                        recentlyPlayedSongs = userData?.recentlyPlayedSongs?: emptyList()



                    )

                )
            }

        }
    }


    private fun observer(){

        lifecycleScope.launch {
           viewModel.uploadUserImage.collect{
               state ->
               when(state){
                   is UiStates.Loading -> {

                   }
                   is UiStates.Success -> {
                       imageUrl = state.data

                       Toast.makeText(requireContext(),"Uploaded Successfully",Toast.LENGTH_SHORT).show()

                   }
                   is UiStates.Failure -> {

                   }
               }
           }
        }
        lifecycleScope.launch {
            viewModel.updateUser.collect{
                    state ->
                when(state){
                    is UiStates.Loading -> {

                    }
                    is UiStates.Success -> {
                        Toast.makeText(requireContext(), "Update Successfully", Toast.LENGTH_SHORT).show()
                      findNavController().popBackStack()
                    }
                    is UiStates.Failure -> {


                    }
                }
            }
        }





    }



    private fun initUi(user: User){


        binding.profileName.text = user.name
        binding.prName.setText(user.name)


        if (user.imageUrl.isNotEmpty()){
            glide
                .load(user.imageUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.default_image) // Replace with your default image resource
                        .error(R.drawable.default_image) // Shown when there is an error loading the image
                )
                .into(binding.circleImageView)
        }
        else
        {
            binding.circleImageView.setImageResource(R.drawable.edit_profile_image)
        }



    }




}