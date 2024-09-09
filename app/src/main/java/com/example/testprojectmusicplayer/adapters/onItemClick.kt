package com.example.testprojectmusicplayer.adapters

import com.example.testprojectmusicplayer.utils.AlbumArtist
import com.example.testprojectmusicplayer.utils.MediaItem

interface OnItemClickListener {
    fun onItemClick(item: MediaItem)
}


interface OnItemClickListener1{
    fun onItemClick(item: AlbumArtist)
}