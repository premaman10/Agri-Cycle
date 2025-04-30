package com.example.agricycle.data.network

import com.example.agricycle.data.service.WeatherStackService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeatherStackConfig {
    private const val BASE_URL = "http://api.weatherstack.com/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val weatherStackService: WeatherStackService = retrofit.create(WeatherStackService::class.java)
}