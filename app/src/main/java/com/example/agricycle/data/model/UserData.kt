package com.example.agricycle.data.model

data class UserData(
    val userId: String = "",
    val fieldData: List<FieldData> = emptyList(),
    val weatherPreferences: WeatherPreferences = WeatherPreferences(),
    val cropHistory: List<CropHistory> = emptyList()
) {
    data class FieldData(
        val fieldId: String = "",
        val name: String = "",
        val location: String = "",
        val size: Double = 0.0,
        val soilType: String = "",
        val lastUpdated: Long = System.currentTimeMillis()
    )

    data class WeatherPreferences(
        val temperatureUnit: String = "Celsius",
        val windSpeedUnit: String = "km/h",
        val precipitationUnit: String = "mm"
    )

    data class CropHistory(
        val cropId: String = "",
        val cropName: String = "",
        val plantingDate: Long = 0,
        val harvestDate: Long? = null,
        val yield: Double? = null,
        val fieldId: String = ""
    )
}