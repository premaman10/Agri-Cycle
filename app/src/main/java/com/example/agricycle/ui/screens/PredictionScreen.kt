package com.example.agricycle.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agricycle.ui.viewmodel.WeatherViewModel
import com.example.agricycle.ui.utils.rememberCurrentLocation
import com.google.android.gms.maps.model.LatLng
import com.example.agricycle.ui.components.VideoBackground
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.SimpleDateFormat
import java.util.*

// Analysis Functions
private fun analyzeCropGrowthTemperature(temperature: Double): String {
    return when {
        temperature in 20.0..30.0 -> "Temperature is ideal for most crops (20-30°C)"
        temperature < 20.0 -> "Temperature is below ideal range. Some cold-resistant crops may grow"
        else -> "Temperature is above ideal range. Consider heat-resistant crops"
    }
}

private fun analyzeSoilTemperature(soilTemp: Double): String {
    return when {
        soilTemp in 18.0..24.0 -> "Soil temperature is ideal for root development (18-24°C)"
        soilTemp < 18.0 -> "Soil temperature is low. Root growth may be slower"
        else -> "Soil temperature is high. Monitor root health"
    }
}

private fun analyzeSoilMoisture(moisture: Double): String {
    return when {
        moisture in 0.20..0.35 -> "Soil moisture is ideal for most crops (20-35%)"
        moisture < 0.20 -> "Soil is dry. Irrigation may be needed"
        else -> "Soil is very moist. Monitor for over-watering"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionScreen(
    navController: NavHostController,
    weatherViewModel: WeatherViewModel = viewModel()
) {
    val currentLocation = rememberCurrentLocation()
    val weatherData by weatherViewModel.weatherData.collectAsState()
    val isLoading by weatherViewModel.isLoading.collectAsState()
    val error by weatherViewModel.error.collectAsState()

    // Video Background URI
    val context = LocalContext.current
    val videoUri = remember(context) {
        Uri.parse("android.resource://${context.packageName}/raw/nature_back2")
    }

    LaunchedEffect(currentLocation) {
        currentLocation.let { location ->
            weatherViewModel.fetchWeatherData(location.latitude, location.longitude)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather Prediction") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            VideoBackground(
                videoUri = videoUri,
                modifier = Modifier.fillMaxSize()
            )

            // Content overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.7f)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else if (error != null) {
                        Text(
                            text = "Error: $error",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else if (weatherData != null) {
                        val weather = weatherData!!

                        // Temperature Forecast
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Temperature Forecast",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Current: ${weather.hourly.temperature_2m[0]}${weather.hourly_units.temperature_2m}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                // Temperature Chart
                                val temperatureEntries = weather.hourly.temperature_2m.take(24)
                                    .mapIndexed { index, value ->
                                        FloatEntry(index.toFloat(), value.toFloat())
                                    }

                                Chart(
                                    chart = lineChart(
                                        lines = listOf(
                                            lineSpec(
                                                lineColor = MaterialTheme.colorScheme.primary,
                                                lineBackgroundShader = null
                                            )
                                        )
                                    ),
                                    model = entryModelOf(temperatureEntries),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    startAxis = rememberStartAxis(
                                        title = weather.hourly_units.temperature_2m,
                                        tickLength = 0.dp
                                    ),
                                    bottomAxis = rememberBottomAxis(
                                        title = "Hours",
                                        tickLength = 0.dp
                                    )
                                )

                                // Crop Growth Analysis
                                Spacer(modifier = Modifier.height(16.dp))
                                val tempCondition =
                                    analyzeCropGrowthTemperature(weather.hourly.temperature_2m[0])
                                Text(
                                    text = tempCondition,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (tempCondition.contains("ideal")) Color(0xFF4CAF50) else Color(
                                        0xFFFF9800
                                    )
                                )

                                // Soil Temperature Analysis
                                Spacer(modifier = Modifier.height(16.dp))
                                val soilTempCondition =
                                    analyzeSoilTemperature(weather.hourly.soil_temperature_0cm[0])
                                Text(
                                    text = soilTempCondition,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (soilTempCondition.contains("ideal")) Color(
                                        0xFF4CAF50
                                    ) else Color(0xFFFF9800)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Soil Temperature Forecast
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Soil Temperature Forecast",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Current: ${weather.hourly.soil_temperature_0cm[0]}${weather.hourly_units.soil_temperature_0cm}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                // Soil Temperature Chart
                                val soilTemperatureEntries =
                                    weather.hourly.soil_temperature_0cm.take(24)
                                        .mapIndexed { index, value ->
                                            FloatEntry(index.toFloat(), value.toFloat())
                                        }

                                Chart(
                                    chart = lineChart(
                                        lines = listOf(
                                            lineSpec(
                                                lineColor = Color(0xFF4CAF50),
                                                lineBackgroundShader = null
                                            )
                                        )
                                    ),
                                    model = entryModelOf(soilTemperatureEntries),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    startAxis = rememberStartAxis(
                                        title = weather.hourly_units.soil_temperature_0cm,
                                        tickLength = 0.dp
                                    ),
                                    bottomAxis = rememberBottomAxis(
                                        title = "Hours",
                                        tickLength = 0.dp
                                    )
                                )

                                // Crop Growth Analysis
                                Spacer(modifier = Modifier.height(16.dp))
                                val tempCondition =
                                    analyzeCropGrowthTemperature(weather.hourly.temperature_2m[0])
                                Text(
                                    text = tempCondition,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (tempCondition.contains("ideal")) Color(0xFF4CAF50) else Color(
                                        0xFFFF9800
                                    )
                                )

                                // Soil Temperature Analysis
                                Spacer(modifier = Modifier.height(16.dp))
                                val soilTempCondition =
                                    analyzeSoilTemperature(weather.hourly.soil_temperature_0cm[0])
                                Text(
                                    text = soilTempCondition,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (soilTempCondition.contains("ideal")) Color(
                                        0xFF4CAF50
                                    ) else Color(0xFFFF9800)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Soil Moisture Forecast
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Soil Moisture Forecast",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Current: ${weather.hourly.soil_moisture_0_to_1cm[0]}${weather.hourly_units.soil_moisture_0_to_1cm}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                // Soil Moisture Chart
                                val soilMoistureEntries =
                                    weather.hourly.soil_moisture_0_to_1cm.take(24)
                                        .mapIndexed { index, value ->
                                            FloatEntry(index.toFloat(), value.toFloat())
                                        }

                                Chart(
                                    chart = lineChart(
                                        lines = listOf(
                                            lineSpec(
                                                lineColor = Color(0xFF00BCD4),
                                                lineBackgroundShader = null
                                            )
                                        )
                                    ),
                                    model = entryModelOf(soilMoistureEntries),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    startAxis = rememberStartAxis(
                                        title = weather.hourly_units.soil_moisture_0_to_1cm,
                                        tickLength = 0.dp
                                    ),
                                    bottomAxis = rememberBottomAxis(
                                        title = "Hours",
                                        tickLength = 0.dp
                                    )
                                )

                                // Soil Moisture Analysis
                                Spacer(modifier = Modifier.height(16.dp))
                                val soilMoistureCondition =
                                    analyzeSoilMoisture(weather.hourly.soil_moisture_0_to_1cm[0])
                                Text(
                                    text = soilMoistureCondition,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (soilMoistureCondition.contains("ideal")) Color(
                                        0xFF4CAF50
                                    ) else Color(0xFFFF9800)
                                )

                                // Crop Growth Analysis
                                Spacer(modifier = Modifier.height(16.dp))
                                val tempCondition =
                                    analyzeCropGrowthTemperature(weather.hourly.temperature_2m[0])
                                Text(
                                    text = tempCondition,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (tempCondition.contains("ideal")) Color(0xFF4CAF50) else Color(
                                        0xFFFF9800
                                    )
                                )

                                // Soil Temperature Analysis
                                Spacer(modifier = Modifier.height(16.dp))
                                val soilTempCondition =
                                    analyzeSoilTemperature(weather.hourly.soil_temperature_0cm[0])
                                Text(
                                    text = soilTempCondition,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (soilTempCondition.contains("ideal")) Color(
                                        0xFF4CAF50
                                    ) else Color(0xFFFF9800)
                                )
                            }
                        }
                    } else if (currentLocation == null) {
                        Text(
                            "Fetching location...",
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                .padding(top = 8.dp)
                        )
                    } else {
                        Text(
                            "No weather data available.",
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}

    