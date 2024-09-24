package com.example.testprojectmusicplayer.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.databinding.PlaylistSongItemCardBinding
import com.example.testprojectmusicplayer.model.Song
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

class PlaylistItemsRecyclerView @Inject constructor(
    private val onItemClicked: (Int) -> Unit,
    private val onMoreClicked: (Song, Int) -> Unit,
    private val glide: RequestManager
) : RecyclerView.Adapter<PlaylistItemsRecyclerView.AudioViewHolder>() {

    private var currentSongId: String? = null  // Store the ID of the current song
    private var audioFiles: List<Song> = emptyList()

    inner class AudioViewHolder(private val binding: PlaylistSongItemCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Song) {
            binding.adsTitle.text = item.title
            binding.adsDescription.text = item.description
            val context = binding.root.context

            glide
                .load(item.imageUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.default_image)
                        .error(R.drawable.default_image)
                )
                .into(binding.adsImage)

            // Change color if the current song's ID matches the item's song ID
            val color = if (item.songId == currentSongId) {
                ContextCompat.getColor(context, R.color.spotify_green)
            } else {
                ContextCompat.getColor(context, R.color.spotify_white)
            }

            binding.adsTitle.setTextColor(color)

            binding.root.setOnClickListener {
                onItemClicked(adapterPosition)
                updateSelection(item.songId)  // Update selection when an item is clicked
            }

            binding.psiMore.setOnClickListener {
                onMoreClicked(item, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val binding = PlaylistSongItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AudioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        holder.bind(audioFiles[position])
    }

    override fun getItemCount() = audioFiles.size

    // Function to update the current song ID and refresh the list
    fun updateSelection(newSongId: String) {
        val previousSongId = currentSongId
        currentSongId = newSongId

        // Find the previous and new song positions and update only those items
        val previousIndex = audioFiles.indexOfFirst { it.songId == previousSongId }
        val newIndex = audioFiles.indexOfFirst { it.songId == newSongId }

        if (previousIndex != -1) notifyItemChanged(previousIndex)  // Update the previous song item
        if (newIndex != -1) notifyItemChanged(newIndex)  // Update the new song item
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(list: List<Song>) {
        audioFiles = list
        notifyDataSetChanged()  // Notify that the entire dataset has changed
    }
}
