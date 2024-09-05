import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.adapters.OnItemClickListener
import com.example.testprojectmusicplayer.databinding.PlaylistSongItemCardBinding
import com.example.testprojectmusicplayer.databinding.SearchArtistItemsBinding
import com.example.testprojectmusicplayer.databinding.SongItemForsearchBinding
import com.example.testprojectmusicplayer.model.Album
import com.example.testprojectmusicplayer.model.Artist
import com.example.testprojectmusicplayer.model.Song

class MediaAdapter(
    private var items: List<MediaItem>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ALBUM = 0
        private const val VIEW_TYPE_ARTIST = 1
        private const val VIEW_TYPE_SONG = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is MediaItem.AlbumItem -> VIEW_TYPE_ALBUM
            is MediaItem.ArtistItem -> VIEW_TYPE_ARTIST
            is MediaItem.SongItem -> VIEW_TYPE_SONG
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
            VIEW_TYPE_SONG -> {
                val binding = SongItemForsearchBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                SongViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is MediaItem.AlbumItem -> (holder as AlbumViewHolder).bind(item.album)
            is MediaItem.ArtistItem -> (holder as ArtistViewHolder).bind(item.artist)
            is MediaItem.SongItem -> (holder as SongViewHolder).bind(item.song)

            else -> {}
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<MediaItem>) {
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
            binding.psiTitle.text = album.title
            binding.psiDescription.text = album.description
            Glide.with(binding.psiImage.context)
                .load(album.imageUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.default_image) // Replace with your default image resource
                        .error(R.drawable.default_image) // Shown when there is an error loading the image
                )
                .into(binding.psiImage)


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
            Glide.with(binding.searchArtistImage.context)
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
    inner class SongViewHolder(private val binding: SongItemForsearchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(items[position])
                }
            }
        }

        fun bind(song: Song) {
            binding.searchSongTitle.text = song.title
            binding.searchSongDescription.text = song.description
            Glide.with(binding.searchSongImage.context)
                .load(song.imageUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.default_image) // Replace with your default image resource
                        .error(R.drawable.default_image) // Shown when there is an error loading the image
                )
                .into(binding.searchSongImage)


            // Set other UI elements as needed
        }
    }
}
