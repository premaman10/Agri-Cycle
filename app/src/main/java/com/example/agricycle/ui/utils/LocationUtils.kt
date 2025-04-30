package com.example.agricycle.ui.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberLocationPermissions(): Pair<Boolean, () -> Unit> {
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    return Pair(
        locationPermissions.allPermissionsGranted,
        { locationPermissions.launchMultiplePermissionRequest() }
    )
}

@SuppressLint("MissingPermission")
@Composable
fun rememberCurrentLocation(
    defaultLocation: LatLng = LatLng(51.5074, -0.1278),
    minAccuracyMeters: Float = 100f,
    minDistanceMeters: Float = 10f
): LatLng {
    var currentLocation by remember { mutableStateOf(defaultLocation) }
    var lastUpdateTime by remember { mutableStateOf(0L) }
    val context = LocalContext.current
    val (hasPermission, requestPermission) = rememberLocationPermissions()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(hasPermission) {
        if (!hasPermission) {
            requestPermission()
        }
    }

    DisposableEffect(hasPermission) {
        if (!hasPermission) {
            currentLocation = defaultLocation
            return@DisposableEffect onDispose {}
        }

        // Try getting last known location immediately
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                currentLocation = LatLng(it.latitude, it.longitude)
            }
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 15000) // 15 seconds interval
            .setMinUpdateIntervalMillis(10000) // 10 seconds minimum interval
            .setMinUpdateDistanceMeters(minDistanceMeters) // Reduced minimum displacement to 10f
            .setMaxUpdateDelayMillis(30000) // Max wait time for batching
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val currentTime = System.currentTimeMillis()
                    val timeSinceLastUpdate = currentTime - lastUpdateTime
                    val speed = if (location.hasSpeed()) location.speed else -1f // Use -1 if no speed
                    android.util.Log.d("LocationUtils_Debug", "Received location: Lat=${location.latitude}, Lng=${location.longitude}, Acc=${location.accuracy}m, Speed=${speed}m/s, TimeSinceLast=${timeSinceLastUpdate}ms")

                    // Adaptive accuracy threshold based on battery and movement
                    val adaptiveAccuracy = if (location.hasSpeed() && location.speed > 5f) {
                        minAccuracyMeters * 1.5f // Relax accuracy requirements when moving
                    } else {
                        minAccuracyMeters
                    }
                    android.util.Log.d("LocationUtils_Debug", "Filtering thresholds: adaptiveAccuracy=${adaptiveAccuracy}m, minTime=5000ms, maxSpeed=30m/s")

                    val isAccurateEnough = location.accuracy <= adaptiveAccuracy
                    val isTimeElapsed = timeSinceLastUpdate >= 5000 // Reduced minimum time for debugging
                    val isSpeedAcceptable = !location.hasSpeed() || location.speed <= 30f

                    android.util.Log.d("LocationUtils_Debug", "Filter check: isAccurateEnough=$isAccurateEnough, isTimeElapsed=$isTimeElapsed, isSpeedAcceptable=$isSpeedAcceptable")

                    if (isAccurateEnough && isTimeElapsed && isSpeedAcceptable) { 
                        android.util.Log.i("LocationUtils_Debug", "Updating location: Lat=${location.latitude}, Lng=${location.longitude}")
                        currentLocation = LatLng(location.latitude, location.longitude)
                        lastUpdateTime = currentTime
                    } else {
                        var reason = "Location update rejected:"
                        if (!isAccurateEnough) reason += " Accuracy (${location.accuracy}m > ${adaptiveAccuracy}m)."
                        if (!isTimeElapsed) reason += " Time (${timeSinceLastUpdate}ms < 5000ms)."
                        if (!isSpeedAcceptable) reason += " Speed (${location.speed}m/s > 30m/s)."
                        android.util.Log.w("LocationUtils_Debug", reason)
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    return currentLocation
}