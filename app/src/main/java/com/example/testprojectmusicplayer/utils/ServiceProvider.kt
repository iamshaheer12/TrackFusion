package com.example.testprojectmusicplayer.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import javax.inject.Inject

class AudioPlaybackServiceProvider(private val context: Context) {

    private var service: AudioPlaybackService? = null
    private var isBound = false
    private var onServiceReady: (() -> Unit)? = null

    fun getService(onReady: (AudioPlaybackService) -> Unit) {
        if (service != null && isBound) {
            onReady(service!!)
        } else {
            // Set the callback to be called once the service is ready
            onServiceReady = {
                if (service != null) {
                    onReady(service!!)
                } else {
                    Log.e("ServiceProvider", "Service is not available.")
                }
            }
            bindService()
        }
    }

    private fun bindService() {
        val intent = Intent(context, AudioPlaybackService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            val localBinder = binder as AudioPlaybackService.LocalBinder
            service = localBinder.getService()
            isBound = true
            onServiceReady?.invoke()
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
