package com.example.agricycle.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.agricycle.ui.components.VideoBackground
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.agricycle.navigation.Screen
import com.example.agricycle.ui.utils.*
import com.example.agricycle.ui.viewmodel.WeatherViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(navController: NavController) {
    val context = LocalContext.current
    val videoUri = remember {
        Uri.parse("android.resource://${context.packageName}/raw/nature_back")
    }
    val weatherViewModel: WeatherViewModel = viewModel()
    val weatherData by weatherViewModel.weatherData.collectAsState()
    val isLoading by weatherViewModel.isLoading.collectAsState()
    val error by weatherViewModel.error.collectAsState()

    // Fetch weather data when the screen is first displayed
    LaunchedEffect(Unit) {
        weatherViewModel.fetchWeatherData(51.5074, -0.1278)
    }

    Scaffold(
        topBar = {
            FadeInAnimation {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            "Weather Analysis",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Video background
            VideoBackground(
                videoUri = videoUri,
                modifier = Modifier.fillMaxSize()
            )
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                weatherData != null -> {
                    val currentWeather = weatherData!!
                    val hourly = currentWeather.hourly
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Air Temperature Analysis
                        SlideInAnimation {
                            AnalysisCard(
                                title = "Air Temperature Analysis",
                                icon = Icons.Default.Thermostat,
                                color = Color(0xFF2196F3)
                            ) {
                                if (hourly.temperature_2m.isNotEmpty()) {
                                    val currentTemp = hourly.temperature_2m[0]
                                    val avgTemp = hourly.temperature_2m.average()
                                    val maxTemp = hourly.temperature_2m.maxOrNull() ?: 0.0
                                    val minTemp = hourly.temperature_2m.minOrNull() ?: 0.0
                                    
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        AnalysisItem(
                                            label = "Current Temperature",
                                            value = "${currentTemp}${currentWeather.hourly_units.temperature_2m}"
                                        )
                                        AnalysisItem(
                                            label = "Average Temperature",
                                            value = "${String.format("%.1f", avgTemp)}${currentWeather.hourly_units.temperature_2m}"
                                        )
                                        AnalysisItem(
                                            label = "Maximum Temperature",
                                            value = "${maxTemp}${currentWeather.hourly_units.temperature_2m}"
                                        )
                                        AnalysisItem(
                                            label = "Minimum Temperature",
                                            value = "${minTemp}${currentWeather.hourly_units.temperature_2m}"
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Soil Temperature Analysis
                        SlideInAnimation {
                            AnalysisCard(
                                title = "Soil Temperature Analysis",
                                icon = Icons.Default.Grass,
                                color = Color(0xFF4CAF50)
                            ) {
                                if (hourly.soil_temperature_0cm.isNotEmpty()) {
                                    val currentSoilTemp = hourly.soil_temperature_0cm[0]
                                    val avgSoilTemp = hourly.soil_temperature_0cm.average()
                                    val maxSoilTemp = hourly.soil_temperature_0cm.maxOrNull() ?: 0.0
                                    val minSoilTemp = hourly.soil_temperature_0cm.minOrNull() ?: 0.0
                                    
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        AnalysisItem(
                                            label = "Current Soil Temperature",
                                            value = "${currentSoilTemp}${currentWeather.hourly_units.soil_temperature_0cm}"
                                        )
                                        AnalysisItem(
                                            label = "Average Soil Temperature",
                                            value = "${String.format("%.1f", avgSoilTemp)}${currentWeather.hourly_units.soil_temperature_0cm}"
                                        )
                                        AnalysisItem(
                                            label = "Maximum Soil Temperature",
                                            value = "${maxSoilTemp}${currentWeather.hourly_units.soil_temperature_0cm}"
                                        )
                                        AnalysisItem(
                                            label = "Minimum Soil Temperature",
                                            value = "${minSoilTemp}${currentWeather.hourly_units.soil_temperature_0cm}"
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Soil Moisture Analysis
                        SlideInAnimation {
                            AnalysisCard(
                                title = "Soil Moisture Analysis",
                                icon = Icons.Default.WaterDrop,
                                color = Color(0xFF00BCD4)
                            ) {
                                if (hourly.soil_moisture_0_to_1cm.isNotEmpty()) {
                                    val currentMoisture = hourly.soil_moisture_0_to_1cm[0]
                                    val avgMoisture = hourly.soil_moisture_0_to_1cm.average()
                                    val maxMoisture = hourly.soil_moisture_0_to_1cm.maxOrNull() ?: 0.0
                                    val minMoisture = hourly.soil_moisture_0_to_1cm.minOrNull() ?: 0.0
                                    
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        AnalysisItem(
                                            label = "Current Soil Moisture",
                                            value = "${currentMoisture}${currentWeather.hourly_units.soil_moisture_0_to_1cm}"
                                        )
                                        AnalysisItem(
                                            label = "Average Soil Moisture",
                                            value = "${String.format("%.1f", avgMoisture)}${currentWeather.hourly_units.soil_moisture_0_to_1cm}"
                                        )
                                        AnalysisItem(
                                            label = "Maximum Soil Moisture",
                                            value = "${maxMoisture}${currentWeather.hourly_units.soil_moisture_0_to_1cm}"
                                        )
                                        AnalysisItem(
                                            label = "Minimum Soil Moisture",
                                            value = "${minMoisture}${currentWeather.hourly_units.soil_moisture_0_to_1cm}"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalysisCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = color
                )
            }
            content()
        }
    }
}

@Composable
fun AnalysisItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}