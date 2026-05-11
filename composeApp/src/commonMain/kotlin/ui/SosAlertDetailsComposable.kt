package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.round
import session.SosAlertSession

@Composable
fun SosAlertDetailsScreen(
    alert: SosAlertSession.Alert?,
    onClose: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1E3A8A), Color(0xFF1E293B))
                )
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (alert == null) {
            Text(
                text = "No active SOS alert",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            return@Box
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = Color(0xFFFCA5A5),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "SOS Alert Received",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(14.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "${alert.victimName} might need help",
                        color = Color(0xFFFEE2E2),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = alert.helpText,
                        color = Color.White,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            val locationText = if (alert.lat != null && alert.lng != null) {
                "Lat: ${formatCoordinate(alert.lat!!)}\nLng: ${formatCoordinate(alert.lng!!)}"
            } else {
                "Location unavailable"
            }

            val hasLocation = alert.lat != null && alert.lng != null
            val mapsUrl = if (hasLocation) "https://maps.google.com/?q=${alert.lat},${alert.lng}" else ""

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (hasLocation) Modifier.clickable { uriHandler.openUri(mapsUrl) } else Modifier
                    ),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(14.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF93C5FD),
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Live location",
                            color = Color(0xFFBFDBFE),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = locationText,
                            color = Color.White,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                if (hasLocation) {
                    Button(
                        onClick = { uriHandler.openUri(mapsUrl) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9)),
                    ) {
                        Text("Open map", color = Color.White)
                    }
                }
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}

private fun formatCoordinate(value: Double): String {
    val sign = if (value < 0) "-" else ""
    val scaled = round(abs(value) * 1_000_000.0).toLong()
    val whole = scaled / 1_000_000
    val fraction = (scaled % 1_000_000).toString().padStart(6, '0')
    return "$sign$whole.$fraction"
}

