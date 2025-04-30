package com.example.agricycle.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.agricycle.service.WeatherNotificationService

class NotificationHelper(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "agricycle_notifications",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    }

    fun isNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        if (enabled) {
            WeatherNotificationService.scheduleNotifications(context)
        }
    }

    fun initialize() {
        if (isNotificationsEnabled()) {
            WeatherNotificationService.scheduleNotifications(context)
        }
    }
}