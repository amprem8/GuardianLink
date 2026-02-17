package ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class HomeUiState(
    val userName: String,
    val phoneNumber: String,
    val contacts: List<String>,
    val voicePhrase: String,
    val gestureType: String,
    val isOnline: Boolean
)

data class HomeActions(
    val onTriggerSOS: () -> Unit,
    val onEditContacts: () -> Unit,
    val onEditConfig: () -> Unit,
    val onLogout: () -> Unit
)

@Composable
fun HomeScreen(
    state: HomeUiState,
    actions: HomeActions
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(45.dp)
    ) {
        Spacer(Modifier.width(3.dp))

        HomeHeader(state.isOnline, actions.onLogout)
        SOSButton(actions.onTriggerSOS)
        ContactsSection(state.contacts, actions.onEditContacts)
        ConfigSection(state.voicePhrase, state.gestureType, actions.onEditConfig)
        HybridInfoSection()

    }
}
@Composable
private fun HomeHeader(isOnline: Boolean, onLogout: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("SOS Guardian", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("Always ready to help", color = Color.Gray, fontSize = 14.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isOnline) {
                StatusChip("Online", Color(0xFFDCFCE7), Color(0xFF15803D))
            } else {
                StatusChip("SMS Mode", Color(0xFFFEF3C7), Color(0xFF92400E))
            }

            Spacer(Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
                        ),
                        CircleShape
                    )
                    .clickable { onLogout() },
                contentAlignment = Alignment.Center
            ) {
                Text("👤")
            }
        }
    }
}
@Composable
private fun SOSButton(onTriggerSOS: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFEF4444), Color(0xFFDC2626))
                ),
                RoundedCornerShape(24.dp)
            )
            .clickable {
                pressed = true
                onTriggerSOS()
                pressed = false
            }
            .padding(vertical = 36.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⚠️", fontSize = 40.sp)
            Spacer(Modifier.height(18.dp))
            Text("Trigger SOS", color = Color.White, fontWeight = FontWeight.Bold)
            Text(
                "Press to send emergency alert",
                color = Color(0xFFFFCDD2),
                fontSize = 12.sp
            )
        }
    }
}
@Composable
private fun ContactsSection(contacts: List<String>, onEditContacts: () -> Unit) {
    DashboardCard(onEditContacts) {
        Text("Emergency Contacts", fontWeight = FontWeight.Bold)
        Text(
            "${contacts.size} contact${if (contacts.size != 1) "s" else ""} configured",
            color = Color.Gray,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(20.dp))

        Row {
            contacts.take(3).forEach {
                Chip(it)
                Spacer(Modifier.width(6.dp))
            }
            if (contacts.size > 3) {
                Chip("+${contacts.size - 3} more")
            }
        }
    }
}
@Composable
private fun ConfigSection(
    voicePhrase: String,
    gestureType: String,
    onEditConfig: () -> Unit
) {
    DashboardCard(onEditConfig) {
        Text("Trigger Configuration", fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(20.dp))

        Text("Voice: \"$voicePhrase\"", color = Color.Gray, fontSize = 13.sp)
        Text(
            "Gesture: ${if (gestureType == "double-tap") "Back Tap (Double)" else "Device Shake"}",
            color = Color.Gray,
            fontSize = 13.sp
        )
    }
}
@Composable
private fun HybridInfoSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                ),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            Text("How Hybrid SOS Works", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))
            Text("Online → internet alerts with GPS + audio", color = Color.LightGray, fontSize = 12.sp)
            Text("Offline → calls first, SMS fallback", color = Color.LightGray, fontSize = 12.sp)
            Text("No answer → transcript to everyone", color = Color.LightGray, fontSize = 12.sp)
            Text("Auto route by connectivity", color = Color.LightGray, fontSize = 12.sp)
        }
    }
}
@Composable
private fun DashboardCard(
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun StatusChip(text: String, bg: Color, fg: Color) {
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, color = fg, fontSize = 12.sp)
    }
}

@Composable
private fun Chip(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFFE0E7FF), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 12.sp)
    }
}
