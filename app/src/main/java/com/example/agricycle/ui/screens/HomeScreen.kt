package com.example.agricycle.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agricycle.ui.utils.rememberCurrentLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import com.example.agricycle.navigation.Screen
import com.example.agricycle.ui.utils.*
import com.example.agricycle.ui.viewmodel.WeatherViewModel
import com.example.agricycle.viewmodel.LoginViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.example.agricycle.ui.components.VideoBackground

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    loginViewModel: LoginViewModel
) {
    val weatherViewModel: WeatherViewModel = viewModel()
    val weatherData by weatherViewModel.weatherData.collectAsState()
    val isLoading by weatherViewModel.isLoading.collectAsState()
    val error by weatherViewModel.error.collectAsState()
    val currentUser by loginViewModel.currentUser.collectAsState()

    // Location permissions
    val (hasLocationPermission, requestLocationPermission) = rememberLocationPermissions()
    // Provide a default location initially
    val defaultLocation = LatLng(51.5074, -0.1278) // Default London coordinates
    val currentLocation = rememberCurrentLocation(defaultLocation = defaultLocation)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 15f) // Start with default
    }

    // Fetch weather data when currentLocation changes and is not the default
    LaunchedEffect(currentLocation) {
        // Avoid fetching with the initial default location if it hasn't updated yet
        if (currentLocation != defaultLocation) {
            weatherViewModel.fetchWeatherData(currentLocation.latitude, currentLocation.longitude)
        }
    }

    // Update map position when currentLocation changes
    LaunchedEffect(currentLocation) {
        if (currentLocation != defaultLocation) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(currentLocation, 15f),
                durationMs = 1000
            )
        }
    }

    Scaffold(
        topBar = {
            FadeInAnimation {
                CenterAlignedTopAppBar(
                    title = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Agricycle",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )

                        }
                    },
                    actions = {
                        var unreadNotifications by remember { mutableStateOf(true) }
                        BadgedBox(
                            badge = {
                                if (unreadNotifications) {
                                    Badge(containerColor = MaterialTheme.colorScheme.error)
                                }
                            }
                        ) {
                            IconButton(onClick = {
                                navController.navigate(Screen.Notifications.route)
                                unreadNotifications = false
                            }) {
                                Icon(
                                    Icons.Filled.Notifications,
                                    contentDescription = "Notifications"
                                )
                            }
                        }
                        IconButton(onClick = { navController.navigate(Screen.Account.route) }) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Account")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
        bottomBar = {
            FadeInAnimation {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = true,
                        onClick = { }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Analytics, contentDescription = "Analysis") },
                        label = { Text("Analysis") },
                        selected = false,
                        onClick = { navController.navigate(Screen.Analysis.route) }
                    )
                    NavigationBarItem(
                        icon = {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = "Prediction"
                            )
                        },
                        label = { Text("Prediction") },
                        selected = false,
                        onClick = { navController.navigate(Screen.Prediction.route) }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Newspaper, contentDescription = "News") },
                        label = { Text("News") },
                        selected = false,
                        onClick = { navController.navigate(Screen.News.route) }
                    )
                }
            }
        }
    ) { paddingValues ->
        // Video Background URI
        val context = LocalContext.current
        val videoUri = remember {
            Uri.parse("android.resource://${context.packageName}/raw/nature_back2")
        }

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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Current Weather Card
                    SlideInAnimation {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                MaterialTheme.colorScheme.surface
                                            )
                                        )
                                    )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "Current Weather",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    when {
                                        isLoading -> {
                                            CircularProgressIndicator(
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(48.dp)
                                            )
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
                                                hourly.soil_moisture_0_to_1cm.isNotEmpty()
                                            ) {
                                                // Air Temperature
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Thermostat,
                                                        contentDescription = "Air Temperature",
                                                        tint = Color(0xFF2196F3),
                                                        modifier = Modifier.size(48.dp)
                                                    )
                                                    Text(
                                                        text = "${hourly.temperature_2m[0]}${currentWeather.hourly_units.temperature_2m}",
                                                        style = MaterialTheme.typography.displayLarge.copy(
                                                            fontSize = 64.sp,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = Color(0xFF2196F3)
                                                    )
                                                    Text(
                                                        text = "Air Temperature",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(24.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceEvenly
                                                ) {
                                                    // Soil Temperature
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Grass,
                                                            contentDescription = "Soil Temperature",
                                                            tint = Color(0xFF4CAF50),
                                                            modifier = Modifier.size(32.dp)
                                                        )
                                                        Text(
                                                            text = "${hourly.soil_temperature_0cm[0]}${currentWeather.hourly_units.soil_temperature_0cm}",
                                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                                fontWeight = FontWeight.Bold
                                                            ),
                                                            color = Color(0xFF4CAF50)
                                                        )
                                                        Text(
                                                            text = "Soil Temperature",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }

                                                    // Soil Moisture
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.WaterDrop,
                                                            contentDescription = "Soil Moisture",
                                                            tint = Color(0xFF00BCD4),
                                                            modifier = Modifier.size(32.dp)
                                                        )
                                                        Text(
                                                            text = "${hourly.soil_moisture_0_to_1cm[0]}${currentWeather.hourly_units.soil_moisture_0_to_1cm}",
                                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                                fontWeight = FontWeight.Bold
                                                            ),
                                                            color = Color(0xFF00BCD4)
                                                        )
                                                        Text(
                                                            text = "Soil Moisture",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = MaterialTheme.colorScheme.onSurface
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

                    // Map Card
                    SlideInAnimation {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                                MaterialTheme.colorScheme.surface
                                            )
                                        )
                                    )
                            ) {
                                when {
                                    !hasLocationPermission -> {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Button(
                                                onClick = { requestLocationPermission() },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary
                                                )
                                            ) {
                                                Text("Enable Location")
                                            }
                                        }
                                    }

                                    else -> {
                                        GoogleMap(
                                            modifier = Modifier.fillMaxSize(),
                                            cameraPositionState = cameraPositionState,
                                            properties = MapProperties(
                                                isMyLocationEnabled = true
                                            ),
                                            uiSettings = MapUiSettings(
                                                zoomControlsEnabled = true,
                                                myLocationButtonEnabled = true
                                            )
                                        ) {
                                            Marker(
                                                state = MarkerState(position = currentLocation),
                                                title = "Current Location"
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
    fun WeatherInfoItem(
        title: String,
        value: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        color: Color
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
        }
    }
}