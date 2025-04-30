package com.example.agricycle.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationManager(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        android.util.Log.i("LocationManager_Debug", "[getCurrentLocation] Entered function") // New Log
        android.util.Log.d("LocationManager", "Starting location request")
        // Check location permissions
        android.util.Log.i("LocationManager_Debug", "[getCurrentLocation] Checking permissions...") // New Log
        val hasFineLocation = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        android.util.Log.d("LocationManager", "Permission status - Fine: $hasFineLocation, Coarse: $hasCoarseLocation")
        android.util.Log.i("LocationManager_Debug", "[getCurrentLocation] Permission check result - Fine: $hasFineLocation, Coarse: $hasCoarseLocation") // New Log

        if (hasFineLocation || hasCoarseLocation) {
            android.util.Log.i("LocationManager_Debug", "[getCurrentLocation] Permissions granted. Proceeding...") // New Log
            try {
                android.util.Log.i("LocationManager_Debug", "[getCurrentLocation] Attempting to get last known location...") // New Log
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { lastLocation ->
                        android.util.Log.i("LocationManager_Debug", "[getCurrentLocation] lastLocation.addOnSuccessListener triggered.") // New Log
                        if (lastLocation != null && System.currentTimeMillis() - lastLocation.time < 30000) {
                            android.util.Log.d("LocationManager", "Using recent last known location - Latitude: ${lastLocation.latitude}, Longitude: ${lastLocation.longitude}, Time: ${lastLocation.time}")
                            continuation.resume(lastLocation)
                            return@addOnSuccessListener
                        }
                        android.util.Log.i("LocationManager_Debug", "[getCurrentLocation] Last known location is null or too old.") // New Log
                        
                        android.util.Log.d("LocationManager", "Last known location is null or too old, requesting fresh updates")
                        requestFreshLocation(continuation)
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.i("LocationManager_Debug", "[getCurrentLocation] lastLocation.addOnFailureListener triggered.") // New Log
                        android.util.Log.e("LocationManager", "Failed to get last location", e)
                        requestFreshLocation(continuation)
                    }
            } catch (e: Exception) {
                android.util.Log.e("LocationManager_Debug", "[getCurrentLocation] Exception during lastLocation check or fresh request call", e) // New Log
                android.util.Log.e("LocationManager", "Error in location request", e)
                continuation.resumeWithException(e)
            }
        } else {
            android.util.Log.w("LocationManager_Debug", "[getCurrentLocation] Permissions NOT granted.") // New Log
            android.util.Log.d("LocationManager", "Location permission not granted")
            continuation.resume(null)
        }
    }

    private fun requestFreshLocation(continuation: kotlinx.coroutines.CancellableContinuation<Location?>) {
        android.util.Log.i("LocationManager_Debug", "[requestFreshLocation] Entered function") // New Log
        android.util.Log.d("LocationManager", "Starting fresh location request with high accuracy priority")
        try {
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    android.util.Log.i("LocationManager_Debug", "[requestFreshLocation] onLocationResult triggered.") // New Log
                    val location = locationResult.lastLocation
                    if (location != null) {
                        android.util.Log.d("LocationManager", "New location update - Latitude: ${location.latitude}, Longitude: ${location.longitude}, Accuracy: ${location.accuracy}")
                        if (location.accuracy <= 100) { // Stricter accuracy threshold
                            android.util.Log.i("LocationManager_Debug", "[requestFreshLocation] Sufficient accuracy (${location.accuracy}m) achieved. Resuming continuation.") // New Log
                            fusedLocationClient.removeLocationUpdates(this)
                            continuation.resume(location)
                            return
                        }
                        // Continue receiving updates if accuracy is not good enough
                        android.util.Log.w("LocationManager_Debug", "[requestFreshLocation] Location accuracy too low: ${location.accuracy}m, waiting for better accuracy") // New Log
                        android.util.Log.d("LocationManager", "Location accuracy too low: ${location.accuracy}m, waiting for better accuracy")
                    } else {
                        android.util.Log.w("LocationManager_Debug", "[requestFreshLocation] Received null location in onLocationResult.") // New Log
                        android.util.Log.d("LocationManager", "Received null location update")
                    }
                }
            }

            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500)
                .setMinUpdateIntervalMillis(250)
                .setMaxUpdates(30) // Increased max updates for better accuracy
                .setWaitForAccurateLocation(true)
                .setMaxUpdateDelayMillis(10000) // Reduced max delay for faster response
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build()
            
            android.util.Log.d("LocationManager", "Location request configured - Update interval: 500ms, Max updates: 15, Max delay: 15000ms, Wait for accurate: true")


            android.util.Log.d("LocationManager", "Requesting location updates from FusedLocationClient")
            android.util.Log.i("LocationManager_Debug", "[requestFreshLocation] Requesting location updates from FusedLocationClient...") // New Log
            android.util.Log.d("LocationManager", "Requesting location updates from FusedLocationClient")
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            android.util.Log.d("LocationManager", "Location updates request registered successfully")
            android.util.Log.i("LocationManager_Debug", "[requestFreshLocation] Location updates request registered successfully.") // New Log

            // Add a timeout mechanism
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                android.util.Log.w("LocationManager_Debug", "[requestFreshLocation] Timeout handler triggered.") // New Log
                fusedLocationClient.removeLocationUpdates(locationCallback)
                if (!continuation.isCompleted) {
                    android.util.Log.d("LocationManager", "Location request timed out after 20 seconds - No accurate location received")
                    android.util.Log.w("LocationManager_Debug", "[requestFreshLocation] Location request timed out after 15 seconds - Resuming with null.") // New Log
                    continuation.resume(null)
                }
                 else { android.util.Log.i("LocationManager_Debug", "[requestFreshLocation] Timeout handler triggered, but continuation already completed.") } // New Log
            }, 15000) // Reduced timeout to 15 seconds for faster fallback

            continuation.invokeOnCancellation {
                android.util.Log.w("LocationManager_Debug", "[requestFreshLocation] Continuation cancelled (invokeOnCancellation).") // New Log
                android.util.Log.d("LocationManager", "Location updates cancelled by user or system")
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        } catch (e: Exception) {
            android.util.Log.e("LocationManager_Debug", "[requestFreshLocation] Exception during location update request.", e) // New Log
            android.util.Log.e("LocationManager", "Error requesting location updates", e)
            continuation.resumeWithException(e)
        }
    }
}