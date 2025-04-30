package com.example.agricycle.data.network

import com.example.agricycle.data.service.OpenWeatherService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OpenWeatherConfig {
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    private const val API_KEY = "5e50931e4c398a2ec9345ac62f06ee26"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val openWeatherService: OpenWeatherService = retrofit.create(OpenWeatherService::class.java)

    fun getApiKey(): String = API_KEY
}