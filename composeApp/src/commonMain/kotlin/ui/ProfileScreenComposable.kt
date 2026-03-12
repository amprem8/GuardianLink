package ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
                badge = if (continuousMonitoring) "ACTIVE" else null,
                badgeColor = Color(0xFF15803D),
                checked = continuousMonitoring,
                onCheckedChange = onSetContinuousMonitoring,
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
                            Text(
                                "Uses Android WorkManager & JobScheduler for intelligent wake cycles. " +
                                        "Average extra drain: <1% per hour. Performance impact: minimal.",
                                fontSize = 11.sp,
                                color = Color(0xFF3B82F6),
                                lineHeight = 15.sp,
                            )
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
                badge = if (voiceChoice) "ON" else null,
                badgeColor = Color(0xFF7C3AED),
                checked = voiceChoice,
                onCheckedChange = onSetVoiceChoice,
                checkedTrackColor = Color(0xFF7C3AED),
            )
        }
    }
}

@Composable
private fun ModernToggleRow(
    icon: ImageVector,
    iconBg: Brush,
    title: String,
    subtitle: String,
    badge: String?,
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
                if (badge != null) {
                    Box(
                        modifier = Modifier.background(badgeColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp)).padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(badge, fontSize = 9.sp, color = badgeColor, fontWeight = FontWeight.Bold)
                    }
                }
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

// ── Safe PIN Card ───────────────────────────────────────────

@Composable
private fun SafePinCard(currentPin: String, onSavePin: (String) -> Unit) {
    var pinString by remember(currentPin) { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(currentPin.isEmpty()) }
    var pinError by remember { mutableStateOf("") }
    var pinSaved by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Brush.linearGradient(listOf(Color(0xFF059669), Color(0xFF10B981))), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Shield, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Safe PIN", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF111827))
                    Text("Required to cancel an active SOS alert", fontSize = 12.sp, color = Color(0xFF6B7280), lineHeight = 16.sp)
                }
                if (!isEditing && currentPin.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFFF3F4F6), CircleShape)
                            .clip(CircleShape)
                            .clickable { isEditing = true; pinString = ""; confirmPin = ""; pinError = ""; pinSaved = false },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Edit, "Edit", tint = Color(0xFF6B7280), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Success banner
            AnimatedVisibility(visible = pinSaved, enter = fadeIn(), exit = fadeOut()) {
                Column {
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
                            modifier = Modifier.size(24.dp).background(Color(0xFF15803D), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        Text("Safe PIN saved successfully!", color = Color(0xFF15803D), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            AnimatedContent(targetState = isEditing, label = "pinMode") { editing ->
                if (editing) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // New PIN dots
                        PinDotsField(
                            label = if (currentPin.isEmpty()) "Set your 4-digit PIN" else "New 4-digit PIN",
                            value = pinString,
                            showDigits = showPin,
                            onValueChange = { v ->
                                if (v.length <= 4 && v.all { it.isDigit() }) { pinString = v; pinError = "" }
                            },
                            onToggleVisibility = { showPin = !showPin },
                        )
                        // Confirm PIN dots
                        PinDotsField(
                            label = "Confirm PIN",
                            value = confirmPin,
                            showDigits = false,
                            onValueChange = { v ->
                                if (v.length <= 4 && v.all { it.isDigit() }) { confirmPin = v; pinError = "" }
                            },
                            onToggleVisibility = null,
                        )

                        if (pinError.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().background(Color(0xFFFEF2F2), RoundedCornerShape(10.dp)).padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text("⚠️", fontSize = 14.sp)
                                Text(pinError, color = Color(0xFFDC2626), fontSize = 13.sp)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (currentPin.isNotEmpty()) {
                                Button(
                                    onClick = { isEditing = false; pinString = ""; confirmPin = ""; pinError = "" },
                                    modifier = Modifier.weight(1f).height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6)),
                                ) { Text("Cancel", color = Color(0xFF374151), fontWeight = FontWeight.Medium) }
                            }
                            Button(
                                onClick = {
                                    when {
                                        pinString.length != 4 -> pinError = "PIN must be exactly 4 digits"
                                        confirmPin != pinString -> pinError = "PINs do not match"
                                        else -> { onSavePin(pinString); isEditing = false; confirmPin = ""; pinError = ""; pinSaved = true }
                                    }
                                },
                                modifier = Modifier.weight(1f).height(46.dp),
                                enabled = pinString.length == 4 && confirmPin.isNotEmpty(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669), disabledContainerColor = Color(0xFFD1D5DB)),
                            ) {
                                Icon(Icons.Filled.Lock, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Save PIN", color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFBEB), RoundedCornerShape(12.dp)).border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp)).padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("🔒", fontSize = 14.sp)
                            Text(
                                "Your PIN is stored locally and never shared. Keep it memorable — you'll need it to cancel a live SOS.",
                                color = Color(0xFF92400E), fontSize = 12.sp, lineHeight = 17.sp,
                            )
                        }
                    }
                } else if (currentPin.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                repeat(4) {
                                    Box(
                                        modifier = Modifier.size(16.dp).background(
                                            Brush.linearGradient(listOf(Color(0xFF059669), Color(0xFF10B981))), CircleShape
                                        )
                                    )
                                }
                            }
                            Text("PIN configured", fontSize = 13.sp, color = Color(0xFF15803D), fontWeight = FontWeight.SemiBold)
                            Box(
                                modifier = Modifier.background(Color(0xFFDCFCE7), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("✓ SECURE", fontSize = 9.sp, color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
                            }
                        }
                        Text("Tap the edit button to change your PIN.", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                    }
                }
            }
        }
    }
}

// ── 4-dot PIN field ─────────────────────────────────────────

@Composable
private fun PinDotsField(
    label: String,
    value: String,
    showDigits: Boolean,
    onValueChange: (String) -> Unit,
    onToggleVisibility: (() -> Unit)?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, fontSize = 13.sp, color = Color(0xFF374151), fontWeight = FontWeight.Medium)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            repeat(4) { idx ->
                val filled = idx < value.length
                val isActive = idx == value.length
                val borderColor by animateColorAsState(
                    targetValue = when {
                        isActive -> Color(0xFF2563EB)
                        filled   -> Color(0xFF10B981)
                        else     -> Color(0xFFD1D5DB)
                    },
                    label = "pinBorder$idx"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .background(if (filled) Color(0xFFF0FDF4) else Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
                        .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        filled  -> Text(if (showDigits) value[idx].toString() else "●", fontSize = if (showDigits) 20.sp else 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        isActive -> Box(modifier = Modifier.size(2.dp, 24.dp).background(Color(0xFF2563EB)))
                    }
                }
            }
        }

        // Invisible driver field (0dp height keeps keyboard working)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(1.dp),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
        )

        if (onToggleVisibility != null) {
            Row(
                modifier = Modifier.align(Alignment.End).clickable { onToggleVisibility() }.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    if (showDigits) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    null, tint = Color(0xFF6B7280), modifier = Modifier.size(16.dp)
                )
                Text(if (showDigits) "Hide PIN" else "Show PIN", fontSize = 12.sp, color = Color(0xFF6B7280))
            }
        }
    }
}
