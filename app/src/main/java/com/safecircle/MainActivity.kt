package com.safecircle

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.Priority
import android.content.IntentSender
import com.safecircle.repository.AuthRepository
import com.safecircle.ui.navigation.SafeCircleNavigation
import com.safecircle.ui.theme.SafeCircleTheme
import com.safecircle.ui.viewmodel.AuthViewModel
import com.safecircle.ui.viewmodel.SOSViewModel
import android.content.Intent

/**
 * Main activity of the SafeCircle app
 */
class MainActivity : ComponentActivity() {
    
    private val authViewModel: AuthViewModel by viewModels()
    private val sosViewModel: SOSViewModel by viewModels()

    // Permission launcher for notifications (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(
                this,
                R.string.notification_permission_required,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Permission launcher for location
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (!fineLocationGranted && !coarseLocationGranted) {
            Toast.makeText(
                this,
                "Location permission is required for SOS features",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Request location permissions
        requestLocationPermissions()

        // Get FCM token
        getFCMToken()

        // Handle intent from notification
        sosViewModel.handleIntent(intent)

        setContent {
            val isDarkMode by authViewModel.isDarkMode.collectAsState()
            
            SafeCircleTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SafeCircleNavigation(
                        authViewModel = authViewModel,
                        sosViewModel = sosViewModel
                    )
                }
            }
        }
    }

    private fun requestLocationPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        val needsRequest = permissions.any {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needsRequest) {
            locationPermissionLauncher.launch(permissions)
        } else {
            // If permissions are already granted, ensure GPS is turned on
            checkLocationSettings()
        }
    }

    private fun checkLocationSettings() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this, 1001)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        sosViewModel.handleIntent(intent)
    }

    /**
     * Get and update FCM token
     */
    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            if (token.isNotEmpty()) {
                // Update FCM token in Firestore
                val authRepository = AuthRepository()
                lifecycleScope.launch {
                    authRepository.updateFCMToken(token)
                }
            }
        }.addOnFailureListener { e ->
            // Handle token retrieval failure
            Toast.makeText(this, "Failed to get notification token", Toast.LENGTH_SHORT).show()
        }
    }
}
