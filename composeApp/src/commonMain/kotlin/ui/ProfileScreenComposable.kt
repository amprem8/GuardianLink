package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

data class ProfileUiState(
    val userName: String,
    val userEmail: String,
    val phoneNumber: String,
    val safePin: String,
    val continuousMonitoring: Boolean,
    val voiceChoice: Boolean,
)

data class ProfileActions(
    val onSavePin: (String) -> Unit,
    val onSetContinuousMonitoring: (Boolean) -> Unit,
    val onSetVoiceChoice: (Boolean) -> Unit,
    val onBack: () -> Unit,
)

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    actions: ProfileActions,
) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                focusManager.clearFocus()
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Gradient hero header ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFF1E40AF), Color(0xFF7C3AED))))
            ) {
                Column {
                    // Back button row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = actions.onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                        Text("My Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Avatar + name + contact
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.size(104.dp).background(Color.White.copy(alpha = 0.15f), CircleShape))
                            Box(
                                modifier = Modifier.size(88.dp).shadow(20.dp, CircleShape).background(Color.White, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    state.userName.firstOrNull()?.uppercase() ?: "U",
                                    color = Color(0xFF2563EB),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 36.sp,
                                )
                            }
                        }
                        Text(state.userName.ifEmpty { "User" }, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                        if (state.userEmail.isNotEmpty()) {
                            Text(state.userEmail, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                        if (state.phoneNumber.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
                                    .padding(horizontal = 14.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(Icons.Filled.Phone, null, tint = Color.White, modifier = Modifier.size(13.dp))
                                Text(state.phoneNumber, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // ── Cards ──
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(Modifier.height(4.dp))
                MonitoringTogglesCard(
                    continuousMonitoring = state.continuousMonitoring,
                    voiceChoice = state.voiceChoice,
                    onSetContinuousMonitoring = actions.onSetContinuousMonitoring,
                    onSetVoiceChoice = actions.onSetVoiceChoice,
                )
                SafePinCard(currentPin = state.safePin, onSavePin = actions.onSavePin)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ── Monitoring Toggles Card ─────────────────────────────────

@Composable
fun MonitoringTogglesCard(
    continuousMonitoring: Boolean,
    voiceChoice: Boolean,
    onSetContinuousMonitoring: (Boolean) -> Unit,
    onSetVoiceChoice: (Boolean) -> Unit,
) {
    var showDisableMonitoringDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF7C3AED))), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.MonitorHeart, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Text("Monitoring Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF111827))
            }

            Spacer(Modifier.height(20.dp))

            ModernToggleRow(
                icon = Icons.Filled.MonitorHeart,
                iconBg = Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF06B6D4))),
                title = "Continuous Monitoring",
                subtitle = "24/7 background trigger detection",
                badgeColor = Color(0xFF15803D),
                checked = continuousMonitoring,
                onCheckedChange = { enabled ->
                    if (continuousMonitoring && !enabled) {
                        showDisableMonitoringDialog = true
                    } else {
                        onSetContinuousMonitoring(enabled)
                    }
                },
                checkedTrackColor = Color(0xFF2563EB),
            )

            AnimatedVisibility(
                visible = continuousMonitoring,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(Icons.Filled.Battery5Bar, null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("Battery-optimised 24/7 mode", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF1E40AF))
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color(0xFFF1F5F9))

            ModernToggleRow(
                icon = Icons.Filled.Mic,
                iconBg = Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFFEC4899))),
                title = "Voice Choice",
                subtitle = "Hands-free voice-triggered SOS",
                badgeColor = Color(0xFF7C3AED),
                checked = voiceChoice,
                onCheckedChange = onSetVoiceChoice,
                checkedTrackColor = Color(0xFF7C3AED),
            )
        }
    }

    if (showDisableMonitoringDialog) {
        DisableContinuousMonitoringDialog(
            onDismiss = { showDisableMonitoringDialog = false },
            onConfirmTurnOff = {
                showDisableMonitoringDialog = false
                onSetContinuousMonitoring(false)
            },
        )
    }
}

