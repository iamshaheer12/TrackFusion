package com.example.testprojectmusicplayer.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.databinding.PlaylistSongItemCardBinding
import com.example.testprojectmusicplayer.databinding.SearchArtistItemsBinding
import com.example.testprojectmusicplayer.model.Album
import com.example.testprojectmusicplayer.model.Artist
import com.example.testprojectmusicplayer.utils.AlbumArtist
import javax.inject.Inject

class LibraryRecyclerViewAdapter @Inject constructor(
    private var items: List<AlbumArtist>,
    private val itemClickListener: OnItemClickListener1,
    private val glide : RequestManager,

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ALBUM = 0
        private const val VIEW_TYPE_ARTIST = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is AlbumArtist.AlbumItem -> VIEW_TYPE_ALBUM
            is AlbumArtist.ArtistItem -> VIEW_TYPE_ARTIST

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ALBUM -> {
                val binding = PlaylistSongItemCardBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                AlbumViewHolder(binding)
            }
            VIEW_TYPE_ARTIST -> {
                val binding = SearchArtistItemsBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ArtistViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is AlbumArtist.AlbumItem -> (holder as AlbumViewHolder).bind(item.album)
            is AlbumArtist.ArtistItem -> (holder as ArtistViewHolder).bind(item.artist)

            else -> {}
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<AlbumArtist>) {
        items = newItems
        notifyDataSetChanged()
    }

    // ViewHolder for Album
    inner class AlbumViewHolder(private val binding: PlaylistSongItemCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(items[position])
                }
            }
        }

        fun bind(album: Album) {
            binding.psiMore.visibility = View.GONE
            binding.adsTitle.text = album.title
            binding.adsDescription.text= album.descriptions
            glide
                .load(album.imageUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.default_image) // Replace with your default image resource
                        .error(R.drawable.default_image) // Shown when there is an error loading the image
                )
                .into(binding.adsImage)


            // Set other UI elements as needed
        }
    }

    // ViewHolder for Artist
    inner class ArtistViewHolder(private val binding: SearchArtistItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(items[position])
                }
            }
        }

        fun bind(artist: Artist) {
            binding.searchArtistName.text = artist.name
            glide
                //.with(binding.searchArtistImage.context)
                .load(artist.imageUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.default_image) // Replace with your default image resource
                        .error(R.drawable.default_image) // Shown when there is an error loading the image
                )
                .into(binding.searchArtistImage)




            // Set other UI elements as needed
        }
    }

    // ViewHolder for Song

}
