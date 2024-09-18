package com.example.testprojectmusicplayer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.databinding.ItemCardWithCheckBoxBinding
import com.example.testprojectmusicplayer.model.Album
import javax.inject.Inject

class AddSongAdapter @Inject constructor(
    private val onItemClick: (Int, Album) -> Unit,
    private val glide: RequestManager
) : RecyclerView.Adapter<AddSongAdapter.ViewHolder>() {

    private val selectedAlbums = mutableSetOf<String>() // To track selected items
    private var albumList: List<Album> = listOf() // To store the list of albums

    inner class ViewHolder(val binding: ItemCardWithCheckBoxBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Album) {
            binding.adsTitle.text = item.title
            binding.adsDescription.text = item.descriptions
            glide
                .load(item.imageUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.default_image) // Replace with your default image resource
                        .error(R.drawable.default_image) // Shown when there is an error loading the image
                )
                .into(binding.adsImage)

            // Set the checkbox state based on whether the item is selected
            binding.adsCheckbox.isChecked = selectedAlbums.contains(item.id)

            // Handle item click and checkbox toggle
            binding.root.setOnClickListener {
                toggleSelection(item)
                onItemClick.invoke(adapterPosition, item)
            }

            binding.adsCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedAlbums.add(item.id)
                } else {
                    selectedAlbums.remove(item.id)
                }
            }
        }
    }

    // Function to toggle the selection of an item
    private fun toggleSelection(item: Album) {
        if (selectedAlbums.contains(item.id)) {
            selectedAlbums.remove(item.id)
        } else {
            selectedAlbums.add(item.id)
        }
        notifyItemChanged(albumList.indexOf(item)) // Update the item in the RecyclerView
    }

    // Create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCardWithCheckBoxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // Replace the contents of a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(albumList[position])
    }

    override fun getItemCount(): Int = albumList.size

    // Update the adapter's list
    fun updateList(newList: List<Album>) {
        albumList = newList
        notifyDataSetChanged()
    }

    // Function to get selected items
    fun getSelectedAlbumsId(): Set<String> {
        return selectedAlbums
    }
}
