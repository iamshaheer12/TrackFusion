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
import androidx.navigation.fragment.navArgs
import com.example.testprojectmusicplayer.databinding.FragmentCreatePlaylistBinding
import com.example.testprojectmusicplayer.model.Album
import com.example.testprojectmusicplayer.model.User
import com.example.testprojectmusicplayer.utils.UiStates
import com.example.testprojectmusicplayer.utils.UserObject
import com.example.testprojectmusicplayer.viewModel.CreatePlaylistViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class CreatePlaylistFragment : Fragment() {
    private lateinit var binding: FragmentCreatePlaylistBinding
    private val viewModel: CreatePlaylistViewModel by viewModels()
    private val args: CreatePlaylistFragmentArgs by navArgs()

    @Inject
    lateinit var userObject: UserObject

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentCreatePlaylistBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val album = args.playlist

        val user = userObject.getUser()
        initUi(album)

        if (user != null) {
            onClick(user, album = album)
        }
        observers()


    }

    private fun onClick(user: User, album: Album?) {
        binding.createPlaylistCreateBtn.setOnClickListener {
            if (album != null) {
                viewModel.updateAlbum(
                    album = Album(
                        id = album.id,
                        createdBy = user.userId,
                        title = binding.playlistTitle.text.toString(),
                        descriptions = binding.playlistDescription.text.toString(),
                        visibility = true
                    )
                )

            } else {
                viewModel.createAlbum(
                    album = Album(

                        createdBy = user.userId,
                        title = binding.playlistTitle.text.toString(),
                        descriptions = binding.playlistDescription.text.toString(),
                        visibility = true
                    )
                )
            }


        }
        binding.createPlaylistCancelBtn.setOnClickListener {
            findNavController().popBackStack()

        }

    }

    private fun observers() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.createAlbum.collect { state ->
                    when (state) {
                        is UiStates.Loading -> {
                            binding.createPlaylistProgressBar.progressBar.visibility = View.VISIBLE
                          //  Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()

                        }

                        is UiStates.Success -> {

                            binding.createPlaylistProgressBar.progressBar.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                "Created Successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            findNavController().popBackStack()


                        }

                        is UiStates.Failure -> {
                            binding.createPlaylistProgressBar.progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()


                        }

                        is UiStates.Initial -> {

                        }


                    }
                }


            }


        }
      lifecycleScope.launch {
            viewModel.updateAlbum.collect { state ->
                when (state) {
                    is UiStates.Loading -> {
                        binding.createPlaylistProgressBar.progressBar.visibility = View.VISIBLE
                        //   Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()

                    }

                    is UiStates.Success -> {

                        binding.createPlaylistProgressBar.progressBar.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            "Successfully Updated",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().popBackStack()

                    }

                    is UiStates.Failure -> {
                        binding.createPlaylistProgressBar.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()

                    }

                    is UiStates.Initial -> {

                    }
                }
            }
        }

    }

    private fun initUi(album: Album?) {
        if (album != null) {
            binding.createPlaylistCreateBtn.text = "Update"
            binding.playlistTitle.setText(album.title)
            binding.playlistDescription.setText(album.descriptions)
            binding.createPlaylistTitle.text = "Update Playlist"
        }


    }


}