package com.example.testprojectmusicplayer.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.model.RecentPlayCard

class RecentGridAdapter(context: Context, private val recentPlayList: List<RecentPlayCard>) :
    ArrayAdapter<RecentPlayCard?>(context, 0, recentPlayList ) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var listViewItem = convertView
        if (listViewItem == null) {
            // Layout Inflater inflates each item to be displayed in GridView.
            listViewItem = LayoutInflater.from(context).inflate(R.layout.recent_play_grid_card, parent, false)
        }

        val recentPlayCard: RecentPlayCard? = getItem(position)
        val courseTV = listViewItem!!.findViewById<TextView>(R.id.rpgc_text)
        val courseIV = listViewItem.findViewById<ImageView>(R.id.rpgc_image)

        courseTV.text = recentPlayCard?.title ?: ""
        courseIV.setImageResource(recentPlayCard?.image ?: R.drawable.atif)
        Log.d("RecentGridAdapter", "Item at position $position: ${recentPlayCard?.title}")

        return listViewItem
    }

    override fun getCount(): Int {
        return recentPlayList.size
    }
}
