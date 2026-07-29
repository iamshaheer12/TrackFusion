package com.example.testprojectmusicplayer.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.testprojectmusicplayer.R
import com.example.testprojectmusicplayer.databinding.FragmentSplashScreenBinding
import com.example.testprojectmusicplayer.utils.UserObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashScreenFragment : Fragment() {

    private lateinit var binding: FragmentSplashScreenBinding
    private lateinit var job: Job

    @Inject
    lateinit var userObject: UserObject

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSplashScreenBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        job = GlobalScope.launch(Dispatchers.Main) {
            delay(2000)


            if (userObject.getUser() != null) {
               // findNavController().popBackStack(R.id.splashScreenFragment, true) // Ensure splash is removed
                findNavController().navigate(R.id.action_splashScreenFragment_to_mainFragment)
            } else {
                //findNavController().popBackStack(R.id.splashScreenFragment, true) // Ensure splash is removed
                findNavController().navigate(R.id.action_splashScreenFragment_to_startingFragment)
            }


            // Adjust the delay as needed

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job.cancel()
    }


}