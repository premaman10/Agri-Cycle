package com.example.agricycle.data.repository

import com.example.agricycle.data.model.WeatherData
import com.example.agricycle.data.service.WeatherService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRepository {
    private val weatherService: WeatherService by lazy {
        Retrofit.Builder()
            .baseUrl(WeatherService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)
    }

    fun getWeatherData(latitude: Double, longitude: Double): Flow<WeatherData> = flow {
        try {
            val response = weatherService.getWeatherData(
                latitude = latitude,
                longitude = longitude
            )
            emit(response)
        } catch (e: Exception) {
            throw e
        }
    }
}