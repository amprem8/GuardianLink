package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneDisabled
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.pow
import kotlin.math.round
import kotlinx.coroutines.delay
import model.EmergencyContact

// ── SOS Status Types ────────────────────────────────────────

enum class SOSStatus {
    DETECTING,
    LOCATING,
    CHECKING_NETWORK,
    ONLINE_SENDING,
    CALLING_CONTACTS,
    CONVERTING_AUDIO,
    SMS_SENDING,
    BLE_SCANNING,
    BLE_RELAYING,
    SUCCESS,
}

enum class ContactDeliveryStatus {
    PENDING, CALLING, ANSWERED, NO_ANSWER, SENT, DELIVERED
}

enum class DeliveryMethod {
    INTERNET, SMS, CALL, BLE
}

data class ContactSOSStatus(
    val contact: EmergencyContact,
    val status: ContactDeliveryStatus = ContactDeliveryStatus.PENDING,
    val method: DeliveryMethod? = null,
)

data class NearbyBLEDevice(
    val name: String,
    val distance: String,
    val isOnline: Boolean,
    val status: String, // "Found", "Connecting...", "Relaying...", "Relayed ✓"
)

// ── Main Active SOS Screen ──────────────────────────────────

@Composable
fun ActiveSOSScreen(
    contacts: List<EmergencyContact>,
    isOnline: Boolean,
    safePin: String = "",
    onCancel: () -> Unit,
) {
    var sosStatus by remember { mutableStateOf(SOSStatus.DETECTING) }
    var location by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var elapsedTime by remember { mutableIntStateOf(0) }
    var contactStatuses by remember {
        mutableStateOf(contacts.map { ContactSOSStatus(contact = it) })
    }
    var someoneAnswered by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var nearbyDevices by remember { mutableStateOf<List<NearbyBLEDevice>>(emptyList()) }

    // ── Elapsed timer ──
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            elapsedTime++
        }
    }

    // ── Simulated SOS flow ──
    LaunchedEffect(Unit) {
        // Step 1: Detecting trigger
        delay(1000)
        sosStatus = SOSStatus.LOCATING

        // Step 2: Acquiring GPS
        delay(1500)
        location = 12.9716 to 77.5946 // Mock: Bangalore
        sosStatus = SOSStatus.CHECKING_NETWORK

        // Step 3: Check connectivity
        delay(1000)

        if (isOnline) {
            // ── PATH 1: ONLINE ──
            sosStatus = SOSStatus.ONLINE_SENDING
            delay(1500)

            for (i in contacts.indices) {
                delay(500)
                contactStatuses = contactStatuses.mapIndexed { idx, cs ->
                    if (idx == i) cs.copy(status = ContactDeliveryStatus.SENT, method = DeliveryMethod.INTERNET)
                    else cs
                }
                delay(400)
                contactStatuses = contactStatuses.mapIndexed { idx, cs ->
                    if (idx == i) cs.copy(status = ContactDeliveryStatus.DELIVERED) else cs
                }
            }
            sosStatus = SOSStatus.SUCCESS
        } else {
            // ── PATH 2/3: OFFLINE ──
            // Call contacts sequentially
            sosStatus = SOSStatus.CALLING_CONTACTS
            delay(1000)

            var answeredIdx = -1
            for (i in contacts.indices) {
                contactStatuses = contactStatuses.mapIndexed { idx, cs ->
                    if (idx == i) cs.copy(status = ContactDeliveryStatus.CALLING, method = DeliveryMethod.CALL)
                    else cs
                }
                delay(2000)

                // Simulate: 30% chance someone answers
                val didAnswer = (0..9).random() > 6 && answeredIdx == -1
                if (didAnswer) {
                    answeredIdx = i
                    someoneAnswered = true
                    contactStatuses = contactStatuses.mapIndexed { idx, cs ->
                        if (idx == i) cs.copy(status = ContactDeliveryStatus.ANSWERED) else cs
                    }
                    break
                } else {
                    contactStatuses = contactStatuses.mapIndexed { idx, cs ->
                        if (idx == i) cs.copy(status = ContactDeliveryStatus.NO_ANSWER) else cs
                    }
                }
            }

            // Convert audio to text if nobody answered
            if (!someoneAnswered) {
                sosStatus = SOSStatus.CONVERTING_AUDIO
                delay(1500)
            }

            // Send SMS
            sosStatus = SOSStatus.SMS_SENDING
            delay(1000)

            if (someoneAnswered && answeredIdx != -1) {
                delay(500)
                contactStatuses = contactStatuses.mapIndexed { idx, cs ->
                    if (idx == answeredIdx) cs.copy(status = ContactDeliveryStatus.SENT, method = DeliveryMethod.SMS)
                    else cs
                }
                delay(400)
                contactStatuses = contactStatuses.mapIndexed { idx, cs ->
                    if (idx == answeredIdx) cs.copy(status = ContactDeliveryStatus.DELIVERED) else cs
                }
            } else {
                for (i in contacts.indices) {
                    delay(500)
                    contactStatuses = contactStatuses.mapIndexed { idx, cs ->
                        if (idx == i) cs.copy(status = ContactDeliveryStatus.SENT, method = DeliveryMethod.SMS)
                        else cs
                    }
                    delay(400)
                    contactStatuses = contactStatuses.mapIndexed { idx, cs ->
                        if (idx == i) cs.copy(status = ContactDeliveryStatus.DELIVERED) else cs
                    }
                }
            }

            // BLE scan (simulate — always show in offline mode)
            sosStatus = SOSStatus.BLE_SCANNING
            delay(800)
            nearbyDevices = listOf(
                NearbyBLEDevice("Priya's iPhone", "~3m", true, "Found"),
            )
            delay(600)
            nearbyDevices = nearbyDevices + NearbyBLEDevice("Rahul's Pixel", "~8m", false, "Found")
            delay(500)
            nearbyDevices = nearbyDevices + NearbyBLEDevice("Unknown Device", "~15m", true, "Found")
            delay(800)

            // Relay through online device
            sosStatus = SOSStatus.BLE_RELAYING
            nearbyDevices = nearbyDevices.mapIndexed { idx, d ->
                if (idx == 0) d.copy(status = "Connecting...") else d
            }
            delay(1000)
            nearbyDevices = nearbyDevices.mapIndexed { idx, d ->
                if (idx == 0) d.copy(status = "Relaying...") else d
            }
            delay(1200)
            nearbyDevices = nearbyDevices.mapIndexed { idx, d ->
                if (idx == 0) d.copy(status = "Relayed ✓") else d
            }
            delay(600)

            sosStatus = SOSStatus.SUCCESS
        }
    }

    // ── UI ──
    Box(modifier = Modifier.fillMaxSize()) {
        // Pulsing red background
        val infiniteTransition = rememberInfiniteTransition()
        val bgAlpha by infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFEF4444).copy(alpha = bgAlpha),
                            Color(0xFFDC2626).copy(alpha = bgAlpha),
                        )
                    )
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 48.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── Pulsing alert icon ──
            SOSPulsingIcon()

            Spacer(Modifier.height(16.dp))

            Text(
                "SOS ACTIVATED",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                letterSpacing = 2.sp,
            )

            Spacer(Modifier.height(8.dp))

            // ── Timer + mode badge ──
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Timer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("⏱", fontSize = 14.sp)
                    Text(
                        formatElapsedTime(elapsedTime),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                    )
                }

                // Mode
                Row(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        if (isOnline) "Online Mode" else "Offline Mode",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Status card ──
            StatusCard(
                sosStatus = sosStatus,
                location = location,
                contactStatuses = contactStatuses,
                someoneAnswered = someoneAnswered,
                nearbyDevices = nearbyDevices,
            )

            Spacer(Modifier.height(20.dp))

            // ── Cancel / Return button ──
            if (sosStatus != SOSStatus.SUCCESS) {
                Button(
                    onClick = { showCancelDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                    ),
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("I am Safe (Cancel Alert)", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Button(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF111827), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Return to Dashboard", color = Color(0xFF111827), fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // ── Cancel confirmation overlay ──
        if (showCancelDialog) {
            CancelPinDialog(
                safePin = safePin,
                onDismiss = { showCancelDialog = false },
                onConfirm = onCancel,
            )
        }
    }
}

// ── Pulsing icon ────────────────────────────────────────────

@Composable
private fun SOSPulsingIcon() {
    val transition = rememberInfiniteTransition()
    val pulseScale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    Box(contentAlignment = Alignment.Center) {
        // Outer pulse ring
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(pulseScale)
                .background(Color.White.copy(alpha = pulseAlpha), CircleShape),
        )
        // Inner ring
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = "SOS",
                tint = Color.White,
                modifier = Modifier.size(40.dp),
            )
        }
    }
}

