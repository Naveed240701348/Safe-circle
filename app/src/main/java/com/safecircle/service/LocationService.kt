package com.safecircle.service

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Location service for getting current GPS location
 */
class LocationService(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Check if location permissions are granted
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get current location using FusedLocationProviderClient with fallback to last location
     */
    suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) {
            throw SecurityException("Location permissions not granted")
        }

        return try {
            suspendCancellableCoroutine { continuation ->
                val cancellationTokenSource = CancellationTokenSource()
                
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location ->
                    if (location != null) {
                        Log.d(TAG, "Current location obtained: ${location.latitude}, ${location.longitude}")
                        continuation.resume(location)
                    } else {
                        // Fallback to last known location if current is null
                        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                            if (lastLoc != null) {
                                Log.d(TAG, "Last known location obtained: ${lastLoc.latitude}, ${lastLoc.longitude}")
                                continuation.resume(lastLoc)
                            } else {
                                Log.w(TAG, "Both current and last location are null")
                                continuation.resume(null)
                            }
                        }.addOnFailureListener { 
                            continuation.resume(null)
                        }
                    }
                }.addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting current location, trying last known", exception)
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                        continuation.resume(lastLoc)
                    }.addOnFailureListener {
                        continuation.resumeWithException(exception)
                    }
                }

                continuation.invokeOnCancellation {
                    cancellationTokenSource.cancel()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in getCurrentLocation", e)
            null
        }
    }

    /**
     * Generate Google Maps link from location
     */
    fun generateMapsLink(location: Location): String {
        return "https://maps.google.com/?q=${location.latitude},${location.longitude}"
    }

    /**
     * Request location permissions (call from Activity)
     */
    fun requestLocationPermission(activity: Activity, requestCode: Int) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            requestCode
        )
    }

    companion object {
        private const val TAG = "LocationService"
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
