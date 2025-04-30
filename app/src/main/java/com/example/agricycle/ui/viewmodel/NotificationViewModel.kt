package com.example.agricycle.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.agricycle.ui.screens.Notification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationViewModel : ViewModel() {
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    init {
        // Add welcome notifications when the ViewModel is created
        addWelcomeNotifications()
    }

    private fun addWelcomeNotifications() {
        val welcomeNotifications = listOf(
            Notification(
                "Welcome to AgriCycle!",
                "Thank you for choosing AgriCycle for your smart farming needs.",
                true
            ),
            Notification(
                "Getting Started",
                "Monitor weather conditions and receive personalized crop recommendations.",
                true
            ),
            Notification(
                "Weather Analysis",
                "We'll analyze weather conditions to help optimize your farming decisions.",
                true
            )
        )
        _notifications.value = welcomeNotifications
    }

    fun addWeatherNotification(temperature: Double, soilTemp: Double, soilMoisture: Double) {
        val notifications = mutableListOf<Notification>()

        // Temperature Analysis
        when {
            temperature in 20.0..30.0 -> notifications.add(
                Notification(
                    "Ideal Temperature Conditions",
                    "Current temperature is optimal for most crops (20-30°C)",
                    true
                )
            )
            temperature < 20.0 -> notifications.add(
                Notification(
                    "Low Temperature Alert",
                    "Temperature is below ideal range. Consider cold-resistant crops",
                    true
                )
            )
            else -> notifications.add(
                Notification(
                    "High Temperature Alert",
                    "Temperature is above ideal range. Monitor heat-resistant crops",
                    true
                )
            )
        }

        // Soil Temperature Analysis
        when {
            soilTemp in 18.0..24.0 -> notifications.add(
                Notification(
                    "Optimal Soil Temperature",
                    "Soil temperature is ideal for root development (18-24°C)",
                    true
                )
            )
            soilTemp < 18.0 -> notifications.add(
                Notification(
                    "Low Soil Temperature",
                    "Soil temperature is low. Root growth may be slower",
                    true
                )
            )
            else -> notifications.add(
                Notification(
                    "High Soil Temperature",
                    "Soil temperature is high. Monitor root health",
                    true
                )
            )
        }

        // Soil Moisture Analysis
        when {
            soilMoisture in 0.20..0.35 -> notifications.add(
                Notification(
                    "Ideal Soil Moisture",
                    "Soil moisture is optimal for most crops (20-35%)",
                    true
                )
            )
            soilMoisture < 0.20 -> notifications.add(
                Notification(
                    "Low Soil Moisture",
                    "Irrigation needed: Soil moisture is below optimal level",
                    true
                )
            )
            else -> notifications.add(
                Notification(
                    "High Soil Moisture",
                    "Warning: Soil moisture is high. Check drainage",
                    true
                )
            )
        }

        _notifications.value = notifications + _notifications.value
    }

    fun markNotificationAsRead(notification: Notification) {
        _notifications.value = _notifications.value.map { 
            if (it == notification) it.copy(isUnread = false) else it 
        }
    }
}