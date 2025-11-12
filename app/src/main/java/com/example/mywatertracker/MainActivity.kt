package com.example.mywatertracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mywatertracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Flag to track if the service has been started
    private var serviceStarted = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startWaterServiceIfNeeded()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkNotificationPermission()

        binding.btnAddWater.setOnClickListener {
            addWater(250.0) // Add 250ml per button press
        }
    }

    private fun checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startWaterServiceIfNeeded()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Start the foreground service only once
    private fun startWaterServiceIfNeeded() {
        if (!serviceStarted) {
            val intent = WaterTrackerService.getServiceIntent(this)
            ContextCompat.startForegroundService(this, intent)
            serviceStarted = true
        }
    }

    private fun addWater(amount: Double) {
        val intent = WaterTrackerService.getServiceIntent(this, amount)
        startService(intent) // Add water to the already running service
    }
}