@Composable
private fun DisableContinuousMonitoringDialog(
    onDismiss: () -> Unit,
    onConfirmTurnOff: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Turn off continuous monitoring?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                )
                Text(
                    text = "If this stays ON, ResQ keeps real-time monitoring active in the background 24/7, works with screen on/off, and remains available even when the app UI is not open.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFF4B5563),
                )
                Text(
                    text = "This mode is designed to be battery-optimised and should not noticeably impact normal performance.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = Color(0xFF6B7280),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE5E7EB),
                            contentColor = Color(0xFF111827),
                        ),
                    ) {
                        Text("Keep On", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onConfirmTurnOff,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDC2626),
                            contentColor = Color.White,
                        ),
                    ) {
                        Text("Turn Off", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernToggleRow(
    icon: ImageVector,
    iconBg: Brush,
    title: String,
    subtitle: String,
    badgeColor: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    checkedTrackColor: Color,
) {
    val scale by animateFloatAsState(
        if (checked) 1f else 0.97f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "rowScale"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(if (checked) Color(0xFFF8FAFF) else Color(0xFFFAFAFA))
            .border(1.dp, if (checked) checkedTrackColor.copy(alpha = 0.2f) else Color(0xFFF1F5F9), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(iconBg, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF111827))
            }
            Text(subtitle, fontSize = 12.sp, color = Color(0xFF6B7280), lineHeight = 16.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = checkedTrackColor,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFD1D5DB),
            ),
        )
    }
}

// ── Safe PIN Card (tappable entry point) ────────────────────

@Composable
private fun SafePinCard(currentPin: String, onSavePin: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var pinSaved by remember { mutableStateOf(false) }

    // Auto-dismiss success banner after 3 s
    LaunchedEffect(pinSaved) {
        if (pinSaved) {
            delay(3000)
            pinSaved = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) { showDialog = true },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // ── Row 1 – icon + text + arrow ──────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Gradient shield icon
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF059669), Color(0xFF10B981))),
                            RoundedCornerShape(14.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (currentPin.isNotEmpty()) Icons.Filled.Lock else Icons.Filled.LockOpen,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        "Safe PIN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF111827),
                    )
                    Text(
                        if (currentPin.isNotEmpty()) "PIN configured · tap to change" else "Tap to set your 4-digit PIN",
                        fontSize = 12.sp,
                        color = if (currentPin.isNotEmpty()) Color(0xFF15803D) else Color(0xFF6B7280),
                        lineHeight = 16.sp,
                    )
                }

                // Arrow or badge
                if (currentPin.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFDCFCE7), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text("✓ SET", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                    }
                }
                Icon(Icons.Filled.ChevronRight, null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(20.dp))
            }

            // ── Row 2 – 4 PIN indicator dots ────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                if (currentPin.isNotEmpty())
                                    Brush.linearGradient(listOf(Color(0xFF059669), Color(0xFF10B981)))
                                else
                                    Brush.linearGradient(listOf(Color(0xFFE5E7EB), Color(0xFFD1D5DB))),
                                CircleShape,
                            ),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    if (currentPin.isNotEmpty()) "Protected" else "Not configured",
                    fontSize = 11.sp,
                    color = if (currentPin.isNotEmpty()) Color(0xFF15803D) else Color(0xFF9CA3AF),
                    fontWeight = FontWeight.Medium,
                )
            }

            // ── Success banner ────────────────────────────────────────
            AnimatedVisibility(
                visible = pinSaved,
                enter = fadeIn(tween(300)) + expandVertically(),
                exit = fadeOut(tween(300)) + shrinkVertically(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF0FDF4), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier.size(22.dp).background(Color(0xFF15803D), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(13.dp))
                    }
                    Text("Safe PIN saved successfully!", color = Color(0xFF15803D), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // ── Info hint ─────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFFBEB), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("🔒", fontSize = 13.sp)
                Text(
                    "Required to cancel an active SOS alert inside the app.",
                    color = Color(0xFF92400E),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
            }
        }
    }

    // ── PIN configuration dialog ─────────────────────────────
    if (showDialog) {
        SafePinDialog(
            hasExistingPin = currentPin.isNotEmpty(),
            onDismiss = { showDialog = false },
            onSave = { newPin ->
                onSavePin(newPin)
                showDialog = false
                pinSaved = true
            },
        )
    }
}

// ── Safe PIN full-screen dialog with numeric keypad ─────────

