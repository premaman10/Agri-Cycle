package com.example.agricycle

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels // Added for viewModels delegate
import androidx.compose.foundation.layout.fillMaxSize
import android.content.Context // Added for getSystemService
import android.app.NotificationChannel // Added for welcome notification
import android.app.NotificationManager // Added for welcome notification
import androidx.core.app.NotificationCompat // Added for welcome notification
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat // Added for permission check
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.agricycle.data.LoginRepository
import com.example.agricycle.service.WeatherNotificationService
import com.example.agricycle.ui.theme.AgricycleTheme
import com.example.agricycle.ui.viewmodel.WeatherViewModel // Import WeatherViewModel
import com.example.agricycle.viewmodel.LoginViewModel
import kotlinx.coroutines.delay // Added for delay
import kotlinx.coroutines.launch
import kotlin.random.Random // Added for random delay and ID
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {

    private val weatherViewModel: WeatherViewModel by viewModels() // Use shared ViewModel
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    // Constants for Welcome Notification
    private val WELCOME_CHANNEL_ID = "welcome_notification_channel"
    private val WELCOME_NOTIFICATION_ID = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize permission launcher
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. You can now schedule notifications or trigger immediate ones.
                scheduleBackgroundWeatherChecks()
                // triggerInitialNotifications() // Removed call
                // Also show welcome notification if permission granted here
                createWelcomeNotificationChannel()
                showWelcomeNotification()
                android.util.Log.i("MainActivity", "Notification permission granted.")
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied.
                android.util.Log.w("MainActivity", "Notification permission denied.")
                // Optionally show a snackbar or dialog explaining why the permission is needed
            }
        }

        // Check and request notification permission if needed (Android 13+)
        checkAndRequestNotificationPermission()

        // Schedule background checks (only if permission granted or not needed)
        if (WeatherNotificationService.hasNotificationPermission(this)) {
            scheduleBackgroundWeatherChecks()
        }

        setContent {
            AgricycleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pass the shared WeatherViewModel to MainContent
                    MainContent(weatherViewModel = weatherViewModel)
                }
            }
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // You can use the API that requires the permission.
                    android.util.Log.i("MainActivity", "Notification permission already granted.")
                    // Trigger initial notifications immediately if permission is already granted - Removed
                    // triggerInitialNotifications()
                    // Also show welcome notification
                    createWelcomeNotificationChannel()
                    showWelcomeNotification()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // In an educational UI, explain to the user why your app requires this
                    // permission for a specific feature to behave as expected, and what
                    // features are disabled if it's declined. In this UI, include a
                    // "cancel" or "no thanks" button that lets the user continue
                    // using your app without granting the permission.
                    android.util.Log.w("MainActivity", "Showing notification permission rationale.")
                    // Show rationale if needed, then request
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Directly ask for the permission
                    android.util.Log.i("MainActivity", "Requesting notification permission.")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
         else {
             // No runtime permission needed for older versions
             // triggerInitialNotifications() // Removed call
             // Also show welcome notification
             createWelcomeNotificationChannel()
             showWelcomeNotification()
         }
    }

    private fun scheduleBackgroundWeatherChecks() {
        // Schedule background weather checks (can be refined later)
        WeatherNotificationService.scheduleNotifications(this)
        android.util.Log.i("MainActivity", "Scheduled background weather checks.")
    }

    // --- Welcome Notification Logic ---

    private fun createWelcomeNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Welcome Notifications"
            val descriptionText = "Shown when the app is opened"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(WELCOME_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            android.util.Log.i("MainActivity", "Welcome notification channel created.")
        }
    }

    private fun showWelcomeNotification() {
        // Double-check permission just in case
        if (!WeatherNotificationService.hasNotificationPermission(this)) {
             android.util.Log.w("MainActivity", "Cannot show welcome notification - permission not granted")
             return
        }

        val builder = NotificationCompat.Builder(this, WELCOME_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Replace with your actual icon
            .setContentTitle("Welcome to Agricycle!")
            .setContentText("App is ready.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true) // Dismiss notification when tapped

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(WELCOME_NOTIFICATION_ID, builder.build())
        android.util.Log.i("MainActivity", "Welcome notification shown.")
    }

    // --- End Welcome Notification Logic ---
}

@Composable
fun MainContent(weatherViewModel: WeatherViewModel) {
    val navController = rememberNavController()
    // Use a simpler way to get LoginViewModel if it doesn't need complex setup
    val context = LocalContext.current
    val loginRepository = remember { LoginRepository(context) }
    val loginViewModel = remember { LoginViewModel(loginRepository) }

    // Observe weather data here if needed for UI elements, 
    // but the notification logic is now handled in the Activity's lifecycleScope.
    // val weatherData by weatherViewModel.weatherData.collectAsState()
    // LaunchedEffect(weatherData) { ... } // Initial notification logic moved to Activity

    NavGraph(navController = navController, loginViewModel = loginViewModel, weatherViewModel = weatherViewModel)
}