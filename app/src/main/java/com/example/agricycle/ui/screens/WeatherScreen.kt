package com.example.agricycle.ui.screens

import android.Manifest
import android.content.Context
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.agricycle.data.location.LocationManager
import com.example.agricycle.ui.components.WeatherInfo
import com.example.agricycle.ui.viewmodel.WeatherViewModel
import com.example.agricycle.util.RequestLocationPermission
import com.example.agricycle.util.hasLocationPermission
import com.example.agricycle.data.model.WeatherData
import com.example.agricycle.data.model.HourlyData
import kotlinx.coroutines.launch
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    navController: NavHostController,
    weatherViewModel: WeatherViewModel // Accept ViewModel as parameter
) {
    val context = LocalContext.current
    // Use the passed-in ViewModel instance
    // val weatherViewModel: WeatherViewModel = viewModel()
    val weatherData by weatherViewModel.weatherData.collectAsState()
    val isLoading by weatherViewModel.isLoading.collectAsState()
    val error by weatherViewModel.error.collectAsState()
    
    var showPermissionDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val locationManager = remember { LocationManager(context) }

    // Fetch weather data when the screen is first displayed OR if data is null
    LaunchedEffect(weatherData) { // Re-trigger if weatherData becomes null
        android.util.Log.i("WeatherScreen_Debug", "[LaunchedEffect(weatherData)] Entered. Current data: ${weatherData != null}")
        if (weatherData == null) { // Only fetch if data isn't already loaded by MainActivity
            if (context.hasLocationPermission()) {
                android.util.Log.i("WeatherScreen_Debug", "[LaunchedEffect] Permissions granted. Checking location services...")
                val locationEnabled = try {
                    val locationMode = android.provider.Settings.Secure.getInt(context.contentResolver, android.provider.Settings.Secure.LOCATION_MODE)
                    android.util.Log.d("WeatherScreen_Debug", "[LaunchedEffect] Location mode: $locationMode")
                    locationMode != android.provider.Settings.Secure.LOCATION_MODE_OFF
                } catch (e: Exception) {
                    android.util.Log.e("WeatherScreen_Debug", "[LaunchedEffect] Error checking location services state", e)
                    false
                }
                
                if (!locationEnabled) {
                    android.util.Log.w("WeatherScreen_Debug", "[LaunchedEffect] Location services disabled")
                    weatherViewModel.setError("Location services are disabled. Please enable location services in system settings to get weather data.")
                    return@LaunchedEffect
                }
                
                scope.launch {
                    try {
                        android.util.Log.i("WeatherScreen_Debug", "[LaunchedEffect] Calling locationManager.getCurrentLocation()...")
                        val currentLocation = locationManager.getCurrentLocation()
                        if (currentLocation != null) {
                            android.util.Log.i("WeatherScreen_Debug", "[LaunchedEffect] Got location: Lat=${currentLocation.latitude}, Lon=${currentLocation.longitude}. Calling fetchWeatherData...")
                            weatherViewModel.fetchWeatherData(
                                currentLocation.latitude,
                                currentLocation.longitude
                            )
                            android.util.Log.d("WeatherScreen", "Fetched weather for initial current location: Lat=${currentLocation.latitude}, Lon=${currentLocation.longitude}")
                        } else {
                            android.util.Log.w("WeatherScreen_Debug", "[LaunchedEffect] locationManager.getCurrentLocation() returned null. Setting error.")
                            weatherViewModel.setError("Unable to get current location. Please ensure you have a clear view of the sky and try again.")
                        }
                    } catch (e: SecurityException) {
                        android.util.Log.e("WeatherScreen_Debug", "[LaunchedEffect] Security exception during location fetch", e)
                        weatherViewModel.setError("Location permission was revoked. Please grant location permission in system settings.")
                        showPermissionDialog = true
                    } catch (e: Exception) {
                        android.util.Log.e("WeatherScreen_Debug", "[LaunchedEffect] Exception during location fetch: ${e.message}", e)
                        weatherViewModel.setError("Error getting location. Please check your device's location settings and try again.")
                    }
                }
            } else {
                android.util.Log.i("WeatherScreen_Debug", "[LaunchedEffect] Permissions NOT granted. Showing permission dialog.")
                showPermissionDialog = true
            }
        }
    }

    if (showPermissionDialog) {
        RequestLocationPermission(
            onPermissionGranted = {
                android.util.Log.i("WeatherScreen_Debug", "[RequestLocationPermission] onPermissionGranted triggered.") // New Log
                showPermissionDialog = false
                scope.launch {
                    android.util.Log.i("WeatherScreen_Debug", "[onPermissionGranted] Calling locationManager.getCurrentLocation()...") // New Log
                    val currentLocation = locationManager.getCurrentLocation()
                    if (currentLocation != null) {
                        android.util.Log.i("WeatherScreen_Debug", "[onPermissionGranted] Got location: Lat=${currentLocation.latitude}, Lon=${currentLocation.longitude}. Calling fetchWeatherData...") // New Log
                        weatherViewModel.fetchWeatherData(
                            currentLocation.latitude,
                            currentLocation.longitude
                        )
                        android.util.Log.d("WeatherScreen", "Fetched weather for initial current location: Lat=${currentLocation.latitude}, Lon=${currentLocation.longitude}")
                    } else {
                         android.util.Log.w("WeatherScreen_Debug", "[onPermissionGranted] locationManager.getCurrentLocation() returned null after permission grant.") // New Log
                    }
                }
            },
            onPermissionDenied = {
                android.util.Log.w("WeatherScreen_Debug", "[RequestLocationPermission] onPermissionDenied triggered.") // New Log
                showPermissionDialog = false
                android.util.Log.w("WeatherScreen", "Location permission denied by user.")
                weatherViewModel.setError("Location permission denied. Weather data requires location access.")
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Weather") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            android.util.Log.i("WeatherScreen_Debug", "[Refresh Button] Clicked.") // New Log
                            if (!context.hasLocationPermission()) {
                                android.util.Log.w("WeatherScreen_Debug", "[Refresh Button] No location permission")
                                showPermissionDialog = true
                                return@IconButton
                            }

                            val locationEnabled = try {
                                android.provider.Settings.Secure.getInt(context.contentResolver, android.provider.Settings.Secure.LOCATION_MODE) != android.provider.Settings.Secure.LOCATION_MODE_OFF
                            } catch (e: Exception) {
                                android.util.Log.e("WeatherScreen_Debug", "[Refresh Button] Error checking location services state", e)
                                false
                            }
                            
                            if (!locationEnabled) {
                                android.util.Log.w("WeatherScreen_Debug", "[Refresh Button] Location services disabled")
                                weatherViewModel.setError("Location services are disabled. Please enable location services in system settings to get weather data.")
                                return@IconButton
                            }

                            scope.launch {
                                try {
                                    android.util.Log.i("WeatherScreen_Debug", "[Refresh Button] Calling locationManager.getCurrentLocation()...") // New Log
                                    val currentLocation = locationManager.getCurrentLocation()
                                    if (currentLocation != null) {
                                        android.util.Log.i("WeatherScreen_Debug", "[Refresh Button] Got location: Lat=${currentLocation.latitude}, Lon=${currentLocation.longitude}. Calling fetchWeatherData...") // New Log
                                        weatherViewModel.fetchWeatherData(
                                            currentLocation.latitude,
                                            currentLocation.longitude
                                        )
                                        android.util.Log.d("WeatherScreen", "Refreshed weather for current location: Lat=${currentLocation.latitude}, Lon=${currentLocation.longitude}")
                                    } else {
                                        android.util.Log.w("WeatherScreen_Debug", "[Refresh Button] locationManager.getCurrentLocation() returned null. Setting error.") // New Log
                                        weatherViewModel.setError("Unable to get current location. Please ensure you have a clear view of the sky and try again.")
                                    }
                                } catch (e: SecurityException) {
                                    android.util.Log.e("WeatherScreen_Debug", "[Refresh Button] Security exception during location fetch", e)
                                    weatherViewModel.setError("Location permission was revoked. Please grant location permission in system settings.")
                                    showPermissionDialog = true
                                } catch (e: Exception) {
                                    android.util.Log.e("WeatherScreen_Debug", "[Refresh Button] Exception during location fetch: ${e.message}", e)
                                    weatherViewModel.setError("Error getting location. Please check your device's location settings and try again.")
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                weatherData != null -> {
                    val currentWeather = weatherData!!
                    val hourly = currentWeather.hourly
                    
                    WeatherContent(hourly = currentWeather.hourly, currentWeather = currentWeather)
                }
            }
        }
    }
}

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

@Composable
fun WeatherContent(
    hourly: HourlyData,
    currentWeather: WeatherData
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Current Weather Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Current Weather Conditions",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                if (hourly.temperature_2m.isNotEmpty()) {
                    Text(
                        text = "Air Temperature: ${hourly.temperature_2m[0]}${currentWeather.hourly_units.temperature_2m}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                if (hourly.soil_temperature_0cm.isNotEmpty()) {
                    Text(
                        text = "Surface Soil Temperature: ${hourly.soil_temperature_0cm[0]}${currentWeather.hourly_units.soil_temperature_0cm}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                if (hourly.soil_moisture_0_to_1cm.isNotEmpty()) {
                    Text(
                        text = "Surface Soil Moisture: ${hourly.soil_moisture_0_to_1cm[0]}${currentWeather.hourly_units.soil_moisture_0_to_1cm}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        // Weather Analysis Section
        if (hourly.temperature_2m.isNotEmpty() && 
            hourly.soil_temperature_0cm.isNotEmpty() && 
            hourly.soil_moisture_0_to_1cm.isNotEmpty()) {
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Weather Analysis",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    val airTemp = hourly.temperature_2m[0]
                    val soilTemp = hourly.soil_temperature_0cm[0]
                    val soilMoisture = hourly.soil_moisture_0_to_1cm[0]
                    
                    // Temperature Analysis
                    val tempAnalysis = analyzeCropGrowthTemperature(airTemp)
                    ListItem(
                        headlineContent = { 
                            Text(
                                "Temperature Conditions",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        },
                        supportingContent = {
                            Text(
                                tempAnalysis,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        },
                        leadingContent = {
                            Icon(
                                if (airTemp in 20.0..30.0) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (airTemp in 20.0..30.0) Color(0xFF4CAF50) else Color(0xFFFF9800)
                            )
                        }
                    )
                    
                    // Soil Temperature Analysis
                    val soilTempAnalysis = analyzeSoilTemperature(soilTemp)
                    ListItem(
                        headlineContent = { 
                            Text(
                                "Soil Temperature Status",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        },
                        supportingContent = {
                            Text(
                                soilTempAnalysis,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        },
                        leadingContent = {
                            Icon(
                                if (soilTemp in 18.0..24.0) Icons.Default.Grass else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (soilTemp in 18.0..24.0) Color(0xFF4CAF50) else Color(0xFFFF9800)
                            )
                        }
                    )
                    
                    // Soil Moisture Analysis
                    val moistureAnalysis = analyzeSoilMoisture(soilMoisture)
                    ListItem(
                        headlineContent = { 
                            Text(
                                "Soil Moisture Level",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        },
                        supportingContent = {
                            Text(
                                moistureAnalysis,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        },
                        leadingContent = {
                            Icon(
                                if (soilMoisture in 0.20..0.35) Icons.Default.WaterDrop else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (soilMoisture in 0.20..0.35) Color(0xFF4CAF50) else Color(0xFFFF9800)
                            )
                        }
                    )
                }
            }
        }

        // Hourly Forecast
        Text(
            text = "Hourly Forecast",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(24) { index ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Hour ${index + 1}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        if (index < hourly.temperature_2m.size) {
                            Text(
                                text = "Air Temperature: ${hourly.temperature_2m[index]}${currentWeather.hourly_units.temperature_2m}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        if (index < hourly.soil_temperature_0cm.size) {
                            Text(
                                text = "Surface Soil Temperature: ${hourly.soil_temperature_0cm[index]}${currentWeather.hourly_units.soil_temperature_0cm}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        if (index < hourly.soil_moisture_0_to_1cm.size) {
                            Text(
                                text = "Surface Soil Moisture: ${hourly.soil_moisture_0_to_1cm[index]}${currentWeather.hourly_units.soil_moisture_0_to_1cm}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HourlyForecastItem(
    time: String,
    temperature: Double,
    soilTemperature: Double,
    soilMoisture: Double,
    humidity: Int,
    windSpeed: Double,
    precipitation: Double
) {
    Card(
        modifier = Modifier.width(120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = time.split("T")[1].substring(0, 5), // Format time to HH:mm
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${temperature}°C",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Soil: ${soilTemperature}°C",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Moisture: ${soilMoisture} m³/m³",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Humidity: ${humidity}%",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Wind: ${windSpeed} km/h",
                style = MaterialTheme.typography.bodySmall
            )
            if (precipitation > 0) {
                Text(
                    text = "Rain: ${precipitation} mm",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}