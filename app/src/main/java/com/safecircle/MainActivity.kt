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
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
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

        // Get FCM token
        getFCMToken()

        // Handle intent from notification
        sosViewModel.handleIntent(intent)

        setContent {
            SafeCircleTheme {
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
