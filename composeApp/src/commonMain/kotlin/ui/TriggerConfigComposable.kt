package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import screenmodel.PRESET_PHRASES
import screenmodel.GestureTestState
import screenmodel.UploadState

// ── Data classes ──────────────────────────────────────────────

data class TriggerConfigUiState(
    val voicePhrase: String,
    val gestureType: String,
    val useCustomPhrase: Boolean,
    val customPhrase: String,
    val isRecording: Boolean,
    val error: String,
    val isValid: Boolean,
    val uploadState: UploadState = UploadState.Idle,
    val gestureTestState: GestureTestState = GestureTestState.Idle,
)

data class TriggerConfigActions(
    val onSetVoicePhrase: (String) -> Unit,
    val onSetGestureType: (String) -> Unit,
    val onSetUseCustomPhrase: (Boolean) -> Unit,
    val onSetCustomPhrase: (String) -> Unit,
    val onRefreshPhrase: () -> Unit,
    val onTestVoice: () -> Unit,
    val onStopRecording: () -> Unit,
    val onDismissError: () -> Unit,
    val onDismissUploadError: () -> Unit,
    val onStartGestureTest: () -> Unit,
    val onStopGestureTest: () -> Unit,
    val onDismissGestureError: () -> Unit,
    val onSave: () -> Unit,
    val onBack: () -> Unit,
)

// ── Main Screen ───────────────────────────────────────────────

@Composable
fun TriggerConfigScreen(
    state: TriggerConfigUiState,
    actions: TriggerConfigActions,
) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                focusManager.clearFocus()
            }
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(bottom = 88.dp)) {
            // ── Top bar ──────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = actions.onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color(0xFF374151))
                }
                Text(
                    "Trigger Configuration",
                    fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827),
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
                Text(
                    "Configure how you want to trigger an SOS alert",
                    color = Color(0xFF6B7280), fontSize = 14.sp, lineHeight = 20.sp,
                )
                VoiceCommandCard(state, actions)
                GestureCard(
                    gestureType = state.gestureType,
                    gestureTestState = state.gestureTestState,
                    onSetGestureType = actions.onSetGestureType,
                    onStartGestureTest = actions.onStartGestureTest,
                    onStopGestureTest = actions.onStopGestureTest,
                    onDismissGestureError = actions.onDismissGestureError,
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        // ── Top-pinned banners (error from save + upload state) ───
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 72.dp)   // below the top bar
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Save error banner
            AnimatedVisibility(
                visible = state.error.isNotEmpty(),
                enter = fadeIn(tween(250)) + expandVertically(),
                exit  = fadeOut(tween(250)) + shrinkVertically(),
            ) {
                BannerRow(
                    text       = state.error,
                    bgColor    = Color(0xFFFEF2F2),
                    borderColor = Color(0xFFFECACA),
                    textColor  = Color(0xFFB91C1C),
                    iconColor  = Color(0xFFB91C1C),
                    onDismiss  = actions.onDismissError,
                )
            }

            // Upload state banner
            AnimatedVisibility(
                visible = state.uploadState != UploadState.Idle,
                enter = fadeIn(tween(250)) + expandVertically(),
                exit  = fadeOut(tween(250)) + shrinkVertically(),
            ) {
                when (val us = state.uploadState) {
                    is UploadState.Uploading ->
                        BannerRow(
                            text        = "Saving voice phrase to cloud…",
                            bgColor     = Color(0xFFEFF6FF),
                            borderColor = Color(0xFFBFDBFE),
                            textColor   = Color(0xFF1E40AF),
                            iconColor   = Color(0xFF2563EB),
                            showSpinner = true,
                            onDismiss   = null,
                        )
                    is UploadState.Success ->
                        BannerRow(
                            text        = "Voice phrase saved ✓",
                            bgColor     = Color(0xFFF0FDF4),
                            borderColor = Color(0xFFBBF7D0),
                            textColor   = Color(0xFF15803D),
                            iconColor   = Color(0xFF15803D),
                            icon        = Icons.Filled.CloudDone,
                            onDismiss   = actions.onDismissUploadError,
                        )
                    is UploadState.Error ->
                        BannerRow(
                            text        = us.message,
                            bgColor     = Color(0xFFFEF2F2),
                            borderColor = Color(0xFFFECACA),
                            textColor   = Color(0xFFB91C1C),
                            iconColor   = Color(0xFFB91C1C),
                            icon        = Icons.Filled.Warning,
                            onDismiss   = actions.onDismissUploadError,
                        )
                    else -> Unit
                }
            }
        }

        // ── Save button (fixed bottom) ────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Button(
                onClick = actions.onSave,
                enabled = state.isValid,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (state.isValid)
                                Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF7C3AED)))
                            else
                                Brush.horizontalGradient(listOf(Color(0xFFD1D5DB), Color(0xFFD1D5DB))),
                            RoundedCornerShape(14.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Text(
                            "Save Configuration",
                            color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                        )
                    }
                }
            }
        }
    }
}

