package ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import platform.PlatformConfig

@Composable
fun AuthScreenComposable(
    onLogin: () -> Unit,
    onSignup: () -> Unit
) {
    val isAndroid = PlatformConfig.isAndroid

    val titleSize = if (isAndroid) 28.sp else 30.sp
    val buttonHeight = if (isAndroid) 52.dp else 56.dp
    val cornerRadius = if (isAndroid) 14.dp else 18.dp
    val easing = if (isAndroid) FastOutSlowInEasing else EaseInOut

    val enterAnim = remember {
        slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(600, easing = easing)
        ) + fadeIn(tween(600))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFF8FAFC), Color(0xFFE0F2FE))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = true,
            enter = enterAnim
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .widthIn(max = 420.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // 🔵 Logo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(16.dp, CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🛡️", fontSize = 32.sp)
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Guardian",
                    fontSize = titleSize,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Your lifeline in emergencies",
                    color = Color(0xFF6B7280)
                )

                Spacer(Modifier.height(32.dp))

                // 🧩 Feature Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = if (isAndroid) 12.dp else 6.dp,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    FeatureItem("✓", "Instant SOS Alerts", "Reach emergency contacts in seconds", Color(0xFFDCFCE7), Color(0xFF16A34A))
                    FeatureItem("✓", "Works Offline", "Mesh network relay when no signal", Color(0xFFDBEAFE), Color(0xFF2563EB))
                    FeatureItem("✓", "Voice & Gesture Activation", "Trigger help hands-free", Color(0xFFF3E8FF), Color(0xFF7C3AED))
                }

                Spacer(Modifier.height(24.dp))

                // 🟣 Create Account
                Button(
                    onClick = onLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(buttonHeight),
                    shape = RoundedCornerShape(cornerRadius),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp) // Make padding uniform
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
                                ),
                                RoundedCornerShape(cornerRadius)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Create Account", color = Color.White)
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    "Trusted by emergency responders worldwide",
                    color = Color(0xFF9CA3AF),
                    fontSize = 12.sp
                )

            }
        }
    }
}

@Composable
private fun FeatureItem(
    icon: String,
    title: String,
    subtitle: String,
    bg: Color,
    fg: Color
) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(bg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, color = fg)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Color(0xFF6B7280), fontSize = 13.sp)
        }
    }
}
