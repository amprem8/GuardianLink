package ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import platform.PlatformConfig

@Composable
fun SplashScreenContent() {
    if (PlatformConfig.isAndroid) {
        AndroidSplash()
    } else {
        IosSplash()
    }
}

@Composable
private fun rememberSplashAnimation(): SplashAnimationState {
    val infinite = rememberInfiniteTransition()

    val pulseScale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = EaseOut)
        )
    )

    val pulseAlpha by infinite.animateFloat(
        initialValue = 0.3f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            tween(1200)
        )
    )

    @Composable
    fun bounce(delay: Int) = infinite.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 600
                0f at 0
                8f at 300
                0f at 600
            },
            initialStartOffset = StartOffset(delay)
        )
    )

    return SplashAnimationState(
        pulseScale,
        pulseAlpha,
        bounce(0).value,
        bounce(150).value,
        bounce(300).value
    )
}

private data class SplashAnimationState(
    val pulseScale: Float,
    val pulseAlpha: Float,
    val d1: Float,
    val d2: Float,
    val d3: Float
)

@Composable
private fun AndroidSplash() {
    val anim = rememberSplashAnimation()

    SplashBaseLayout(anim) {
        Text("Guardian", color = Color.White, fontSize = 28.sp)
    }
}

@Composable
private fun IosSplash() {
    val anim = rememberSplashAnimation()

    SplashBaseLayout(anim) {
        Text("Guardian", color = Color.White, fontSize = 32.sp)
    }
}

@Composable
private fun SplashBaseLayout(
    anim: SplashAnimationState,
    titleContent: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF2563EB), // Blue start
                        Color(0xFF7C3AED)  // Purple end
                    ),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            ),
        contentAlignment = Alignment.Center
    )  {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Box(Modifier.size(140.dp), contentAlignment = Alignment.Center) {

                Box(
                    Modifier
                        .size(140.dp)
                        .scale(anim.pulseScale)
                        .alpha(anim.pulseAlpha)
                        .background(Color.White, CircleShape)
                )

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .shadow(16.dp, CircleShape)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🛡️", fontSize = 52.sp)
                }
            }

            Spacer(Modifier.height(32.dp))

            titleContent()

            Spacer(Modifier.height(8.dp))

            Text(
                "Hybrid Emergency Response System",
                color = Color(0xFFDBEAFE),
                fontSize = 14.sp
            )

            Spacer(Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Dot(anim.d1)
                Dot(anim.d2)
                Dot(anim.d3)
            }
        }
    }
}

@Composable
private fun Dot(offset: Float) {
    Box(
        Modifier
            .size(8.dp)
            .offset(y = offset.dp)
            .background(Color.White, CircleShape)
    )
}
