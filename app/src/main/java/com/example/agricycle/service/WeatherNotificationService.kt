package com.example.agricycle.service

import android.Manifest // Added for POST_NOTIFICATIONS
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager // Added for permission check
import android.os.Build
import androidx.core.app.ActivityCompat // Added for permission check
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.agricycle.MainActivity
import com.example.agricycle.R
import com.example.agricycle.data.model.WeatherData
import com.example.agricycle.ui.viewmodel.WeatherViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.random.Random // Added for random notification IDs

class WeatherNotificationService(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val CHANNEL_ID = "weather_notification_channel"
        // Removed const NOTIFICATION_ID = 1
        private const val WORK_NAME = "weather_notification_work"
        const val PERMISSION_REQUEST_CODE = 123 // Added for notification permission

        // Function to check for notification permission
        fun hasNotificationPermission(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } 
            return true // Permissions are not needed for older versions
        }

        fun scheduleNotifications(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<WeatherNotificationService>(3, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
        }
        
        // Made public for access from MainActivity
        suspend fun getCurrentWeatherData(context: Context): WeatherData? {
            // Consider dependency injection instead of direct ViewModel instantiation here
            // This might cause issues if the ViewModel relies on Activity/Fragment lifecycle
            val weatherViewModel = WeatherViewModel(context.applicationContext as android.app.Application)
            // Fetching location needs to be handled before calling this or passed in
            // For now, assuming ViewModel handles location internally or has cached data
            // This might need refinement based on how WeatherViewModel gets location.
            // Let's assume fetchWeatherData needs to be called first.
            // This static method might not be the best approach.
            // Returning null for now as direct fetch isn't feasible without location.
            // TODO: Refactor weather fetching logic for background use
            // return weatherViewModel.weatherData.first() 
             return null // Placeholder - Requires proper background data fetching strategy
        }
        
        // Updated to accept a unique notificationId
        fun showNotification(context: Context, title: String, message: String, notificationId: Int) {
            if (!hasNotificationPermission(context)) {
                // Optionally, log or handle the case where permission is missing
                android.util.Log.w("WeatherNotificationService", "Notification permission not granted.")
                return
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            createNotificationChannelIfNeeded(context, notificationManager) // Ensure channel exists
            
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager.notify(notificationId, notification) // Use the provided ID
        }

        // Moved channel creation to a reusable function
        private fun createNotificationChannelIfNeeded(context: Context, notificationManager: NotificationManager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                    val channel = NotificationChannel(
                        CHANNEL_ID,
                        "Weather Notifications",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "Channel for weather condition notifications"
                    }
                    notificationManager.createNotificationChannel(channel)
                }
            }
        }

        // Made analysis functions public static
        fun analyzeCropGrowthTemperature(temperature: Double): String {
            return when {
                temperature in 20.0..30.0 -> "Air Temp: Ideal (20-30°C)"
                temperature < 20.0 -> "Air Temp: Low (<20°C). Consider cold-resistant crops."
                else -> "Air Temp: High (>30°C). Consider heat-resistant crops."
            }
        }

        fun analyzeSoilTemperature(soilTemp: Double): String {
            return when {
                soilTemp in 18.0..24.0 -> "Soil Temp: Ideal (18-24°C)"
                soilTemp < 18.0 -> "Soil Temp: Low (<18°C). Root growth may slow."
                else -> "Soil Temp: High (>24°C). Monitor root health."
            }
        }

        fun analyzeSoilMoisture(moisture: Double): String {
            // Assuming moisture is a percentage or fraction. Adjust thresholds if needed.
            // The original check used 0.20..0.35, implying a fraction. Let's keep that.
             return when {
                moisture in 0.20..0.35 -> "Soil Moisture: Ideal (20-35%)"
                moisture < 0.20 -> "Soil Moisture: Low (<20%). Irrigation may be needed."
                else -> "Soil Moisture: High (>35%). Monitor for over-watering."
            }
        }
    }

    // Keep the worker's notificationManager instance if needed for its own logic
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Ensure channel exists for the worker too
            createNotificationChannelIfNeeded(applicationContext, notificationManager)

            // TODO: Implement proper background location fetching here
            // The current WeatherViewModel approach might not work reliably in a Worker.
            // For now, the worker might fail to get data.
            val weatherData = getCurrentWeatherData(applicationContext) // Using the static method (needs fixing)

            if (weatherData != null && weatherData.hourly.temperature_2m.isNotEmpty()) { // Added null/empty check
                val temperature = weatherData.hourly.temperature_2m[0]
                val soilTemperature = weatherData.hourly.soil_temperature_0cm[0]
                val soilMoisture = weatherData.hourly.soil_moisture_0_to_1cm[0]

                // Analyze conditions using static methods
                val tempAnalysis = analyzeCropGrowthTemperature(temperature)
                val soilTempAnalysis = analyzeSoilTemperature(soilTemperature)
                val moistureAnalysis = analyzeSoilMoisture(soilMoisture)

                // Use unique IDs for worker notifications to avoid conflicts
                val baseWorkerId = 1000 // Base ID for worker notifications

                // Send notifications for non-ideal conditions
                if (!tempAnalysis.contains("Ideal")) {
                    showNotification(applicationContext, "Periodic Weather Alert", tempAnalysis, baseWorkerId + 1)
                }
                if (!soilTempAnalysis.contains("Ideal")) {
                    showNotification(applicationContext, "Periodic Soil Temp Alert", soilTempAnalysis, baseWorkerId + 2)
                }
                if (!moistureAnalysis.contains("Ideal")) {
                    showNotification(applicationContext, "Periodic Soil Moisture Alert", moistureAnalysis, baseWorkerId + 3)
                }
            } else {
                 android.util.Log.w("WeatherNotificationWorker", "Failed to get weather data for periodic check.")
            }

            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("WeatherNotificationWorker", "Error in periodic work", e)
            Result.failure()
        }
    }

    // Removed duplicate private analysis functions
    // Removed duplicate private showNotification
    // Removed duplicate private createNotificationChannel
}