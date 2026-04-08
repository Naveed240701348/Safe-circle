package com.safecircle.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrecautionsScreen(
    onNavigateBack: () -> Unit
) {
    val precautions = listOf(
        PrecautionItem(
            "Stay Aware",
            "Be conscious of your surroundings at all times, especially in unfamiliar areas.",
            Icons.Default.Visibility,
            Color(0xFF4CAF50)
        ),
        PrecautionItem(
            "Keep Phone Charged",
            "Ensure your mobile device is fully charged before heading out.",
            Icons.Default.BatteryChargingFull,
            Color(0xFF2196F3)
        ),
        PrecautionItem(
            "Share Location",
            "Always keep your trusted circle updated about your real-time location.",
            Icons.Default.LocationOn,
            Color(0xFFFF9800)
        ),
        PrecautionItem(
            "Trust Your Instincts",
            "If a situation or place feels unsafe, leave immediately.",
            Icons.Default.Warning,
            Color(0xFFF44336)
        ),
        PrecautionItem(
            "Avoid Dark Areas",
            "Stick to well-lit paths and avoid secluded areas during late hours.",
            Icons.Default.Lightbulb,
            Color(0xFF9C27B0)
        ),
        PrecautionItem(
            "Emergency Plan",
            "Have a pre-planned route and a designated person to call in emergencies.",
            Icons.Default.Assignment,
            Color(0xFF607D8B)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Safety Precautions",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "Essential Safety Tips",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        "Follow these precautions to enhance your personal safety and stay prepared for any situation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                items(precautions) { item ->
                    PrecautionCard(item)
                }
            }
        }
    }
}

data class PrecautionItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color
)

@Composable
fun PrecautionCard(item: PrecautionItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(item.iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    item.icon,
                    contentDescription = null,
                    tint = item.iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