// ── Status Card ─────────────────────────────────────────────

@Composable
private fun StatusCard(
    sosStatus: SOSStatus,
    location: Pair<Double, Double>?,
    contactStatuses: List<ContactSOSStatus>,
    someoneAnswered: Boolean,
    nearbyDevices: List<NearbyBLEDevice>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp))
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Status header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        if (sosStatus == SOSStatus.SUCCESS) Color(0xFF22C55E) else Color(0xFF3B82F6),
                        CircleShape,
                    ),
            )
            Text(
                "Status",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF111827),
            )
        }

        // Status message
        Text(
            getStatusMessage(sosStatus, someoneAnswered),
            color = Color(0xFF374151),
            fontSize = 15.sp,
            lineHeight = 22.sp,
        )

        // Location badge
        AnimatedVisibility(visible = location != null, enter = fadeIn() + slideInVertically()) {
            location?.let { (lat, lng) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEFF6FF), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        "Location: ${lat.toFixed(4)}°, ${lng.toFixed(4)}°",
                        color = Color(0xFF1E40AF),
                        fontSize = 14.sp,
                    )
                }
            }
        }

        // Audio conversion indicator
        AnimatedVisibility(visible = sosStatus == SOSStatus.CONVERTING_AUDIO) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF7ED), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    "Converting audio to text for SMS delivery...",
                    color = Color(0xFF92400E),
                    fontSize = 14.sp,
                )
            }
        }

        // ── Contact statuses ──
        val showContacts = sosStatus in listOf(
            SOSStatus.ONLINE_SENDING,
            SOSStatus.CALLING_CONTACTS,
            SOSStatus.CONVERTING_AUDIO,
            SOSStatus.SMS_SENDING,
            SOSStatus.BLE_SCANNING,
            SOSStatus.BLE_RELAYING,
            SOSStatus.SUCCESS,
        )
        if (showContacts && contactStatuses.isNotEmpty()) {
            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE5E7EB))

            Text(
                "Emergency Contacts",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF111827),
            )

            contactStatuses.forEach { cs ->
                ContactStatusRow(cs)
            }
        }

        // ── BLE Nearby Devices ──
        val showBLE = sosStatus in listOf(SOSStatus.BLE_SCANNING, SOSStatus.BLE_RELAYING, SOSStatus.SUCCESS)
        if (showBLE && nearbyDevices.isNotEmpty()) {
            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE5E7EB))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    if (sosStatus == SOSStatus.BLE_SCANNING) "Scanning nearby devices..."
                    else "Mesh Relay Network",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF111827),
                )
            }

            nearbyDevices.forEach { device ->
                BLEDeviceRow(device)
            }

            if (sosStatus == SOSStatus.SUCCESS && nearbyDevices.any { it.status == "Relayed ✓" }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF0FDF4), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("✅", fontSize = 14.sp)
                    Text(
                        "SOS relayed through BLE mesh — server notified!",
                        color = Color(0xFF15803D),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

// ── Contact row ─────────────────────────────────────────────

@Composable
private fun ContactStatusRow(cs: ContactSOSStatus) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                cs.contact.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color(0xFF111827),
            )
            Text(
                cs.contact.phone,
                fontSize = 13.sp,
                color = Color(0xFF6B7280),
            )
        }

        when (cs.status) {
            ContactDeliveryStatus.PENDING -> StatusBadge("Pending...", Color(0xFF6B7280), Color(0xFFF3F4F6))
            ContactDeliveryStatus.CALLING -> StatusBadge("Calling...", Color(0xFF2563EB), Color(0xFFDBEAFE), Icons.Default.PhoneInTalk)
            ContactDeliveryStatus.ANSWERED -> StatusBadge("Answered", Color(0xFF15803D), Color(0xFFDCFCE7), Icons.Default.Phone)
            ContactDeliveryStatus.NO_ANSWER -> StatusBadge("No Answer", Color(0xFF6B7280), Color(0xFFF3F4F6), Icons.Default.PhoneDisabled)
            ContactDeliveryStatus.SENT -> StatusBadge("Sent", Color(0xFF2563EB), Color(0xFFDBEAFE))
            ContactDeliveryStatus.DELIVERED -> StatusBadge("Delivered", Color(0xFF15803D), Color(0xFFDCFCE7), Icons.Default.Check)
        }
    }
}

