package com.example.agricycle.ui.state

import com.example.agricycle.data.model.WeatherData

sealed class WeatherState {
    object Initial : WeatherState()
    object Loading : WeatherState()
    data class Success(val weather: WeatherData) : WeatherState()
    data class Error(val message: String) : WeatherState()
}