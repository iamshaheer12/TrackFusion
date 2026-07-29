package com.example.testprojectmusicplayer.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.example.testprojectmusicplayer.adapters.AddSongAdapter
import com.example.testprojectmusicplayer.databinding.FragmentAddSongBinding
import com.example.testprojectmusicplayer.utils.UiStates
import com.example.testprojectmusicplayer.utils.UserObject
import com.example.testprojectmusicplayer.viewModel.AddSongViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddSongFragment : Fragment() {

    private lateinit var binding: FragmentAddSongBinding
    private val args: AddSongFragmentArgs by navArgs()
    private val viewModel: AddSongViewModel by viewModels()

    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var userObject: UserObject

    private lateinit var adapter: AddSongAdapter

    private var songId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddSongBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        songId = args.song
        if (userObject.getUser() != null) {
            val user = userObject.getUser()
            viewModel.getAllAlbum(userId = user?.userId ?: "")

        }

        onClick()
        settingAdapter()
        observers()
    }


    private fun settingAdapter() {
        binding.addSongRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        adapter = AddSongAdapter(onItemClick = { position, album ->
            // Handle item click event here
        }, glide = glide)

        binding.addSongRecyclerview.adapter = adapter

    }

    private fun onClick() {
        binding.arrowBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.createPlaylistCancelBtn.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.createPlaylistCreateBtn.setOnClickListener {

            if (adapter.getSelectedAlbumsId().toMutableList().isEmpty()) {
                Log.d("EmtpyList", "12345")
                Toast.makeText(requireContext(), "Pleas Select Album", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("addSongCalled", "12345")

                viewModel.addSongs(
                    songId = songId,
                    albumIds = adapter.getSelectedAlbumsId().toMutableList()
                )

            }
        }
    }

    private fun observers() {
        lifecycleScope.launch {

            viewModel.addSongState.collect { state ->
                when (state) {
                    is UiStates.Loading -> {

                        binding.adProgressBar.visibility = View.VISIBLE

                        // Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()

                    }

                    is UiStates.Success -> {
                        binding.adProgressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()
                       // findNavController().popBackStack()
                        adapter.clearSelections()

                    }

                    is UiStates.Failure -> {
                        binding.adProgressBar.visibility = View.GONE

                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                        adapter.clearSelections()

                    }

                    is UiStates.Initial -> {

                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.allAlbums.collect { state ->
                when (state) {
                    is UiStates.Loading -> {


                        binding.adCenterProgressBar.visibility = View.VISIBLE

                       // Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()

                    }

                    is UiStates.Success -> {
                        binding.adCenterProgressBar.visibility = View.GONE

                        val albumList = state.data.toMutableList()
//                        Toast.makeText(
//                            requireContext(),
//                            state.data.toString(),
//                            Toast.LENGTH_SHORT
//                        ).show()
                        adapter.updateList(albumList)


                    }

                    is UiStates.Failure -> {
                        binding.adCenterProgressBar.visibility = View.GONE

                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()

                    }

                    is UiStates.Initial -> {

                    }
                }
            }
        }
    }
}