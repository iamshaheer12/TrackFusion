package com.example.testprojectmusicplayer

import android.content.ComponentName
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.testprojectmusicplayer.utils.PlaybackService
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.testprojectmusicplayer", appContext.packageName)
    }

    @Test
    fun mediaControllerConnectsToPlaybackService() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        try {
            val controller = controllerFuture.get(5, TimeUnit.SECONDS)
            assertEquals(sessionToken.packageName, controller.connectedToken?.packageName)
        } finally {
            controllerFuture.cancel(false)
        }
    }
}
