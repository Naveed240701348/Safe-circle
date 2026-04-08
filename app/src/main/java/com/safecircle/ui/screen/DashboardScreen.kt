package com.safecircle.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.safecircle.data.model.User
import com.safecircle.ui.viewmodel.DashboardViewModel

import com.safecircle.ui.viewmodel.SOSViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    sosViewModel: SOSViewModel,
    onNavigateToSOS: () -> Unit,
    onNavigateToFriends: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPrecautions: () -> Unit,
    onLogout: () -> Unit,
    onToggleTheme: () -> Unit,
    isDarkMode: Boolean
) {
    val uiState by viewModel.uiState.collectAsState()
    val sosHistory by sosViewModel.sosHistory.collectAsState()
    
    var showHistory by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "SafeCircle", 
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    ) 
                },
                actions = {
                    var showMenu by remember { mutableStateOf(false) }
                    
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Profile Settings") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToProfile()
                                },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Safety Precautions") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToPrecautions()
                                },
                                leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isDarkMode) "Light Mode" else "Dark Mode") },
                                onClick = {
                                    showMenu = false
                                    onToggleTheme()
                                },
                                leadingIcon = { 
                                    Icon(
                                        if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode, 
                                        contentDescription = null 
                                    ) 
                                }
                            )
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                            DropdownMenuItem(
                                text = { Text("Logout", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    onLogout()
                                },
                                leadingIcon = { 
                                    Icon(
                                        Icons.Default.Logout, 
                                        contentDescription = null, 
                                        tint = MaterialTheme.colorScheme.error 
                                    ) 
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToSOS,
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(80.dp)
                    .shadow(12.dp, CircleShape)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PriorityHigh, contentDescription = "SOS", modifier = Modifier.size(32.dp))
                    Text("SOS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "My Circle",
                        value = uiState.friends.size.toString(),
                        icon = Icons.Default.Groups,
                        gradient = Brush.linearGradient(
                            colors = listOf(Color(0xFF6200EE), Color(0xFFBB86FC))
                        ),
                        onClick = onNavigateToFriends
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "SOS History",
                        value = sosHistory.size.toString(),
                        icon = Icons.Default.History,
                        gradient = Brush.linearGradient(
                            colors = listOf(Color(0xFFF44336), Color(0xFFFF8A80))
                        ),
                        onClick = { showHistory = !showHistory }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (showHistory) {
                    SectionHeader(
                        title = "Alert History", 
                        onClose = { showHistory = false },
                        isCloseVisible = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (sosHistory.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.History,
                            message = "No SOS alerts sent yet"
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(sosHistory) { event ->
                                SOSHistoryItem(event)
                            }
                        }
                    }
                } else {
                    SectionHeader(
                        title = "Your Trusted Circle", 
                        onAction = onNavigateToFriends,
                        actionIcon = Icons.Default.Add
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (uiState.isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (uiState.friends.isEmpty()) {
                        EmptyFriendsView(onNavigateToFriends)
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(uiState.friends) { friend ->
                                ModernFriendItem(friend)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String, 
    onClose: (() -> Unit)? = null, 
    isCloseVisible: Boolean = false,
    onAction: (() -> Unit)? = null,
    actionIcon: ImageVector? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (isCloseVisible && onClose != null) {
            TextButton(onClick = onClose) {
                Text("View Friends")
            }
        } else if (onAction != null && actionIcon != null) {
            IconButton(
                onClick = onAction,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Icon(actionIcon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun SOSHistoryItem(event: Map<String, Any>) {
    val timestamp = event["timestamp"] as? Long ?: 0
    val date = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(timestamp))
    val friendsNotified = event["friendsNotified"] as? Long ?: 0
    val context = androidx.compose.ui.platform.LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.GpsFixed, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "SOS Sent", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                Text(
                    text = "Notified $friendsNotified friends", 
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Button(
                onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        data = android.net.Uri.parse(event["locationLink"].toString())
                    }
                    context.startActivity(intent)
                },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
            ) {
                Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Map", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    gradient: Brush,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = Color.White)
                Column {
                    Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text(text = title, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    }
}

@Composable
private fun ModernFriendItem(friend: User) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(50.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (friend.profileImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = friend.profileImageUrl,
                            contentDescription = "Friend Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = friend.name.take(1).uppercase(),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = friend.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = friend.email, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )
            }
            Icon(
                Icons.Default.ChevronRight, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Composable
fun EmptyStateView(icon: ImageVector, message: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(12.dp))
            Text(message, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun EmptyFriendsView(onNavigateToFriends: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.GroupAdd,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Your circle is lonely", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            "Add friends to keep each other safe", 
            color = MaterialTheme.colorScheme.outline,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToFriends, shape = RoundedCornerShape(12.dp)) {
            Text("Find Friends")
        }
    }
}