// ── Reusable banner row ───────────────────────────────────────

@Composable
private fun BannerRow(
    text: String,
    bgColor: Color,
    borderColor: Color,
    textColor: Color,
    iconColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    showSpinner: Boolean = false,
    onDismiss: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        when {
            showSpinner ->
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = iconColor,
                    strokeCap = StrokeCap.Round,
                )
            icon != null ->
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(16.dp))
        }
        Text(
            text,
            modifier = Modifier.weight(1f),
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 18.sp,
        )
        if (onDismiss != null) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Dismiss",
                tint = iconColor,
                modifier = Modifier
                    .size(18.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onDismiss,
                    ),
            )
        }
    }
}

// ── Voice Command Card ────────────────────────────────────────

@Composable
private fun VoiceCommandCard(state: TriggerConfigUiState, actions: TriggerConfigActions) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(42.dp).background(Color(0xFFDBEAFE), CircleShape),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Filled.Mic, null, tint = Color(0xFF2563EB), modifier = Modifier.size(22.dp)) }
            Text("Voice Command", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF111827))
        }

        Spacer(Modifier.height(16.dp))

        // Preset / Custom pill toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF3F4F6), RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            PillButton("Preset Phrases", !state.useCustomPhrase, { actions.onSetUseCustomPhrase(false) }, Modifier.weight(1f))
            PillButton("Custom Phrase",   state.useCustomPhrase,  { actions.onSetUseCustomPhrase(true) },  Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        if (!state.useCustomPhrase) {
            PresetPhraseSection(selected = state.voicePhrase, onSelect = actions.onSetVoicePhrase, onRefresh = actions.onRefreshPhrase)
        } else {
            CustomPhraseSection(value = state.customPhrase, onChange = actions.onSetCustomPhrase)
        }

        Spacer(Modifier.height(20.dp))

        // ── Test voice recognition button ──────────────────────
        if (!state.isRecording) {
            Button(
                onClick = actions.onTestVoice,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF7C3AED))),
                            RoundedCornerShape(12.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Filled.Mic, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Text(
                            "Test Voice Recognition",
                            color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                        )
                    }
                }
            }
        } else {
            // ── Active recording UI ───────────────────────────────
            RecordingIndicator(onStop = actions.onStopRecording)
        }

        Spacer(Modifier.height(14.dp))

        // Info hint
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEFF6FF), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(10.dp))
                .padding(12.dp),
        ) {
            Text(
                "Tap the button above, then say your phrase clearly. Recording stops automatically when you finish speaking.",
                color = Color(0xFF1E40AF), fontSize = 12.sp, lineHeight = 17.sp,
            )
        }
    }
}

// ── Recording indicator with pulsing mic ─────────────────────

@Composable
private fun RecordingIndicator(onStop: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF1F2), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Pulsing mic circle
        Box(contentAlignment = Alignment.Center) {
            // Ripple ring
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .scale(pulseScale)
                    .background(Color(0xFFDC2626).copy(alpha = pulseAlpha), CircleShape),
            )
            // Solid mic button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFDC2626), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Mic, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Say your phrase now…",
                fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF111827),
            )
            Text(
                "Stops automatically when done",
                fontSize = 12.sp, color = Color(0xFF6B7280),
            )
        }

        // Manual stop button
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFFDC2626), CircleShape)
                .clip(CircleShape)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onStop,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Stop, "Stop", tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

// ── Gesture Card ──────────────────────────────────────────────

