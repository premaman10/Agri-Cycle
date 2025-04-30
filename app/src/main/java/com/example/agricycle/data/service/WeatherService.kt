package com.example.agricycle.data.service

import com.example.agricycle.data.model.WeatherData
import com.example.agricycle.data.model.LocationData
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("forecast")
    suspend fun getWeatherData(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourly: String = "temperature_2m,soil_temperature_0cm,soil_temperature_6cm,soil_temperature_18cm,soil_moisture_0_to_1cm,soil_moisture_3_to_9cm,soil_moisture_1_to_3cm",
        @Query("timezone") timezone: String = "auto"
    ): WeatherData

    companion object {
        const val BASE_URL = "https://api.open-meteo.com/v1/"
        
        fun handleNetworkError(e: Exception): String {
            return when (e) {
                is java.net.UnknownHostException -> "Unable to resolve host. Please check your internet connection."
                is java.net.ConnectException -> "Could not connect to the server. Please try again later."
                is java.net.SocketTimeoutException -> "Connection timed out. Please check your network connection."
                else -> "An error occurred: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }
}

data class WeatherResponse(
    val current: Current,
    val current_units: CurrentUnits
)

data class Current(
    val temperature_2m: Double,
    val relative_humidity_2m: Int,
    val precipitation: Double,
    val wind_speed_10m: Double,
    val weather_code: Int
)

data class CurrentUnits(
    val temperature_2m: String,
    val relative_humidity_2m: String,
    val precipitation: String,
    val wind_speed_10m: String,
    val weather_code: String
)