package com.example.testprojectmusicplayer.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.testprojectmusicplayer.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MusicPlayerBottomSheet : BottomSheetDialogFragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this bottom sheet
        return inflater.inflate(R.layout.musci_player_screen, container, false)
    }
}
