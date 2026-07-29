package com.example.testprojectmusicplayer.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import javax.inject.Inject

class AudioPlaybackServiceProvider @Inject constructor(private val context: Context) {

    private var service: AudioPlaybackService? = null
    private var isBound = false
    private var onServiceReady: ((AudioPlaybackService) -> Unit)? = null

    fun getService(onReady: (AudioPlaybackService) -> Unit) {
        if (isBound && service != null) {
            // Service is already bound, immediately return the service
            onReady(service!!)
        } else {
            // Bind and wait for service to be ready
            onServiceReady = { service ->
                onReady(service)
            }
            bindService()
        }
    }

    private fun bindService() {
        if (!isBound) {
            val intent = Intent(context, AudioPlaybackService::class.java)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            val localBinder = binder as AudioPlaybackService.LocalBinder
            service = localBinder.getService()

            isBound = true
            onServiceReady?.invoke(service!!)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            service = null
            isBound = false
        }
    }

    fun unbindService() {
        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
        }
    }
}
