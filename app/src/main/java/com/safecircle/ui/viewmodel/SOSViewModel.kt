package com.safecircle.ui.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.safecircle.repository.SOSRepository
import com.safecircle.service.LocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for SOS operations
 */
class SOSViewModel : ViewModel() {
    
    private val sosRepository = SOSRepository()
    private lateinit var locationService: LocationService
    
    private val _sosState = MutableStateFlow<SOSState>(SOSState.Idle)
    val sosState: StateFlow<SOSState> = _sosState
    
    private val _locationState = MutableStateFlow<LocationState>(LocationState.Idle)
    val locationState: StateFlow<LocationState> = _locationState

    private val _incomingAlert = MutableStateFlow<Map<String, Any>?>(null)
    val incomingAlert: StateFlow<Map<String, Any>?> = _incomingAlert

    private var alertListener: com.google.firebase.firestore.ListenerRegistration? = null

    private val _sosHistory = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val sosHistory: StateFlow<List<Map<String, Any>>> = _sosHistory

    init {
        startListeningForAlerts()
        loadSOSHistory()
    }

    /**
     * Load SOS history for current user
     */
    fun loadSOSHistory() {
        viewModelScope.launch {
            val result = sosRepository.getSOSHistory()
            result.fold(
                onSuccess = { history ->
                    _sosHistory.value = history
                },
                onFailure = { /* Handle error if needed */ }
            )
        }
    }

    private var listenerStartTime: Long = 0

    /**
     * Start listening for SOS alerts from friends
     */
    fun startListeningForAlerts() {
        listenerStartTime = System.currentTimeMillis()
        alertListener?.remove()
        alertListener = sosRepository.listenForIncomingAlerts { alertData ->
            val timestamp = alertData["timestamp"] as? Long ?: 0
            
            // Only process alerts that happened AFTER we started listening
            if (timestamp > listenerStartTime) {
                _incomingAlert.value = alertData
                triggerSystemNotification(alertData)
            }
        }
    }

    /**
     * Handle intent extras for SOS alerts
     */
    fun handleIntent(intent: android.content.Intent?) {
        val locationLink = intent?.getStringExtra("locationLink")
        val body = intent?.getStringExtra("body")
        if (locationLink != null) {
            _incomingAlert.value = mapOf(
                "locationLink" to locationLink,
                "body" to (body ?: "A friend needs help!"),
                "timestamp" to System.currentTimeMillis()
            )
        }
    }

    /**
     * Trigger a system notification for the SOS alert
     */
    private var context: android.content.Context? = null

    fun setContext(context: android.content.Context) {
        this.context = context
    }

    private fun triggerSystemNotification(alertData: Map<String, Any>) {
        val ctx = context ?: return
        val title = alertData["title"]?.toString() ?: "🚨 EMERGENCY ALERT"
        val body = alertData["body"]?.toString() ?: "A friend needs help!"
        
        val channelId = "safe_circle_emergency"
        val notificationManager = ctx.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Emergency Alerts",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = android.content.Intent(ctx, com.safecircle.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("locationLink", alertData["locationLink"]?.toString())
            putExtra("body", alertData["body"]?.toString())
        }
        
        val pendingIntent = android.app.PendingIntent.getActivity(
            ctx, 0, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = androidx.core.app.NotificationCompat.Builder(ctx, channelId)
            .setSmallIcon(com.safecircle.R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    /**
     * Clear the incoming alert
     */
    fun dismissAlert() {
        _incomingAlert.value = null
    }

    override fun onCleared() {
        super.onCleared()
        alertListener?.remove()
    }

    /**
     * Initialize location service
     */
    fun initLocationService(locationService: LocationService) {
        this.locationService = locationService
    }

    /**
     * Send SOS alert
     */
    fun sendSOSAlert() {
        if (!::locationService.isInitialized) {
            _sosState.value = SOSState.Error("Location service not initialized")
            return
        }

        if (!locationService.hasLocationPermission()) {
            _sosState.value = SOSState.Error("Location permission not granted")
            return
        }

        _sosState.value = SOSState.Sending
        _locationState.value = LocationState.GettingLocation

        viewModelScope.launch {
            try {
                // Get current location
                val location = locationService.getCurrentLocation()
                
                if (location == null) {
                    _locationState.value = LocationState.Error("Unable to get location. Please check GPS.")
                    _sosState.value = SOSState.Error("Location unavailable")
                    return@launch
                }

                _locationState.value = LocationState.Success(location)

                // Send SOS alert
                val result = sosRepository.sendSOSAlert(location)
                result.fold(
                    onSuccess = {
                        _sosState.value = SOSState.Success(location)
                        loadSOSHistory() // Refresh history
                    },
                    onFailure = { exception ->
                        _sosState.value = SOSState.Error(exception.message ?: "Failed to send SOS alert")
                    }
                )
            } catch (e: Exception) {
                _locationState.value = LocationState.Error(e.message ?: "Location error")
                _sosState.value = SOSState.Error(e.message ?: "SOS failed")
            }
        }
    }

    /**
     * Reset SOS state
     */
    fun resetSOSState() {
        _sosState.value = SOSState.Idle
        _locationState.value = LocationState.Idle
    }

    /**
     * Generate maps link from location
     */
    fun generateMapsLink(location: Location): String {
        return locationService.generateMapsLink(location)
    }
}

/**
 * SOS state sealed class
 */
sealed class SOSState {
    object Idle : SOSState()
    object Sending : SOSState()
    data class Success(val location: Location) : SOSState()
    data class Error(val message: String) : SOSState()
}

/**
 * Location state sealed class
 */
sealed class LocationState {
    object Idle : LocationState()
    object GettingLocation : LocationState()
    data class Success(val location: Location) : LocationState()
    data class Error(val message: String) : LocationState()
}
