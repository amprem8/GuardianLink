package ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import contacts.ContactsPermissionState
import kotlinx.coroutines.delay
import model.DeviceContact
import model.EmergencyContact

// ── Constants ───────────────────────────────────────────────
private const val MIN_CONTACTS = 3
private const val MAX_CONTACTS = 10

// ── Main Screen ─────────────────────────────────────────────

@Composable
fun EmergencyContactsScreen(
    contacts: List<EmergencyContact>,
    error: String,
    showAddDialog: Boolean,
    deviceContacts: List<DeviceContact>,
    isLoadingContacts: Boolean,
    permissionState: ContactsPermissionState,
    verifiedPhone: String = "",
    onAddContact: (name: String, phone: String) -> Unit,
    onAddDeviceContact: (DeviceContact) -> Unit,
    onRemoveContact: (String) -> Unit,
    onToggleGPS: (String) -> Unit,
    onToggleAudio: (String) -> Unit,
    onShowAddDialog: (Boolean) -> Unit,
    onDismissError: () -> Unit,
    onLoadDeviceContacts: () -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val hasEnoughContacts = contacts.size >= MIN_CONTACTS

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { focusManager.clearFocus() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Only leave room for the bottom button when it's visible
                .padding(bottom = if (hasEnoughContacts) 88.dp else 0.dp)
        ) {
            // ── Top bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF374151),
                    )
                }
                Text(
                    "Emergency Contacts",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                )
            }

            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE5E7EB))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // ── Subtitle ──
                Text(
                    "Add $MIN_CONTACTS–$MAX_CONTACTS trusted contacts who will be alerted in case of emergency",
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )

                // ── India-only notice ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEFF6FF), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        "🇮🇳  India Only: Currently accepting Indian mobile numbers " +
                                "(10 digits starting with 6, 7, 8, or 9)",
                        color = Color(0xFF1E40AF),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                    )
                }

                // ── Error banner (moved to overlay) ──

                // ── Contact cards ──
                contacts.forEach { contact ->
                    ContactCard(
                        contact = contact,
                        onRemove = { onRemoveContact(contact.id) },
                        onToggleGPS = { onToggleGPS(contact.id) },
                        onToggleAudio = { onToggleAudio(contact.id) },
                    )
                }

                // ── Add contact button ──
                if (contacts.size < MAX_CONTACTS) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, Color(0xFFD1D5DB), RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onShowAddDialog(true) }
                            .padding(vertical = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.PersonAdd,
                                contentDescription = "Add",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(32.dp),
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Add Emergency Contact",
                                color = Color(0xFF6B7280),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                "${contacts.size}/$MAX_CONTACTS contacts added",
                                color = Color(0xFF9CA3AF),
                                fontSize = 13.sp,
                            )
                        }
                    }
                }

                // ── Minimum notice ──
                if (contacts.size < MIN_CONTACTS) {
                    Text(
                        "⚠️  Add at least $MIN_CONTACTS contacts to continue",
                        color = Color(0xFF92400E),
                        fontSize = 13.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFEF3C7), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }

        // ── Continue button (fixed bottom) — only visible when MIN_CONTACTS reached ──
        if (hasEnoughContacts) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
                                ),
                                RoundedCornerShape(14.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                "Continue",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                            )
                        }
                    }
                }
            }
        }

        // ── Slide-in error toast (top-right overlay) ──
        // Auto-dismiss after 3 seconds
        LaunchedEffect(error) {
            if (error.isNotEmpty()) {
                delay(3000)
                onDismissError()
            }
        }

        AnimatedVisibility(
            visible = error.isNotEmpty(),
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 350)
            ) + fadeIn(animationSpec = tween(350)),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300)
            ) + fadeOut(animationSpec = tween(300)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 64.dp, end = 12.dp),
        ) {
            Row(
                modifier = Modifier
                    .shadow(8.dp, RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFF1F2), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    error,
                    color = Color(0xFFB91C1C),
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Dismiss",
                    tint = Color(0xFFB91C1C),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onDismissError() },
                )
            }
        }

        // ── Add-contact modal overlay ──
        if (showAddDialog) {
            AddContactDialog(
                deviceContacts = deviceContacts,
                isLoadingContacts = isLoadingContacts,
                permissionState = permissionState,
                verifiedPhone = verifiedPhone,
                onAdd = onAddContact,
                onAddDevice = onAddDeviceContact,
                onDismiss = { onShowAddDialog(false) },
                onLoadDeviceContacts = onLoadDeviceContacts,
            )
        }
    }
}