@Composable
private fun StatusBadge(
    text: String,
    fg: Color,
    bg: Color,
    icon: ImageVector? = null,
) {
    Row(
        modifier = Modifier
            .background(bg, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
        }
        Text(text, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

// ── BLE Device Row ──────────────────────────────────────────

@Composable
private fun BLEDeviceRow(device: NearbyBLEDevice) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (device.status == "Relayed ✓") Color(0xFFBBF7D0) else Color(0xFFE5E7EB),
                RoundedCornerShape(12.dp),
            )
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f),
        ) {
            // Device icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (device.isOnline) Color(0xFFDBEAFE) else Color(0xFFF3F4F6),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = if (device.isOnline) Color(0xFF2563EB) else Color(0xFF9CA3AF),
                    modifier = Modifier.size(18.dp),
                )
            }

            Column {
                Text(
                    device.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color(0xFF111827),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(device.distance, fontSize = 12.sp, color = Color(0xFF6B7280))
                    Box(
                        modifier = Modifier
                            .background(
                                if (device.isOnline) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                                RoundedCornerShape(4.dp),
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            if (device.isOnline) "●Online" else "○Offline",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (device.isOnline) Color(0xFF15803D) else Color(0xFF92400E),
                        )
                    }
                }
            }
        }

        // Status
        Text(
            device.status,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = when (device.status) {
                "Relayed ✓" -> Color(0xFF15803D)
                "Relaying..." -> Color(0xFF2563EB)
                "Connecting..." -> Color(0xFFD97706)
                else -> Color(0xFF6B7280)
            },
        )
    }
}

