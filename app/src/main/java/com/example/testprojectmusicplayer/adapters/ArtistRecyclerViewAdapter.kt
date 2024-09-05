package com.example.testprojectmusicplayer.adapters

import android.provider.MediaStore.Audio.Artists
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.databinding.ArtistCardBinding
import com.example.testprojectmusicplayer.databinding.CardWithCorneredImageDescriptionBinding
import com.example.testprojectmusicplayer.databinding.RecentPlayCardBinding
import com.example.testprojectmusicplayer.model.Album
import com.example.testprojectmusicplayer.model.Artist

class ArtistRecyclerViewAdapter(
    private val onItemClick: (Int,Artist)->Unit

):RecyclerView.Adapter<ArtistRecyclerViewAdapter.ViewHolder>() {

    private var list:List<Artist> = emptyList()



    inner class ViewHolder(val binding: ArtistCardBinding):RecyclerView.ViewHolder(binding.root){

        fun bind(item:Artist) {
            binding.artistName.text = item.name
            Glide.with(binding.artistImage.context)
                .load(item.imageUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.default_image) // Replace with your default image resource
                        .error(R.drawable.default_image) // Shown when there is an error loading the image
                )
                .into(binding.artistImage)
            binding.artistItem.setOnClickListener {
                onItemClick.invoke(adapterPosition,item)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = ArtistCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(itemView)

    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)

    }
    fun updateList(newList: List<Artist>) {
        list = newList
        notifyDataSetChanged()
    }
}