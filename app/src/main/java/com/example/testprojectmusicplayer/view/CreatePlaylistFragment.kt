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
import com.example.testprojectmusicplayer.databinding.FragmentCreatePlaylistBinding
import com.example.testprojectmusicplayer.model.Album
import com.example.testprojectmusicplayer.utils.UiStates
import com.example.testprojectmusicplayer.viewModel.CreatePlaylistViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class CreatePlaylistFragment : Fragment() {
    private lateinit var binding:FragmentCreatePlaylistBinding
    private val viewModel: CreatePlaylistViewModel by viewModels()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentCreatePlaylistBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClick()
        observers()


    }

    private fun onClick() {
        binding.createPlaylistCreateBtn.setOnClickListener {
            viewModel.updateAlbum(album = Album(
                createdBy = "1243556",
                title = binding.playlistTitle.text.toString(),
                descriptions = binding.playlistDescription.text.toString()
            ))

        }
        binding.createPlaylistCancelBtn.setOnClickListener {

        }
    }

    private fun observers(){

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.updateAlbum.collect{
                    state->
                    when(state){
                        is UiStates.Loading -> {
                            Toast.makeText(requireContext(),"Loading",Toast.LENGTH_SHORT).show()


                        }
                        is UiStates.Success -> {


                            Toast.makeText(requireContext(),"Created Successfully",Toast.LENGTH_SHORT).show()


                        }
                        is UiStates.Failure -> {
                            Toast.makeText(requireContext(),state.error,Toast.LENGTH_SHORT).show()


                        }
                    }
                }

            }
        }

    }


}