// ── Contact Card ────────────────────────────────────────────

@Composable
private fun ContactCard(
    contact: EmergencyContact,
    onRemove: () -> Unit,
    onToggleGPS: () -> Unit,
    onToggleAudio: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        // Name + phone + delete
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFFDBEAFE), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = contact.name.firstOrNull()?.uppercase() ?: "?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF2563EB),
                    )
                }
                Column {
                    Text(
                        contact.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color(0xFF111827),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            Icons.Filled.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF6B7280),
                        )
                        Text(
                            contact.phone,
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280),
                        )
                    }
                }
            }

            IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Remove",
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            thickness = 0.5.dp,
            color = Color(0xFFF3F4F6),
        )

        // Toggles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ToggleRow(
                icon = { Icon(Icons.Filled.LocationOn, null, Modifier.size(14.dp), tint = Color(0xFF6B7280)) },
                label = "GPS",
                checked = contact.includeGPS,
                onToggle = onToggleGPS,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(12.dp))
            ToggleRow(
                icon = { Icon(Icons.Filled.Mic, null, Modifier.size(14.dp), tint = Color(0xFF6B7280)) },
                label = "Audio",
                checked = contact.includeAudio,
                onToggle = onToggleAudio,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ToggleRow(
    icon: @Composable () -> Unit,
    label: String,
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        icon()
        Text(
            label,
            fontSize = 12.sp,
            color = Color(0xFF374151),
            modifier = Modifier.weight(1f, fill = false),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF2563EB),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFD1D5DB),
            ),
            modifier = Modifier
                .height(18.dp)
                .graphicsLayer(scaleX = 0.75f, scaleY = 0.75f),
        )
    }
}

// ── Add Contact Dialog ──────────────────────────────────────

@Composable
private fun AddContactDialog(
    deviceContacts: List<DeviceContact>,
    isLoadingContacts: Boolean,
    permissionState: ContactsPermissionState,
    verifiedPhone: String = "",
    onAdd: (name: String, phone: String) -> Unit,
    onAddDevice: (DeviceContact) -> Unit,
    onDismiss: () -> Unit,
    onLoadDeviceContacts: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var isManualMode by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf("") }

    // Normalize verified phone to last 10 digits for comparison
    val verifiedDigits = verifiedPhone.replace("\\D".toRegex(), "").takeLast(10)

    // Full-screen overlay with fade
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { focusManager.clearFocus(force = true) },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(24.dp),
        ) {
            // ── Header ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Add Contact", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF111827))
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Close, "Close", tint = Color(0xFF6B7280), modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Modern animated pill toggle ──
            ModernPillToggle(
                isManualMode = isManualMode,
                onModeChange = { manual ->
                    isManualMode = manual
                    localError = ""
                    if (!manual && permissionState.isGranted) {
                        onLoadDeviceContacts()
                    }
                },
            )

            Spacer(Modifier.height(20.dp))

            // ── Animated content transition ──
            AnimatedContent(
                targetState = isManualMode,
                transitionSpec = {
                    if (targetState) {
                        (slideInHorizontally { -it } + fadeIn(tween(250))) togetherWith
                                (slideOutHorizontally { it } + fadeOut(tween(200)))
                    } else {
                        (slideInHorizontally { it } + fadeIn(tween(250))) togetherWith
                                (slideOutHorizontally { -it } + fadeOut(tween(200)))
                    }
                },
                label = "mode-transition",
            ) { manual ->
                if (manual) {
                    ManualEntryFields(
                        name = name,
                        phone = phone,
                        error = localError,
                        onNameChange = { if (it.length <= 10) name = it },
                        onPhoneChange = { phone = it.filter { c -> c.isDigit() }.take(10) },
                    )
                } else {
                    if (!permissionState.isGranted) {
                        PermissionRequest(onGrant = { permissionState.launchRequest() })
                    } else {
                        LaunchedEffect(permissionState.isGranted) {
                            onLoadDeviceContacts()
                        }
                        ContactPickerList(
                            contacts = deviceContacts,
                            isLoading = isLoadingContacts,
                            search = search,
                            verifiedDigits = verifiedDigits,
                            onSearchChange = { search = it },
                            onPick = { dc ->
                                val dcDigits = dc.phone.replace("\\D".toRegex(), "").takeLast(10)
                                if (dcDigits == verifiedDigits && verifiedDigits.isNotEmpty()) {
                                    localError = "Your verified number cannot be added as an emergency contact"
                                } else {
                                    onAddDevice(dc)
                                    onDismiss()
                                }
                            },
                            onError = { msg -> localError = msg },
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Error for import mode ──
            if (!isManualMode && localError.isNotBlank()) {
                Text(localError, color = Color(0xFFDC2626), fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
            }

            // ── Buttons (only for manual mode) ──
            if (isManualMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Cancel
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder(true),
                    ) {
                        Text("Cancel", color = Color(0xFF374151))
                    }

                    // Add
                    Button(
                        onClick = {
                            when {
                                name.isBlank() -> localError = "Please enter contact name"
                                name.length > 10 -> localError = "Name must be 10 characters or less"
                                phone.isBlank() -> localError = "Please enter phone number"
                                phone.length != 10 -> localError = "Phone number must be exactly 10 digits"
                                phone == verifiedDigits && verifiedDigits.isNotEmpty() ->
                                    localError = "Your verified number cannot be added as an emergency contact"
                                else -> {
                                    localError = ""
                                    onAdd(name.trim(), phone.trim())
                                }
                            }
                        },
                        enabled = name.isNotBlank() && phone.length == 10,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            disabledContainerColor = Color(0xFFD1D5DB),
                        ),
                    ) {
                        Text("Add Contact", color = Color.White)
                    }
                }
            }
        }
    }
}

