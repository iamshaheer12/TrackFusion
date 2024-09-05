package com.example.testprojectmusicplayer.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.databinding.PlaylistSongItemCardBinding
import com.example.testprojectmusicplayer.model.Song

class PlaylistItemsRecyclerView(
    private val onItemClicked: (Int) -> Unit,
    private val onMoreClicked: (Song, Int) -> Unit
) : RecyclerView.Adapter<PlaylistItemsRecyclerView.AudioViewHolder>() {

    private var selectedPosition = -1
    private var audioFiles: List<Song> = emptyList()

    inner class AudioViewHolder(private val binding: PlaylistSongItemCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Song) {
            binding.psiTitle.text = item.title
            binding.psiDescription.text = item.description
            val context = binding.root.context


            Glide.with(binding.psiImage)
                .load(item.imageUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.default_image) // Replace with your default image resource
                        .error(R.drawable.default_image) // Shown when there is an error loading the image
                )
                .into(binding.psiImage)
            val color = if (adapterPosition == selectedPosition) {
                ContextCompat.getColor(context, R.color.spotify_green) // Replace 'green' with your actual color resource name
            } else {
                ContextCompat.getColor(context, R.color.spotify_white) // Replace 'defaultColor' with your actual color resource name
            }

            // Change the color of the title based on whether this item is selected

                binding.psiTitle.setTextColor(color)


            binding.root.setOnClickListener {
                onItemClicked(adapterPosition)
                updateSelection(adapterPosition) // Update selection when an item is clicked
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

    // Function to update the selection
    private fun updateSelection(newPosition: Int) {
        val previousPosition = selectedPosition
        selectedPosition = newPosition
        notifyItemChanged(previousPosition) // Update the previous item to default color
        notifyItemChanged(newPosition) // Update the newly selected item
    }
     fun updateList(list: List<Song>){
        audioFiles =  list
        notifyDataSetChanged()


    }
}
