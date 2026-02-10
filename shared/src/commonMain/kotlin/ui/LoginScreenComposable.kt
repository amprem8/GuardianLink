package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.sync.Mutex
import platform.PlatformConfig

@Composable
fun LoginScreenComposable() {

    val isAndroid = PlatformConfig.isAndroid

    val titleSize = if (isAndroid) 18.sp else 22.sp
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
        contentAlignment = Alignment.TopCenter
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
                    text = "Create Account",
                    fontSize = titleSize,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = "Join Gaurdian for safe living",
                    color = Color(0xFF6B7280),
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 4.dp).align(Alignment.CenterHorizontally)
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
                    Text("Full Name", fontWeight = FontWeight.Normal, fontSize = 14.sp)
                    CommonInputField(
                        value = "",
                        onValueChange = {},
                        placeholder = "test",
                        leadingIcon = "👤",
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Email Address", fontWeight = FontWeight.Normal, fontSize = 14.sp)

                    CommonInputField(
                        value = "",
                        onValueChange = {},
                        placeholder = "test@gmail.com",
                        leadingIcon = "👤",
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Password", fontWeight = FontWeight.Normal, fontSize = 14.sp)

                    CommonInputField(
                        value = "",
                        onValueChange = {},
                        placeholder = "minimum 8 characters",
                        leadingIcon = "👤",
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Confirm Password", fontWeight = FontWeight.Normal, fontSize = 14.sp)

                    CommonInputField(
                        value = "",
                        onValueChange = {},
                        placeholder = "Re-enter password",
                        leadingIcon = "👤",
                    )
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                BorderStroke(1.dp, Color(0xFFFFE685)), // #fee685
                                RoundedCornerShape(12.dp)
                            )
                            .background(Color(0xFFFFFBEA), RoundedCornerShape(12.dp)) // #fffbea
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                    {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Checkbox(
                                checked = true,
                                onCheckedChange = null,
                                modifier = Modifier.scale(0.7f).align(
                                    Alignment.Top
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "I agree to the Terms of Service and Privacy Policy. I understand this app is for emergency use and my data will be used to facilitate SOS alerts.",
                                modifier = Modifier.align(Alignment.Top),
                                fontSize = 12.sp,
                                color = Color(0xFF92400E),
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { /* TODO: Handle create account action */ },
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

                    Spacer(Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        HorizontalDivider(
                            color = Color(0xFFD1D5DB),
                            thickness = 2.dp,
                            modifier = Modifier
                                .height(1.dp)
                                .weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Or",
                            modifier = Modifier.align(Alignment.Top),
                            fontSize = 12.sp,
                            color = Color(0xFF92400E),
                        )
                        Spacer(Modifier.width(8.dp))
                        HorizontalDivider(
                            color = Color(0xFFD1D5DB),
                            thickness = 2.dp,
                            modifier = Modifier
                                .height(1.dp)
                                .weight(1f)
                        )

                    }

                    Button(
                        onClick = { /* TODO: Handle create account action */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                BorderStroke(1.dp, Color(0xFFD1D5DB)),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.background(Color.White)
                        ) {
                            Text(
                                text = "👤",
                                fontSize = 18.sp,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Sign up with Comcast SSO  ", color = Color.Black)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.background(Color.White)
                    ) {
                        Text(
                            text = "Already have an account? ",
                            fontSize = 18.sp,
                        )
                        Spacer(Modifier.width(3.dp))

                        Button(
                            onClick = { /* TODO: Navigate to login screen */ },
                            content = {
                                Text(
                                    text = "Sign in",
                                    fontSize = 18.sp,
                                )
                            }
                        )
                    }
                }
            }
    }
        }
    }

@Composable
fun CommonInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: String,
    modifier: Modifier = Modifier
) {
    Spacer(Modifier.height(8.dp))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(
                BorderStroke(1.dp, Color(0xFFD1D5DB)),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = leadingIcon,
                fontSize = 18.sp,
            )
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder) },
                modifier = Modifier.background(Color.White),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
        }
    }
}


