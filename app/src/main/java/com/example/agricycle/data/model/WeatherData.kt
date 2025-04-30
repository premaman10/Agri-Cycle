package com.example.agricycle.data.model

data class WeatherData(
    val latitude: Double,
    val longitude: Double,
    val generationtime_ms: Double,
    val utc_offset_seconds: Int,
    val timezone: String,
    val timezone_abbreviation: String,
    val elevation: Double,
    val hourly_units: HourlyUnits,
    val hourly: HourlyData
)

data class HourlyUnits(
    val time: String,
    val temperature_2m: String,
    val soil_temperature_0cm: String,
    val soil_temperature_6cm: String,
    val soil_temperature_18cm: String,
    val soil_moisture_0_to_1cm: String,
    val soil_moisture_3_to_9cm: String,
    val soil_moisture_1_to_3cm: String
)

data class HourlyData(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val soil_temperature_0cm: List<Double>,
    val soil_temperature_6cm: List<Double>,
    val soil_temperature_18cm: List<Double>,
    val soil_moisture_0_to_1cm: List<Double>,
    val soil_moisture_1_to_3cm: List<Double>,
    val soil_moisture_3_to_9cm: List<Double>,
    val precipitation: List<Double>
)

data class DailyUnits(
    val time: String,
    val temperature_2m_min: String,
    val temperature_2m_max: String,
    val rain_sum: String
)

data class DailyData(
    val time: List<String>,
    val temperature_2m_min: List<Double>,
    val temperature_2m_max: List<Double>,
    val rain_sum: List<Double>
)

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
) 