package com.example.agricycle.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@Composable
fun WeatherCard(
    temperature: Double,
    condition: String,
    iconUrl: String,
    humidity: Int,
    windSpeed: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$temperature°C",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = condition,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Humidity: $humidity%",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Wind Speed: $windSpeed kph",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (iconUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(iconUrl),
                    contentDescription = condition,
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