@Composable
private fun SafePinDialog(
    hasExistingPin: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    // Two-step: first enter new PIN, then confirm it
    var step by remember { mutableStateOf(0) } // 0 = enter, 1 = confirm
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    var shakeState by remember { mutableStateOf(false) }

    // Shake animation
    val shakeOffset by animateFloatAsState(
        targetValue = if (shakeState) 10f else 0f,
        animationSpec = spring(dampingRatio = 0.2f, stiffness = 800f),
        finishedListener = { shakeState = false },
        label = "shake",
    )

    val currentInput = if (step == 0) newPin else confirmPin
    val setCurrentInput: (String) -> Unit = { v ->
        errorMsg = ""
        if (step == 0) newPin = v else confirmPin = v
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = true),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDismiss() },
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { /* consume */ },
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = Color.White,
                shadowElevation = 24.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 28.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    // Handle bar
                    Box(
                        modifier = Modifier
                            .size(40.dp, 4.dp)
                            .background(Color(0xFFE5E7EB), RoundedCornerShape(2.dp)),
                    )
                    Spacer(Modifier.height(20.dp))

                    // Icon
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF059669), Color(0xFF10B981))),
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Shield, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.height(14.dp))

                    // Title
                    Text(
                        text = if (step == 0) {
                            if (hasExistingPin) "Change Safe PIN" else "Set Safe PIN"
                        } else "Confirm Safe PIN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF111827),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (step == 0) "Enter a new 4-digit PIN" else "Re-enter your PIN to confirm",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280),
                    )
                    Spacer(Modifier.height(28.dp))

                    // 4 dot display
                    Row(
                        modifier = Modifier.offset(x = shakeOffset.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(4) { idx ->
                            val filled = idx < currentInput.length
                            val dotColor by animateColorAsState(
                                targetValue = when {
                                    errorMsg.isNotEmpty() -> Color(0xFFDC2626)
                                    filled               -> Color(0xFF059669)
                                    else                 -> Color(0xFFD1D5DB)
                                },
                                label = "dot$idx",
                            )
                            val dotSize by animateFloatAsState(
                                targetValue = if (filled) 22f else 18f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "dotSize$idx",
                            )
                            Box(
                                modifier = Modifier
                                    .size(dotSize.dp)
                                    .background(dotColor, CircleShape),
                            )
                        }
                    }

                    // Error
                    AnimatedVisibility(
                        visible = errorMsg.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Column {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                errorMsg,
                                color = Color(0xFFDC2626),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // Numeric keypad 3×4
                    val keys = listOf("1","2","3","4","5","6","7","8","9","","0","⌫")
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        keys.chunked(3).forEach { rowKeys ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                rowKeys.forEach { key ->
                                    if (key.isEmpty()) {
                                        Spacer(Modifier.weight(1f))
                                    } else {
                                        PinKeyButton(
                                            label = key,
                                            modifier = Modifier.weight(1f),
                                            isDelete = key == "⌫",
                                            onClick = {
                                                if (key == "⌫") {
                                                    if (currentInput.isNotEmpty()) setCurrentInput(currentInput.dropLast(1))
                                                } else if (currentInput.length < 4) {
                                                    val next = currentInput + key
                                                    setCurrentInput(next)
                                                    // Auto-advance
                                                    if (next.length == 4) {
                                                        if (step == 0) {
                                                            step = 1
                                                        } else {
                                                            if (next == newPin) {
                                                                onSave(newPin)
                                                            } else {
                                                                errorMsg = "PINs don't match — try again"
                                                                shakeState = true
                                                                confirmPin = ""
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Cancel / back row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (step == 1) {
                            Button(
                                onClick = { step = 0; newPin = ""; confirmPin = ""; errorMsg = "" },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6)),
                            ) {
                                Text("Back", color = Color(0xFF374151), fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                        ) {
                            Text("Cancel", color = Color(0xFFDC2626), fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

// ── Numeric keypad button ────────────────────────────────────

@Composable
private fun PinKeyButton(
    label: String,
    modifier: Modifier = Modifier,
    isDelete: Boolean = false,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "keyScale")

    Box(
        modifier = modifier
            .aspectRatio(1.6f)
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isDelete) Color(0xFFFEE2E2) else Color(0xFFF3F4F6))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isDelete) {
            Icon(
                Icons.AutoMirrored.Filled.Backspace,
                null,
                tint = Color(0xFFDC2626),
                modifier = Modifier.size(22.dp),
            )
        } else {
            Text(
                label,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827),
            )
        }
    }
}
