package com.example.testprojectmusicplayer.utils

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.testprojectmusicplayer.R

class ForeGround:Service() {

    private val notificationChannel = "MediaService"
    private val serviceId = 1
    private lateinit var notification: Notification
    private lateinit var notificationManager: NotificationManager
    val handle:Handler = Handler(Looper.getMainLooper())

    var count = 0


    override fun onCreate() {
        super.onCreate()
        createChannel()
        createNotification()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(serviceId,notification)
//        val toastRunnable = object :Runnable{
//            override fun run() {
//                Toast.makeText(this@ForeGround,"$count",Toast.LENGTH_LONG).show()
//                count++
//                handle.postDelayed(this,1000)
//            }
//        }
        //handle.postDelayed(toastRunnable,1000)



        return  START_STICKY
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(notificationChannel,"ForeGround",NotificationManager.IMPORTANCE_HIGH)
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }



    @SuppressLint("RemoteViewLayout")
    fun createNotification() {
        val remoteView = RemoteViews(packageName,R.layout.notification_layout)
        remoteView.setTextViewText(R.id.notification_title, "Tile")
        remoteView.setTextViewText(R.id.notification_description,"Description")



        val build = NotificationCompat.Builder(this,notificationChannel)
            .setSmallIcon(R.drawable.default_image)
            .setContentTitle("Title")
            .setContentText("Description")
         notification = build.build()
    }


    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, ForeGround::class.java).apply { this.action = action }
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }









    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        const val ACTION_PLAY = "com.example.testprojectmusicplayer.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.testprojectmusicplayer.ACTION_PAUSE"
        const val ACTION_NEXT = "com.example.testprojectmusicplayer.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.example.testprojectmusicplayer.ACTION_PREVIOUS"
    }
}