package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import screenmodel.PRESET_PHRASES

// ── Data classes ────────────────────────────────────────────

data class TriggerConfigUiState(
    val voicePhrase: String,
    val gestureType: String,
    val useCustomPhrase: Boolean,
    val customPhrase: String,
    val isRecording: Boolean,
    val error: String,
    val isValid: Boolean,
)

data class TriggerConfigActions(
    val onSetVoicePhrase: (String) -> Unit,
    val onSetGestureType: (String) -> Unit,
    val onSetUseCustomPhrase: (Boolean) -> Unit,
    val onSetCustomPhrase: (String) -> Unit,
    val onRefreshPhrase: () -> Unit,
    val onTestVoice: () -> Unit,
    val onDismissError: () -> Unit,
    val onSave: () -> Unit,
    val onBack: () -> Unit,
)

// ── Main Screen ─────────────────────────────────────────────

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
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { focusManager.clearFocus() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 88.dp)
        ) {
            // ── Top bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = actions.onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF374151),
                    )
                }
                Text(
                    "Trigger Configuration",
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
                    "Configure how you want to trigger an SOS alert",
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )

                // ── Error banner ──
                AnimatedVisibility(visible = state.error.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF1F2), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                            .clickable { actions.onDismissError() }
                    ) {
                        Text(state.error, color = Color(0xFFB91C1C), fontSize = 13.sp)
                    }
                }

                // ── Voice Command Section ──
                VoiceCommandCard(state, actions)

                // ── Gesture Section ──
                GestureCard(state.gestureType, actions.onSetGestureType)

                Spacer(Modifier.height(16.dp))
            }
        }

        // ── Save button (fixed bottom) ──
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 16.dp)
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
                            if (state.isValid) Brush.horizontalGradient(
                                listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
                            ) else Brush.horizontalGradient(
                                listOf(Color(0xFFD1D5DB), Color(0xFFD1D5DB))
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
                            "Save Configuration",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                        )
                    }
                }
            }
        }
    }
}

// ── Voice Command Card ──────────────────────────────────────

@Composable
private fun VoiceCommandCard(
    state: TriggerConfigUiState,
    actions: TriggerConfigActions,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(20.dp),
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFFDBEAFE), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Mic,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(22.dp),
                )
            }
            Text(
                "Voice Command",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color(0xFF111827),
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Preset / Custom toggle ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF3F4F6), RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            PillButton(
                text = "Preset Phrases",
                selected = !state.useCustomPhrase,
                onClick = { actions.onSetUseCustomPhrase(false) },
                modifier = Modifier.weight(1f),
            )
            PillButton(
                text = "Custom Phrase",
                selected = state.useCustomPhrase,
                onClick = { actions.onSetUseCustomPhrase(true) },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(16.dp))

        if (!state.useCustomPhrase) {
            // Preset mode
            PresetPhraseSection(
                selected = state.voicePhrase,
                onSelect = actions.onSetVoicePhrase,
                onRefresh = actions.onRefreshPhrase,
            )
        } else {
            // Custom mode
            CustomPhraseSection(
                value = state.customPhrase,
                onChange = actions.onSetCustomPhrase,
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Test voice button ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (state.isRecording) Color(0xFF2563EB) else Color(0xFFD1D5DB),
                    RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
                .clickable(enabled = !state.isRecording) { actions.onTestVoice() }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Filled.VolumeUp,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    if (state.isRecording) "Listening..." else "Test Voice Recognition",
                    color = Color(0xFF2563EB),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        // ── Recording indicator ──
        AnimatedVisibility(visible = state.isRecording) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF2563EB), CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Say your activation phrase",
                    color = Color(0xFF2563EB),
                    fontSize = 13.sp,
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Info note ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEFF6FF), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(10.dp))
                .padding(14.dp),
        ) {
            Text(
                "ℹ️  Voice recognition uses filtering to reduce false triggers. " +
                        "Works best in quiet environments.",
                color = Color(0xFF1E40AF),
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
        }
    }
}

// ── Preset Phrase Section ───────────────────────────────────