// ── Cancel PIN Dialog ───────────────────────────────────────

@Composable
private fun CancelPinDialog(
    safePin: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    var pin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf("") }
    val pinNotSet = safePin.isEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .shadow(16.dp, RoundedCornerShape(20.dp))
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Cancel SOS Alert",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF111827),
            )

            Spacer(Modifier.height(8.dp))

            Text(
                if (pinNotSet)
                    "Enter any 4-digit PIN to confirm you are safe (no PIN configured)."
                else
                    "Enter your Safe PIN to confirm you are safe and cancel the alert.",
                color = Color(0xFF6B7280),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = {
                    if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                        pin = it
                        pinError = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                placeholder = { Text("Enter 4-digit PIN", color = Color(0xFFD1D5DB)) },
                singleLine = true,
                isError = pinError.isNotEmpty(),
            )

            if (pinError.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(pinError, color = Color(0xFFDC2626), fontSize = 13.sp, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6)),
                ) {
                    Text("Back", color = Color(0xFF374151), fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = {
                        when {
                            pin.length != 4 -> pinError = "Please enter a 4-digit PIN"
                            !pinNotSet && pin != safePin -> pinError = "Incorrect PIN. Try again."
                            else -> onConfirm()
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = pin.length == 4,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB),
                        disabledContainerColor = Color(0xFFD1D5DB),
                    ),
                ) {
                    Text("Confirm Safe", color = Color.White, fontWeight = FontWeight.Medium)
                }
            }

            if (pinNotSet) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Set a Safe PIN in your Profile page for better security.",
                    color = Color(0xFF9CA3AF),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                )
            }
        }
    }
}

// ── Helpers ─────────────────────────────────────────────────

private fun Double.toFixed(decimals: Int): String {
    val factor = 10.0.pow(decimals)
    val rounded = round(this * factor) / factor
    val str = rounded.toString()
    val dot = str.indexOf('.')
    return if (dot == -1) "$str.${"0".repeat(decimals)}"
    else str.padEnd(dot + 1 + decimals, '0').substring(0, dot + 1 + decimals)
}

private fun formatElapsedTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "$mins:${secs.toString().padStart(2, '0')}"
}

private fun getStatusMessage(status: SOSStatus, someoneAnswered: Boolean): String = when (status) {
    SOSStatus.DETECTING -> "Processing trigger signal..."
    SOSStatus.LOCATING -> "Acquiring GPS location..."
    SOSStatus.CHECKING_NETWORK -> "Checking network connectivity..."
    SOSStatus.ONLINE_SENDING -> "Sending alerts via internet to all contacts..."
    SOSStatus.CALLING_CONTACTS -> "Calling emergency contacts..."
    SOSStatus.CONVERTING_AUDIO -> "Converting audio message to text..."
    SOSStatus.SMS_SENDING -> if (someoneAnswered) "Someone answered! Sending location SMS..."
    else "No answer. Sending SMS with location + audio to all contacts..."
    SOSStatus.BLE_SCANNING -> "Scanning for nearby ResQ devices..."
    SOSStatus.BLE_RELAYING -> "Routing SOS via BLE mesh network..."
    SOSStatus.SUCCESS -> if (someoneAnswered) "Emergency contact reached! Location shared successfully."
    else "All emergency contacts alerted successfully!"
}
