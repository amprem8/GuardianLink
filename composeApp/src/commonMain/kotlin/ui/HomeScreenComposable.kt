package ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        bottomBar = {
            BottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = { },
                onSettingsClick = actions.onEditConfig
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HomeHeader(
                userName = state.userName,
                phoneNumber = state.phoneNumber,
                isOnline = state.isOnline,
                onEditConfig = actions.onEditConfig,
                onLogout = actions.onLogout
            )
            SOSButton(actions.onTriggerSOS)
            ContactsSection(state.contacts, actions.onEditContacts)
            ConfigSection(state.voicePhrase, state.gestureType, actions.onEditConfig)
            HybridInfoSection()
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Header ──────────────────────────────────────────────────

@Composable
private fun HomeHeader(
    userName: String,
    phoneNumber: String,
    isOnline: Boolean,
    onEditConfig: () -> Unit,
    onLogout: () -> Unit
) {
    var showProfileMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "ResQ",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xFF111827)
            )
            Text("Always ready to help", color = Color(0xFF6B7280), fontSize = 14.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Status chip
            if (isOnline) {
                StatusChip(
                    icon = "📶",
                    text = "Online",
                    bg = Color(0xFFDCFCE7),
                    fg = Color(0xFF15803D)
                )
            } else {
                StatusChip(
                    icon = "💬",
                    text = "SMS Mode",
                    bg = Color(0xFFFEF3C7),
                    fg = Color(0xFF92400E)
                )
            }

            // Profile button + menu
            Box {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(
                            elevation = if (showProfileMenu) 8.dp else 0.dp,
                            shape = CircleShape
                        )
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
                            ),
                            CircleShape
                        )
                        .clip(CircleShape)
                        .clickable { showProfileMenu = !showProfileMenu },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.firstOrNull()?.uppercase() ?: "👤",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                DropdownMenu(
                    expanded = showProfileMenu,
                    onDismissRequest = { showProfileMenu = false },
                    modifier = Modifier
                        .width(280.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White),
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 12.dp,
                    tonalElevation = 0.dp
                ) {
                    // Gradient header with user info
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Avatar circle in header
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userName.firstOrNull()?.uppercase() ?: "U",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Column {
                                Text(
                                    text = userName.ifEmpty { "User" },
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                                if (phoneNumber.isNotEmpty()) {
                                    Text(
                                        text = phoneNumber,
                                        color = Color.White.copy(alpha = 0.75f),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    // Settings
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFF3F4F6), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = Color(0xFF6B7280),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    "Settings",
                                    color = Color(0xFF374151),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        },
                        onClick = {
                            showProfileMenu = false
                            onEditConfig()
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Divider
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                        thickness = 0.5.dp,
                        color = Color(0xFFE5E7EB)
                    )

                    // Sign Out
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFFEE2E2), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = "Sign Out",
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    "Sign Out",
                                    color = Color(0xFFDC2626),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        },
                        onClick = {
                            showProfileMenu = false
                            onLogout()
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

// ── SOS Button ──────────────────────────────────────────────

@Composable
private fun SOSButton(onTriggerSOS: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(150)
    )

    // Pulsing glow for the inner circle
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFEF4444), Color(0xFFDC2626))
                ),
                RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
            .clickable(interactionSource = interactionSource, indication = null) {
                onTriggerSOS()
            }
            .padding(vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Pulsing circle
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .scale(pulseScale)
                        .background(
                            Color.White.copy(alpha = pulseAlpha),
                            CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚠️", fontSize = 36.sp)
                }
            }

            Text(
                "Trigger SOS",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Text(
                "Press to send emergency alert",
                color = Color(0xFFFFCDD2),
                fontSize = 14.sp
            )
        }
    }
}

// ── Contacts card ───────────────────────────────────────────

