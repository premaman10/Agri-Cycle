package com.example.agricycle.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.agricycle.data.model.WeatherData

@Composable
fun WeatherInfo(
    weatherData: WeatherData?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Current Weather",
                style = MaterialTheme.typography.titleLarge
            )
            
            if (weatherData != null) {
                val hourly = weatherData.hourly
                if (hourly.temperature_2m.isNotEmpty() && 
                    hourly.soil_temperature_0cm.isNotEmpty() && 
                    hourly.soil_moisture_0_to_1cm.isNotEmpty()) {
                    Text(
                        text = "Air Temperature: ${hourly.temperature_2m[0]}${weatherData.hourly_units.temperature_2m}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Surface Soil Temperature: ${hourly.soil_temperature_0cm[0]}${weatherData.hourly_units.soil_temperature_0cm}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Surface Soil Moisture: ${hourly.soil_moisture_0_to_1cm[0]}${weatherData.hourly_units.soil_moisture_0_to_1cm}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Text(
                        text = "Weather data not available",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Text(
                    text = "No weather data available",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun WeatherDetail(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}