package com.example.testprojectmusicplayer.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.adapters.GenresAdapterForSearch
import com.example.testprojectmusicplayer.adapters.RecentGridAdapter
import com.example.testprojectmusicplayer.databinding.FragmentSearchBinding
import com.example.testprojectmusicplayer.model.RecentPlayCard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment() {
   private lateinit var binding: FragmentSearchBinding
    private lateinit var categoryGrid: GridView



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingAdapter()

        onClick()


    }

    private fun onClick(){
        binding.searchBar.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_searchScreen2Fragment)
        }
    }


    private fun settingAdapter(){
        categoryGrid =binding.searchCategoryItems
        categoryGrid.numColumns = 2

        val adapter = GenresAdapterForSearch(requireContext(), provideGridData())
        categoryGrid.adapter = adapter
    }

    private fun provideGridData(): ArrayList<RecentPlayCard>{
        return arrayListOf(
            RecentPlayCard(title = "Pop", image = R.drawable.atif),
            RecentPlayCard(title = "Bollywood", image = R.drawable.atif),
            RecentPlayCard(title = "Sad", image = R.drawable.atif),
            RecentPlayCard(title = "Classical", image = R.drawable.atif)

        )
    }
}