// ── Modern Pill Toggle ──────────────────────────────────────

@Composable
private fun ModernPillToggle(
    isManualMode: Boolean,
    onModeChange: (Boolean) -> Unit,
) {
    val pillGradientLeft = listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
    val pillGradientRight = listOf(Color(0xFF7C3AED), Color(0xFF2563EB))

    // Animate the pill offset (0 = left / Manual, 1 = right / Contacts)
    val animatedOffset by animateFloatAsState(
        targetValue = if (isManualMode) 0f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "pill-offset",
    )

    // Subtle scale bounce on the active pill
    val leftScale by animateFloatAsState(
        targetValue = if (isManualMode) 1f else 0.95f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "left-scale",
    )
    val rightScale by animateFloatAsState(
        targetValue = if (!isManualMode) 1f else 0.95f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "right-scale",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(6.dp, RoundedCornerShape(26.dp))
            .background(Color(0xFFF1F5F9), RoundedCornerShape(26.dp))
            .padding(4.dp),
    ) {
        // Sliding pill indicator
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(44.dp)
                .offset(
                    x = animateDpAsState(
                        targetValue = if (isManualMode) 0.dp else (/* half width */ 160.dp),
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                        label = "pill-x",
                    ).value,
                )
                .shadow(4.dp, RoundedCornerShape(22.dp))
                .background(
                    Brush.horizontalGradient(
                        if (isManualMode) pillGradientLeft else pillGradientRight
                    ),
                    RoundedCornerShape(22.dp),
                ),
        )

        // Text labels on top
        Row(modifier = Modifier.fillMaxSize()) {
            // Manual tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(22.dp))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { onModeChange(true) }
                    .graphicsLayer { scaleX = leftScale; scaleY = leftScale },
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isManualMode) Color.White else Color(0xFF94A3B8),
                    )
                    Text(
                        "Manual",
                        fontSize = 14.sp,
                        fontWeight = if (isManualMode) FontWeight.Bold else FontWeight.Medium,
                        color = if (isManualMode) Color.White else Color(0xFF64748B),
                    )
                }
            }

            // From Contacts tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(22.dp))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { onModeChange(false) }
                    .graphicsLayer { scaleX = rightScale; scaleY = rightScale },
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Icons.Filled.Contacts,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (!isManualMode) Color.White else Color(0xFF94A3B8),
                    )
                    Text(
                        "Contacts",
                        fontSize = 14.sp,
                        fontWeight = if (!isManualMode) FontWeight.Bold else FontWeight.Medium,
                        color = if (!isManualMode) Color.White else Color(0xFF64748B),
                    )
                }
            }
        }
    }
}

// ── Manual entry fields ─────────────────────────────────────