@Composable
private fun GestureCard(
    gestureType: String,
    gestureTestState: GestureTestState,
    onSetGestureType: (String) -> Unit,
    onStartGestureTest: () -> Unit,
    onStopGestureTest: () -> Unit,
    onDismissGestureError: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(42.dp).background(Color(0xFFF3E8FF), CircleShape),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Filled.Phone, null, tint = Color(0xFF7C3AED), modifier = Modifier.size(22.dp)) }
            Text("Gesture Trigger", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF111827))
        }

        Spacer(Modifier.height(16.dp))

        GestureOption("Back Tap (Double)",  "Tap the back of your phone twice rapidly",           gestureType == "double-tap",          { onSetGestureType("double-tap") })
        Spacer(Modifier.height(10.dp))
        GestureOption("Back Tap (Triple)",  "Tap the back of your phone three times rapidly",     gestureType == "triple-tap",          { onSetGestureType("triple-tap") })
        Spacer(Modifier.height(10.dp))
        GestureOption("Device Shake",       "Shake your phone vigorously",                        gestureType == "shake",               { onSetGestureType("shake") })
        Spacer(Modifier.height(10.dp))
        GestureOption("Volume Down ×3",     "Press the volume down button three times rapidly",   gestureType == "volume-triple-down",  { onSetGestureType("volume-triple-down") })

        Spacer(Modifier.height(16.dp))

        when (val test = gestureTestState) {
            is GestureTestState.Listening -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEFF6FF), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                ) {
                    Text(
                        "Listening for ${gestureLabel(test.gestureType)}... perform the gesture now.",
                        color = Color(0xFF1E40AF), fontSize = 13.sp, lineHeight = 18.sp,
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            is GestureTestState.Success -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF0FDF4), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                ) {
                    Text(
                        "${test.message} ✓",
                        color = Color(0xFF15803D), fontSize = 13.sp, lineHeight = 18.sp,
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            is GestureTestState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF2F2), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                ) {
                    Text(
                        test.message,
                        color = Color(0xFFB91C1C), fontSize = 13.sp, lineHeight = 18.sp,
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            GestureTestState.Idle -> Unit
        }

        Button(
            onClick = {
                if (gestureTestState is GestureTestState.Listening) onStopGestureTest()
                else {
                    onDismissGestureError()
                    onStartGestureTest()
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (gestureTestState is GestureTestState.Listening) Color(0xFFDC2626) else Color(0xFF2563EB)
            ),
        ) {
            Text(
                text = if (gestureTestState is GestureTestState.Listening) "Stop Gesture Test" else "Test Selected Gesture",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFEF3C7), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(10.dp))
                .padding(14.dp),
        ) {
            Text(
                "⚠️  Gesture triggers may activate accidentally. Consider using in combination with voice confirmation.",
                color = Color(0xFF92400E), fontSize = 13.sp, lineHeight = 18.sp,
            )
        }
    }
}

private fun gestureLabel(type: String): String = when (type) {
    "double-tap" -> "Back Tap (Double)"
    "triple-tap" -> "Back Tap (Triple)"
    "shake" -> "Device Shake"
    "volume-triple-down" -> "Volume Down x3"
    else -> type
}

@Composable
private fun GestureOption(title: String, description: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFF0F5FF) else Color.White, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(
            selected = isSelected, onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFF2563EB), unselectedColor = Color(0xFF9CA3AF),
            ),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF111827))
            Text(description, fontSize = 13.sp, color = Color(0xFF6B7280), lineHeight = 18.sp)
        }
    }
}

// ── Phrase helpers ────────────────────────────────────────────

@Composable
private fun PresetPhraseSection(selected: String, onSelect: (String) -> Unit, onRefresh: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Selected Phrase", fontSize = 13.sp, color = Color(0xFF374151), fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFF9FAFB), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Text("\"$selected\"", color = Color(0xFF111827), fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onRefresh() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Refresh, "Refresh", tint = Color(0xFF6B7280), modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text("Choose from presets:", fontSize = 13.sp, color = Color(0xFF374151), fontWeight = FontWeight.Medium)
        PRESET_PHRASES.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { phrase ->
                    PhraseChip(phrase, phrase == selected, { onSelect(phrase) }, Modifier.weight(1f))
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun PhraseChip(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFF2563EB) else Color.White, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = if (isSelected) Color.White else Color(0xFF374151),
            fontSize = 13.sp, fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center, maxLines = 1,
        )
    }
}

@Composable
private fun CustomPhraseSection(value: String, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Custom Activation Phrase", fontSize = 13.sp, color = Color(0xFF374151), fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text("e.g. Guardian activate", color = Color(0xFF9CA3AF)) },
            leadingIcon = { Icon(Icons.Filled.Edit, null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2563EB),
                unfocusedBorderColor = Color(0xFFE5E7EB),
                cursorColor = Color(0xFF2563EB),
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            "A short, unique phrase you can say quickly in an emergency.",
            fontSize = 12.sp, color = Color(0xFF9CA3AF), lineHeight = 16.sp,
        )
    }
}

// ── Pill toggle ───────────────────────────────────────────────

@Composable
private fun PillButton(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val elevation by animateDpAsState(if (selected) 2.dp else 0.dp, tween(150), label = "pillElev")
    Box(
        modifier = modifier
            .shadow(elevation, RoundedCornerShape(8.dp))
            .background(if (selected) Color.White else Color.Transparent, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text, fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color(0xFF111827) else Color(0xFF6B7280),
        )
    }
}
