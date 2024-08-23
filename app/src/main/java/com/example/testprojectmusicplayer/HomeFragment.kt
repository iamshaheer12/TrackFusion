package com.suleiman.material.fragments

import android.os.Bundle
import android.view.*
import android.widget.GridView
import androidx.fragment.app.Fragment
import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.adapters.RecentGridAdapter
import com.example.testprojectmusicplayer.model.RecentPlayCard

class HomeFragment : Fragment() {
    private lateinit var recentPlayGrid: GridView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recentPlayGrid = view.findViewById(R.id.hm_recent_play_grid)
        recentPlayGrid.numColumns = 2

        val adapter = RecentGridAdapter(requireContext(),provideGridData())
        recentPlayGrid.adapter = adapter



    }

    private fun provideGridData(): ArrayList<RecentPlayCard>{
        return arrayListOf(
            RecentPlayCard(title = "Atif Aslam is the best singer", image = R.drawable.atif),
            RecentPlayCard(title = "Another title", image = R.drawable.atif),
            RecentPlayCard(title = "Yet another title", image = R.drawable.atif),
            RecentPlayCard(title = "Yet another title", image = R.drawable.atif)

        )
    }

}
