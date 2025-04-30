package com.example.agricycle.data.service

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherStackService {
    @GET("current")
    suspend fun getCurrentWeather(
        @Query("access_key") accessKey: String = "cb2c14dfe5f4929488fbeee4c25efb3f",
        @Query("query") location: String
    ): WeatherStackResponse
}

data class WeatherStackResponse(
    val current: WeatherStackCurrent,
    val location: WeatherStackLocation
)

data class WeatherStackCurrent(
    val temperature: Int,
    val humidity: Int,
    val wind_speed: Int,
    val precip: Double,
    val weather_descriptions: List<String>,
    val weather_icons: List<String>
)

data class WeatherStackLocation(
    val name: String,
    val country: String,
    val lat: String,
    val lon: String,
    val timezone_id: String
)