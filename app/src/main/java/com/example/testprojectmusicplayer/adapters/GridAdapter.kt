package com.example.testprojectmusicplayer.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.RequestManager
import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.model.Album
import javax.inject.Inject

class RecentGridAdapter @Inject constructor(
    context: Context,
    private var recentPlayList: List<Album>, // Made var for mutability
    private val glide: RequestManager // Injecting Glide's RequestManager
) : ArrayAdapter<Album>(context, 0, recentPlayList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listViewItem = convertView ?: LayoutInflater.from(context).inflate(R.layout.recent_play_grid_card, parent, false)

        val recentPlayCard: Album? = getItem(position)
        val titleTextView = listViewItem.findViewById<TextView>(R.id.rpgc_text)
        val imageView = listViewItem.findViewById<ImageView>(R.id.rpgc_image)

        titleTextView.text = recentPlayCard?.title ?: ""

        // Use Glide to load the image from the imageUrl
        recentPlayCard?.imageUrl?.let { imageUrl ->
            glide.load(imageUrl)
                .placeholder(R.drawable.default_image) // Optional placeholder
                .error(R.drawable.default_image) // Optional error image
                .into(imageView)
        } ?: run {
            imageView.setImageResource(R.drawable.default_image) // Fallback image
        }

        Log.d("RecentGridAdapter", "Item at position $position: ${recentPlayCard?.title}")

        return listViewItem
    }

    override fun getCount(): Int {
        return recentPlayList.size
    }

    // Method to update the list and notify changes
    fun updateList(newList: List<Album>) {
        recentPlayList = newList
        notifyDataSetChanged() // Notify the adapter to refresh the views
    }
}
