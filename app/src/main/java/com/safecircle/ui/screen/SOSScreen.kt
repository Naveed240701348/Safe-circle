package com.safecircle.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.safecircle.R
import com.safecircle.service.LocationService
import com.safecircle.ui.viewmodel.SOSViewModel
import com.safecircle.ui.viewmodel.SOSState
import com.safecircle.ui.viewmodel.LocationState

/**
 * SOS screen with emergency button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SOSScreen(
    sosViewModel: SOSViewModel,
    onNavigateToFriends: () -> Unit,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val locationService = remember { LocationService(context) }
    
    val sosState by sosViewModel.sosState.collectAsState()
    val currentLocationState by sosViewModel.locationState.collectAsState()
    val incomingAlert by sosViewModel.incomingAlert.collectAsState()
    
    // Initialize location service
    LaunchedEffect(Unit) {
        sosViewModel.initLocationService(locationService)
    }

    // ... (rest of the file until the Box containing the SOS button)
    
    // Incoming Alert Overlay
    incomingAlert?.let { alert ->
        AlertDialog(
            onDismissRequest = { sosViewModel.dismissAlert() },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp)) },
            title = { Text(text = "🚨 EMERGENCY ALERT", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(text = alert["body"]?.toString() ?: "A friend needs help!", fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val link = alert["locationLink"]?.toString()
                            if (link != null) {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(link))
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Location on Maps")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { sosViewModel.dismissAlert() }) {
                    Text("Dismiss")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back to Dashboard",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onNavigateToFriends) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "Friends",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Text(
                text = "SafeCircle",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Status text
        val currentSosState = sosState
        when (currentSosState) {
            is SOSState.Idle -> {
                Text(
                    text = "Press SOS in case of emergency",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
            is SOSState.Sending -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Sending emergency alert...",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
            is SOSState.Success -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Emergency alert sent!\nLocation: $currentLocationState",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Return to Dashboard")
                    }
                }
            }
            is SOSState.Error -> {
                Text(
                    text = "Error: ${currentSosState.message}",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(64.dp))
        
        // SOS Button
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(
                    if (sosState is SOSState.Sending)
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        Color(0xFFFF1744) // Emergency red
                ),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    if (sosState !is SOSState.Sending) {
                        sosViewModel.sendSOSAlert()
                    }
                },
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (sosState is SOSState.Sending)
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        Color(0xFFFF1744)
                ),
                enabled = sosState !is SOSState.Sending
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PriorityHigh,
                        contentDescription = "SOS",
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.sos_button),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Location status
        val locState = currentLocationState
        when (locState) {
            is LocationState.Idle -> {
                Text(
                    text = "Location ready",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            is LocationState.GettingLocation -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Getting location...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is LocationState.Success -> {
                Text(
                    text = "Location: ${String.format("%.6f", locState.location.latitude)}, ${String.format("%.6f", locState.location.longitude)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            is LocationState.Error -> {
                Text(
                    text = "Location error: ${locState.message}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
