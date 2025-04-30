package com.example.agricycle.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agricycle.navigation.Screen
import com.example.agricycle.ui.utils.rememberCurrentLocation
import com.example.agricycle.ui.viewmodel.WeatherViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldAnalysisScreen(navController: NavController) {
    val weatherViewModel: WeatherViewModel = viewModel()
    val weatherData by weatherViewModel.weatherData.collectAsState()
    val isLoading by weatherViewModel.isLoading.collectAsState()
    val error by weatherViewModel.error.collectAsState()
    
    // Get current location
    val currentLocation = rememberCurrentLocation()

    // Fetch weather data when currentLocation changes and is not the default
    LaunchedEffect(currentLocation) {
        // Avoid fetching with the initial default location if it hasn't updated yet
        if (currentLocation != LatLng(51.5074, -0.1278)) { // Avoid fetching with default
            weatherViewModel.fetchWeatherData(currentLocation.latitude, currentLocation.longitude)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Field Analysis") },
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Field Analysis",
                style = MaterialTheme.typography.titleLarge
            )
            
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
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
                    
                    if (hourly.temperature_2m.isNotEmpty() && 
                        hourly.soil_temperature_0cm.isNotEmpty() && 
                        hourly.soil_moisture_0_to_1cm.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Current Field Conditions",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Air Temperature: ${hourly.temperature_2m[0]}${currentWeather.hourly_units.temperature_2m}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "Surface Soil Temperature: ${hourly.soil_temperature_0cm[0]}${currentWeather.hourly_units.soil_temperature_0cm}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "Surface Soil Moisture: ${hourly.soil_moisture_0_to_1cm[0]}${currentWeather.hourly_units.soil_moisture_0_to_1cm}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
            
            Button(
                onClick = { navController.navigate(Screen.Prediction.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Weather Prediction")
            }
        }
    }
}

@Composable
fun MapView() {
    // Consider using currentLocation here if the map should show the user's location
    val defaultMapLocation = LatLng(1.35, 103.87) // Default to Singapore for now
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultMapLocation, 10f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        )
        
        // Floating Action Buttons for map controls
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = { /* TODO: Add field boundary */ },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, "Add Field")
            }
            FloatingActionButton(
                onClick = { /* TODO: Toggle satellite view */ },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Satellite, "Toggle Satellite")
            }
        }
    }
}

@Composable
fun AnalysisView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Field Health Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Field Health Index",
                    style = MaterialTheme.typography.titleMedium
                )
                LinearProgressIndicator(
                    progress = 0.75f,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Health Score: 75/100")
            }
        }

        // Soil Analysis Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Soil Analysis",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Moisture")
                        Text("65%", style = MaterialTheme.typography.bodyLarge)
                    }
                    Column {
                        Text("pH Level")
                        Text("6.5", style = MaterialTheme.typography.bodyLarge)
                    }
                    Column {
                        Text("Nitrogen")
                        Text("High", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}