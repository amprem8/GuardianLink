package ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SignupScreen(
    isLoading: Boolean,
    error: String,
    onSignup: (name: String, email: String, password: String, confirmPassword: String, agreeToTerms: Boolean) -> Unit,
    onDismissError: () -> Unit,
    onSignIn: () -> Unit = {},
) {

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    var agreeToTerms by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFF8FAFC), Color(0xFFE0F2FE))
                )
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { focusManager.clearFocus() }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // HEADER
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 6.dp, top = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(6.dp, CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF2563EB),
                                    Color(0xFF7C3AED)
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.HealthAndSafety,
                        contentDescription = "ResQ Logo",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    "Create Account",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    "Join ResQ for safer living",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            // CARD
            Column(
                modifier = Modifier
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                if (error.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF1F2), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                            .clickable { onDismissError() }
                    ) {
                        Text(error, color = Color(0xFFB91C1C))
                    }
                }

                LabeledInput(
                    label = "Full Name",
                    value = name,
                    icon = Icons.Outlined.Person,
                    keyboardConfig = KeyboardConfig(
                        imeAction = ImeAction.Next,
                        onImeAction = { emailFocusRequester.requestFocus() },
                    ),
                ) { name = it }

                LabeledInput(
                    label = "Email Address",
                    value = email,
                    icon = Icons.Outlined.Email,
                    keyboardConfig = KeyboardConfig(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Email,
                        focusRequester = emailFocusRequester,
                        onImeAction = { passwordFocusRequester.requestFocus() },
                    ),
                ) { email = it }

                PasswordInput(
                    label = "Password",
                    value = password,
                    visible = showPassword,
                    keyboardConfig = KeyboardConfig(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Password,
                        focusRequester = passwordFocusRequester,
                        onImeAction = { confirmPasswordFocusRequester.requestFocus() },
                    ),
                    onValueChange = { password = it },
                    onToggle = { showPassword = !showPassword },
                )

                PasswordInput(
                    label = "Confirm Password",
                    value = confirmPassword,
                    visible = showConfirmPassword,
                    keyboardConfig = KeyboardConfig(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Password,
                        focusRequester = confirmPasswordFocusRequester,
                        onImeAction = { focusManager.clearFocus() },
                    ),
                    onValueChange = { confirmPassword = it },
                    onToggle = { showConfirmPassword = !showConfirmPassword },
                )
                Spacer(Modifier.height(6.dp))

                TermsSection(
                    checked = agreeToTerms,
                    onCheckedChange = { agreeToTerms = it }
                )

                Spacer(Modifier.height(6.dp))

                Button(
                    onClick = {
                        onSignup(name, email, password, confirmPassword, agreeToTerms)
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF2563EB),
                                        Color(0xFF7C3AED)
                                    )
                                ),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text("Create Account", color = Color.White)
                        }
                    }
                }

                DividerSection()

                OutlinedButton(
                    onClick = { },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Sign in with Comcast SSO")
                }
            }
            Spacer(Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Already have an account? ", fontSize = 12.sp)
                Text(
                    "Sign in",
                    color = Color(0xFF2563EB),
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { onSignIn() }
                )
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}

/** Groups keyboard-related config so composables stay under the 7-param limit. */
data class KeyboardConfig(
    val imeAction: ImeAction = ImeAction.Next,
    val keyboardType: KeyboardType = KeyboardType.Text,
    val focusRequester: FocusRequester? = null,
    val onImeAction: (() -> Unit)? = null,
)

@Composable
fun LabeledInput(
    label: String,
    value: String,
    icon: ImageVector = Icons.Outlined.Person,
    keyboardConfig: KeyboardConfig = KeyboardConfig(),
    onChange: (String) -> Unit,
) {
    Column {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Spacer(Modifier.height(2.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            leadingIcon = { Icon(icon, contentDescription = null) },
            modifier = if (keyboardConfig.focusRequester != null)
                Modifier.fillMaxWidth().focusRequester(keyboardConfig.focusRequester)
            else
                Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardConfig.keyboardType,
                imeAction = keyboardConfig.imeAction,
            ),
            keyboardActions = KeyboardActions(
                onNext = { keyboardConfig.onImeAction?.invoke() },
                onDone = { keyboardConfig.onImeAction?.invoke() },
            ),
        )
    }
}

@Composable
fun PasswordInput(
    label: String,
    value: String,
    visible: Boolean,
    keyboardConfig: KeyboardConfig = KeyboardConfig(imeAction = ImeAction.Done, keyboardType = KeyboardType.Password),
    onValueChange: (String) -> Unit,
    onToggle: () -> Unit,
) {
    Column {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Spacer(Modifier.height(2.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                // Block paste: only allow single-char additions or deletions
                val diff = newValue.length - value.length
                if (diff <= 1) onValueChange(newValue)
            },
            singleLine = true,
            visualTransformation =
                if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = { Text("🔒") },
            trailingIcon = {
                Text(
                    if (visible) "🙈" else "👁",
                    modifier = Modifier.clickable { onToggle() },
                )
            },
            modifier = if (keyboardConfig.focusRequester != null)
                Modifier.fillMaxWidth().focusRequester(keyboardConfig.focusRequester)
            else
                Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = keyboardConfig.imeAction,
            ),
            keyboardActions = KeyboardActions(
                onNext = { keyboardConfig.onImeAction?.invoke() },
                onDone = { keyboardConfig.onImeAction?.invoke() },
            ),
        )
    }
}


@Composable
fun TermsSection(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .background(Color(0xFFFFFBEB), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp))
            .padding(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "I agree to the Terms of Service and Privacy Policy.",
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun DividerSection() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f)
        )

        Text(
            "  Or  ",
            color = Color.Gray
        )

        HorizontalDivider(
            modifier = Modifier.weight(1f)
        )
    }
}