@Composable
private fun PresetPhraseSection(
    selected: String,
    onSelect: (String) -> Unit,
    onRefresh: () -> Unit,
) {
    // Current phrase display + refresh
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Current Phrase", fontSize = 13.sp, color = Color(0xFF374151), fontWeight = FontWeight.Medium)

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFF9FAFB), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Text(
                    "\"$selected\"",
                    color = Color(0xFF111827),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onRefresh() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = "Refresh",
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Text("Or choose from presets:", fontSize = 13.sp, color = Color(0xFF374151), fontWeight = FontWeight.Medium)

        // ── Phrase chips grid ──
        val rows = PRESET_PHRASES.chunked(3)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            rows.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    row.forEach { phrase ->
                        PhraseChip(
                            text = phrase,
                            isSelected = phrase == selected,
                            onClick = { onSelect(phrase) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    // Fill remaining space if row has fewer than 3 items
                    repeat(3 - row.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PhraseChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (isSelected) Color(0xFF2563EB) else Color.White
    val fgColor = if (isSelected) Color.White else Color(0xFF374151)
    val borderColor = if (isSelected) Color(0xFF2563EB) else Color(0xFFE5E7EB)

    Box(
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .background(bgColor, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = fgColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

// ── Custom Phrase Section ───────────────────────────────────

@Composable
private fun CustomPhraseSection(
    value: String,
    onChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Custom Activation Phrase",
            fontSize = 13.sp,
            color = Color(0xFF374151),
            fontWeight = FontWeight.Medium,
        )

        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text("Enter your custom phrase", color = Color(0xFF9CA3AF)) },
            leadingIcon = {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(20.dp),
                )
            },
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
            "Choose a unique phrase that's easy to remember and say quickly",
            fontSize = 12.sp,
            color = Color(0xFF9CA3AF),
            lineHeight = 16.sp,
        )
    }
}

// ── Gesture Card ────────────────────────────────────────────

@Composable
private fun GestureCard(gestureType: String, onSetGestureType: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(20.dp),
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFFF3E8FF), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Phone,
                    contentDescription = null,
                    tint = Color(0xFF7C3AED),
                    modifier = Modifier.size(22.dp),
                )
            }
            Text(
                "Gesture Trigger",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color(0xFF111827),
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Double-tap option ──
        GestureOption(
            title = "Back Tap (Double)",
            description = "Tap the back of your phone twice rapidly",
            isSelected = gestureType == "double-tap",
            onClick = { onSetGestureType("double-tap") },
        )

        Spacer(Modifier.height(10.dp))

        // ── Shake option ──
        GestureOption(
            title = "Device Shake",
            description = "Shake your phone vigorously",
            isSelected = gestureType == "shake",
            onClick = { onSetGestureType("shake") },
        )

        Spacer(Modifier.height(16.dp))

        // ── Warning note ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFEF3C7), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(10.dp))
                .padding(14.dp),
        ) {
            Text(
                "⚠️  Gesture triggers may activate accidentally. Consider using " +
                        "in combination with voice confirmation.",
                color = Color(0xFF92400E),
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
private fun GestureOption(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) Color(0xFF2563EB) else Color(0xFFE5E7EB)
    val bgColor = if (isSelected) Color(0xFFF0F5FF) else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .background(bgColor, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFF2563EB),
                unselectedColor = Color(0xFF9CA3AF),
            ),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color(0xFF111827),
            )
            Text(
                description,
                fontSize = 13.sp,
                color = Color(0xFF6B7280),
                lineHeight = 18.sp,
            )
        }
    }
}

// ── Pill toggle button ──────────────────────────────────────

@Composable
private fun PillButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val elevation by animateDpAsState(
        targetValue = if (selected) 2.dp else 0.dp,
        animationSpec = tween(150),
        label = "pillElevation",
    )

    Box(
        modifier = modifier
            .shadow(elevation, RoundedCornerShape(8.dp))
            .background(
                if (selected) Color.White else Color.Transparent,
                RoundedCornerShape(8.dp),
            )
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color(0xFF111827) else Color(0xFF6B7280),
        )
    }
}
