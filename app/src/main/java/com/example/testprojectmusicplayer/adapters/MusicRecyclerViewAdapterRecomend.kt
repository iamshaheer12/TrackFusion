package com.example.testprojectmusicplayer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.databinding.PlaylistWithTitleDescriptionCardBinding
import com.example.testprojectmusicplayer.model.Album
import com.example.testprojectmusicplayer.model.Song
import com.example.testprojectmusicplayer.utils.MusicItem



class MusicRecyclerViewAdapter(
    private val onItemClicked: (MusicItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var itemList: List<MusicItem> = arrayListOf()

    companion object {
        private const val TYPE_ALBUM = 0
        private const val TYPE_SONG = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (itemList[position]) {
            is MusicItem.AlbumItem -> TYPE_ALBUM
            is MusicItem.SongItem -> TYPE_SONG
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_ALBUM -> {
                val binding =PlaylistWithTitleDescriptionCardBinding .inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                AlbumViewHolder(binding)
            }

            TYPE_SONG -> {
                val binding = PlaylistWithTitleDescriptionCardBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                SongViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = itemList[position]) {
            is MusicItem.AlbumItem -> (holder as AlbumViewHolder).bind(item.album)
            is MusicItem.SongItem -> (holder as SongViewHolder).bind(item.song)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<MusicItem>) {
        itemList = newList
        notifyDataSetChanged()
    }

    // ViewHolder for Album
    inner class AlbumViewHolder(
        private val binding: PlaylistWithTitleDescriptionCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(album: Album) {
            // Load album data, including image and description
            Glide.with(binding.rCardImage.context)
                .load(album.imageUrl)
                .placeholder(R.drawable.default_image)
                .error(R.drawable.default_image)
                .into(binding.rCardImage)

            binding.rPlaylistTitle.text = album.title
            binding.rPlaylistDescription.text = album.description

            binding.root.setOnClickListener {
                onItemClicked.invoke(MusicItem.AlbumItem(album))
            }
        }
    }

    // ViewHolder for Song
    inner class SongViewHolder(
        private val binding: PlaylistWithTitleDescriptionCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song) {
            binding.rPlaylistTitle.text = song.title
            binding.rPlaylistDescription.text = song.description

            binding.root.setOnClickListener {
                onItemClicked.invoke(MusicItem.SongItem(song))
            }
        }
    }
}
