package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.remember
import auth.OtpUiState

private object OtpColors {
    val Primary = Color(0xFF2563EB) // Blue-600
    val ScreenBg = Color(0xFFF8FAFC)
    val CardBg = Color.White
    val AmberBg = Color(0xFFFFFBEB)
    val AmberBorder = Color(0xFFFDE68A)
    val AmberText = Color(0xFF92400E)
    val GrayText = Color(0xFF4B5563)
    val Border = Color(0xFFD1D5DB)
}

@Composable
fun PhoneInputField(value: String, onChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Mobile Number",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF374151),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text("Enter your mobile number", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OtpColors.Primary,
                unfocusedBorderColor = OtpColors.Border,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth() // Removed fixed height to prevent clipping
        )
    }
}

@Composable
fun LegalNotice() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(OtpColors.AmberBg, RoundedCornerShape(8.dp))
            .border(1.dp, OtpColors.AmberBorder, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Legal Notice: Your phone number will be used to validate SOS alerts and prevent misuse.",
            color = OtpColors.AmberText,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun SetupOtpScreen(
    uiState: OtpUiState,
    phone: String,
    otp: String,
    onPhoneChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    onVerifyOtp: () -> Unit,
    onErrorDismiss: () -> Unit
) {
    val focusManager=LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OtpColors.ScreenBg)
            .clickable(indication = null, interactionSource = remember{ MutableInteractionSource() }){
                focusManager.clearFocus()
            },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.95f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = OtpColors.CardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon Header
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFDBEAFE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🛡️", fontSize = 32.sp)
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    "Verify Identity",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Your identity is linked to SOS alerts for security and legal compliance",
                    fontSize = 14.sp,
                    color = OtpColors.GrayText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(32.dp))

                if (uiState is OtpUiState.Error) {
                    ErrorBanner(uiState.message, onErrorDismiss)
                    Spacer(Modifier.height(16.dp))
                }

                when (uiState) {
                    OtpUiState.PhoneEntry, OtpUiState.Loading -> {
                        PhoneInputField(phone, onPhoneChange)
                        Spacer(Modifier.height(16.dp))
                        LegalNotice()
                        Spacer(Modifier.height(24.dp))

                        Button(
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            onClick = onSendOtp,
                            enabled = uiState !is OtpUiState.Loading,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OtpColors.Primary)
                        ) {
                            if (uiState is OtpUiState.Loading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Get OTP", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    OtpUiState.OtpEntry -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "Enter OTP",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = otp,
                                onValueChange = onOtpChange,
                                placeholder = { Text("6-digit code", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = LocalTextStyle.current.copy(
                                    textAlign = TextAlign.Center,
                                    letterSpacing = 8.sp,
                                    fontSize = 18.sp
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                            )
                            Text(
                                "Code sent to $phone",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        Button(
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            onClick = onVerifyOtp,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OtpColors.Primary)
                        ) {
                            Text("Verify OTP", fontWeight = FontWeight.SemiBold)
                        }

                        TextButton(onClick = onErrorDismiss) {
                            Text("Change Number", color = OtpColors.Primary)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFEF2F2), RoundedCornerShape(8.dp)) // Red-50 bg
            .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(8.dp)) // Red-300 border
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = message,
                color = Color(0xFF991B1B), // Red-800 text
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = onDismiss,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Dismiss", color = Color(0xFF991B1B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
