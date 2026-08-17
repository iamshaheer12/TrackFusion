package com.example.testprojectmusicplayer.utils

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import javax.inject.Inject

class MediaControllerProvider @Inject constructor(private val context: Context) {

    private var controller: MediaController? = null
    private var building = false
    private val pendingActions = mutableListOf<(MediaController) -> Unit>()

    fun getController(onReady: (MediaController) -> Unit) {
        val current = controller
        if (current != null) {
            onReady(current)
            return
        }
        pendingActions.add(onReady)
        if (building) return
        building = true

        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        future.addListener(
            {
                val built = try {
                    future.get()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to connect to PlaybackService", e)
                    null
                }
                val actions = pendingActions.toList()
                pendingActions.clear()
                building = false
                if (built != null) {
                    controller = built
                    actions.forEach { it(built) }
                } else {
                    Log.e(TAG, "MediaController connection failed")
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    fun isConnected(): Boolean = controller != null

    fun releaseController() {
        val current = controller ?: return
        controller = null
        current.release()
    }

    private companion object {
        const val TAG = "MediaControllerProvider"
    }
}