@Composable
private fun ManualEntryFields(
    name: String,
    phone: String,
    error: String,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Name
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Name", color = Color(0xFF374151), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(
                    "${name.length}/10",
                    fontSize = 12.sp,
                    color = if (name.length >= 10) Color(0xFFDC2626) else Color(0xFF9CA3AF),
                )
            }
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 10) onNameChange(it) },
                singleLine = true,
                placeholder = { Text("Contact name", color = Color(0xFF9CA3AF)) },
                leadingIcon = { Icon(Icons.Filled.Person, null, tint = Color(0xFF9CA3AF)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            )
        }

        // Phone
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Phone Number", color = Color(0xFF374151), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(
                    "${phone.length}/10",
                    fontSize = 12.sp,
                    color = if (phone.length == 10) Color(0xFF16A34A) else Color(0xFF9CA3AF),
                )
            }
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { onPhoneChange(it) },
                singleLine = true,
                placeholder = { Text("9876543210", color = Color(0xFF9CA3AF)) },
                leadingIcon = {
                    Text(
                        "+91",
                        color = Color(0xFF374151),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 12.dp),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            )
            Text("Enter exactly 10-digit Indian mobile number", color = Color(0xFF9CA3AF), fontSize = 12.sp)
        }

        // Error
        if (error.isNotBlank()) {
            Text(error, color = Color(0xFFDC2626), fontSize = 13.sp)
        }
    }
}

// ── Permission request ──────────────────────────────────────

@Composable
private fun PermissionRequest(onGrant: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF7ED), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFFED7AA), RoundedCornerShape(12.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Filled.Person,
            contentDescription = null,
            tint = Color(0xFFEA580C),
            modifier = Modifier.size(40.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Contact Access Required",
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = Color(0xFF9A3412),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Grant permission to read your contacts so you can quickly add trusted people.",
            color = Color(0xFFEA580C),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onGrant,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
        ) {
            Text("Grant Permission", color = Color.White)
        }
    }
}

// ── Contact picker list ─────────────────────────────────────

@Composable
private fun ContactPickerList(
    contacts: List<DeviceContact>,
    isLoading: Boolean,
    search: String,
    verifiedDigits: String = "",
    onSearchChange: (String) -> Unit,
    onPick: (DeviceContact) -> Unit,
    onError: (String) -> Unit = {},
) {
    val filtered = if (search.isBlank()) contacts
    else contacts.filter {
        it.name.contains(search, ignoreCase = true) ||
                it.phone.contains(search)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = search,
            onValueChange = onSearchChange,
            singleLine = true,
            placeholder = { Text("Search contacts…", color = Color(0xFF9CA3AF)) },
            leadingIcon = { Icon(Icons.Filled.Search, null, tint = Color(0xFF9CA3AF)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        )

        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF2563EB),
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 3.dp,
                )
                Text("Loading contacts…", color = Color(0xFF6B7280), fontSize = 14.sp)
            }
        } else if (contacts.isEmpty()) {
            Text(
                "No contacts found",
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(vertical = 24.dp).fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        } else {
            LazyColumn(modifier = Modifier.height(260.dp)) {
                items(filtered, key = { "${it.name}|${it.phone}" }) { dc ->
                    val dcDigits = dc.phone.replace("\\D".toRegex(), "").takeLast(10)
                    val isVerifiedNumber = verifiedDigits.isNotEmpty() && dcDigits == verifiedDigits
                    DeviceContactRow(
                        contact = dc,
                        isBlocked = isVerifiedNumber,
                        onPick = {
                            if (isVerifiedNumber) {
                                onError("Your verified number cannot be added as an emergency contact")
                            } else {
                                onPick(dc)
                            }
                        }
                    )
                }
                if (filtered.isEmpty()) {
                    item {
                        Text(
                            "No contacts match your search",
                            color = Color(0xFF6B7280),
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceContactRow(contact: DeviceContact, isBlocked: Boolean = false, onPick: (DeviceContact) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onPick(contact) }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(
                if (isBlocked) Color(0xFFFEE2E2) else Color(0xFFEDE9FE),
                CircleShape
            ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                contact.name.firstOrNull()?.uppercase() ?: "?",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isBlocked) Color(0xFFDC2626) else Color(0xFF7C3AED),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                contact.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isBlocked) Color(0xFF9CA3AF) else Color(0xFF111827),
            )
            Text(
                contact.phone,
                fontSize = 12.sp,
                color = if (isBlocked) Color(0xFFDC2626) else Color(0xFF6B7280),
            )
        }
        if (isBlocked) {
            Text(
                "Your number",
                fontSize = 11.sp,
                color = Color(0xFFDC2626),
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

// end of file