@Composable
private fun ContactsSection(contacts: List<String>, onEditContacts: () -> Unit) {
    DashboardCard(onClick = onEditContacts) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFDBEAFE), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("👥", fontSize = 22.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Emergency Contacts",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF111827)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${contacts.size} contact${if (contacts.size != 1) "s" else ""} configured",
                    color = Color(0xFF6B7280),
                    fontSize = 13.sp
                )

                if (contacts.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        contacts.take(3).forEach {
                            Chip(it, Color(0xFFEFF6FF), Color(0xFF1D4ED8))
                        }
                        if (contacts.size > 3) {
                            Chip("+${contacts.size - 3} more", Color(0xFFF3F4F6), Color(0xFF4B5563))
                        }
                    }
                }
            }
        }
    }
}

// ── Config card ─────────────────────────────────────────────

@Composable
private fun ConfigSection(
    voicePhrase: String,
    gestureType: String,
    onEditConfig: () -> Unit
) {
    DashboardCard(onClick = onEditConfig) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFF3E8FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("⚙️", fontSize = 22.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Trigger Configuration",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF111827)
                )

                Spacer(Modifier.height(10.dp))

                // Voice row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF7C3AED), CircleShape)
                    )
                    Text(
                        "Voice: \"$voicePhrase\"",
                        color = Color(0xFF6B7280),
                        fontSize = 14.sp
                    )
                }

                Spacer(Modifier.height(6.dp))

                // Gesture row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF7C3AED), CircleShape)
                    )
                    Text(
                        "Gesture: ${if (gestureType == "double-tap") "Back Tap (Double)" else "Device Shake"}",
                        color = Color(0xFF6B7280),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// ── How Hybrid SOS Works ────────────────────────────────────

@Composable
private fun HybridInfoSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                ),
                RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            // Shield icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🛡️", fontSize = 22.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "How ResQ Works",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(12.dp))

                HybridInfoItem(
                    checkColor = Color(0xFF4ADE80),
                    label = "Online:",
                    detail = "Instant alerts via internet to all contacts with GPS location and audio clip"
                )
                Spacer(Modifier.height(8.dp))
                HybridInfoItem(
                    checkColor = Color(0xFFFBBF24),
                    label = "Offline (Calls First):",
                    detail = "Calls emergency contacts; if someone answers, sends GPS via SMS to them only"
                )
                Spacer(Modifier.height(8.dp))
                HybridInfoItem(
                    checkColor = Color(0xFF60A5FA),
                    label = "No Answer:",
                    detail = "Converts audio to text and sends SMS with GPS + transcript to all contacts"
                )
                Spacer(Modifier.height(8.dp))
                HybridInfoItem(
                    checkColor = Color(0xFFC084FC),
                    label = "Smart Routing:",
                    detail = "Automatically switches between internet and cellular based on connectivity"
                )
            }
        }
    }
}

@Composable
private fun HybridInfoItem(checkColor: Color, label: String, detail: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("✓", color = checkColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Column {
            Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(detail, color = Color(0xFFCBD5E1), fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}

// ── Shared components ───────────────────────────────────────

@Composable
private fun DashboardCard(
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(20.dp),
        content = content
    )
}

@Composable
private fun StatusChip(icon: String, text: String, bg: Color, fg: Color) {
    Row(
        modifier = Modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(icon, fontSize = 14.sp)
        Text(text, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun Chip(text: String, bg: Color = Color(0xFFEFF6FF), fg: Color = Color(0xFF1D4ED8)) {
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 12.sp, color = fg)
    }
}

// ── Bottom Navigation Bar ───────────────────────────────────

private data class NavItem(val label: String, val icon: ImageVector)

@Composable
private fun BottomNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onSettingsClick: () -> Unit
) {
    val items = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Profile", Icons.Default.Person),
        NavItem("Settings", Icons.Default.Settings)
    )

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = {
                    onTabSelected(index)
                    if (index == 2) onSettingsClick()
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        item.label,
                        fontSize = 11.sp,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF2563EB),
                    selectedTextColor = Color(0xFF2563EB),
                    unselectedIconColor = Color(0xFF9CA3AF),
                    unselectedTextColor = Color(0xFF9CA3AF),
                    indicatorColor = Color(0xFFDBEAFE)
                )
            )
        }
    }
}
