package com.example.agricycle.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agricycle.AgricycleApplication
import com.example.agricycle.data.model.WeatherData
import com.example.agricycle.data.service.WeatherService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val _weatherData = MutableStateFlow<WeatherData?>(null)
    val weatherData: StateFlow<WeatherData?> = _weatherData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun setError(message: String) {
        _error.value = message
    }

    private val weatherService: WeatherService by lazy {
        val okHttpClient = okhttp3.OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        Retrofit.Builder()
            .baseUrl(WeatherService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getApplication<AgricycleApplication>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun fetchWeatherData(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                if (!isNetworkAvailable()) {
                    _error.value = "No internet connection available"
                    return@launch
                }
                
                val response = weatherService.getWeatherData(
                    latitude = latitude,
                    longitude = longitude
                )
                
                _weatherData.value = response
                _error.value = null
            } catch (e: Exception) {
                _error.value = when (e) {
                    is java.net.UnknownHostException -> "Unable to resolve host. Please check your internet connection."
                    is java.net.SocketTimeoutException -> "Connection timed out. Please check your network connection."
                    else -> "Error fetching weather data: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun retryFetchWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            var retryCount = 0
            val maxRetries = 3
            var delayMs = 1000L

            while (retryCount < maxRetries) {
                if (isNetworkAvailable()) {
                    try {
                        fetchWeatherData(latitude, longitude)
                        return@launch
                    } catch (e: IOException) {
                        retryCount++
                        if (retryCount >= maxRetries) {
                            _error.value = "Failed to connect after $maxRetries attempts. Please check your internet connection."
                            break
                        }
                        delay(delayMs)
                        delayMs *= 2 // Exponential backoff
                    }
                } else {
                    _error.value = "No internet connection available. Please check your network settings."
                    break
                }
            }
        }
    }
}