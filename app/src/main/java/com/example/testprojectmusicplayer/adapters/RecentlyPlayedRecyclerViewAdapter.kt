package com.example.testprojectmusicplayer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.databinding.RecentPlayCardBinding
import com.example.testprojectmusicplayer.model.Album
import javax.inject.Inject

class RecentlyPlayedRecyclerViewAdapter @Inject constructor(
    private val onItemClicked:(Int, Album) ->Unit,
    private val glide : RequestManager
):RecyclerView.Adapter<RecentlyPlayedRecyclerViewAdapter.ViewHolder>() {




    private var list: List<Album> = arrayListOf()

    inner class ViewHolder(
        private val binding: RecentPlayCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Album) {
            glide
                //.with(binding.rpCardImage.context)
                .load(item.imageUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.default_image) // Replace with your default image resource
                        .error(R.drawable.default_image) // Shown when there is an error loading the image
                )
                .into(binding.rpCardImage)
            binding.rpPlaylistTitle.text = item.title

            binding.rpItem.setOnClickListener {
                onItemClicked.invoke(adapterPosition, item)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = RecentPlayCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    // Method to update the list and notify the adapter
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<Album>) {
        list = newList
        notifyDataSetChanged()
    }
}