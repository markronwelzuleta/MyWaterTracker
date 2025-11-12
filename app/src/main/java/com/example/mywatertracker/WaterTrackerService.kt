package com.example.mywatertracker

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import kotlin.math.max

class WaterTrackerService : Service() {

    companion object {
        private const val CHANNEL_ID = "WaterTrackerChannel"
        private const val NOTIFICATION_ID = 1
        private const val EXTRA_ADD_WATER = "ADD_WATER"

        // Helper to easily start or update the service
        fun getServiceIntent(context: Context, addedWater: Double = 0.0): Intent {
            return Intent(context, WaterTrackerService::class.java).apply {
                putExtra(EXTRA_ADD_WATER, addedWater)
            }
        }
    }

    // Variables
    private var waterLevel = 2500.0 // ml, start balanced
    private val updateInterval = 5000L // every 5 seconds
    private val waterLossRate = 0.144 // ml lost per interval

    private val handler = Handler(Looper.getMainLooper())

    // Periodic task that decreases water level
    private val updateTask = object : Runnable {
        override fun run() {
            waterLevel = max(0.0, waterLevel - waterLossRate)
            updateNotification()
            handler.postDelayed(this, updateInterval)

        }
    }


    

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val addedWater = intent?.getDoubleExtra(EXTRA_ADD_WATER, 0.0) ?: 0.0
        if (addedWater > 0) {
            waterLevel += addedWater
            updateNotification()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(updateTask)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // --- Notification-related functions ---

    private fun buildNotification(): Notification {
        val contentText = "Water level: %.1f ml".format(waterLevel)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Hydration Tracker")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Water Tracker Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
