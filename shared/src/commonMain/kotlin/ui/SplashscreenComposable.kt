package ui

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import screenmodel.SplashScreenModel

class SplashScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val model = rememberScreenModel { SplashScreenModel() }
        val finished by model.finished.collectAsState()

        LaunchedEffect(finished) {
            if (finished) {
                navigator?.replace(LoginScreen())
            }
        }

        SplashScreenContent()
    }

@Composable
fun SplashScreenContent() {

    val infinite = rememberInfiniteTransition()

    val pulseScale by infinite.animateFloat(
        1f, 1.4f,
        infiniteRepeatable(tween(1200, easing = EaseOut))
    )

    val pulseAlpha by infinite.animateFloat(
        0.3f, 0f,
        infiniteRepeatable(tween(1200))
    )

    @Composable
    fun bounce(delay: Int) = infinite.animateFloat(
        0f, -8f,
        infiniteRepeatable(
            keyframes {
                durationMillis = 600
                0f at 0
                8f at 300
                0f at 600
            },
            initialStartOffset = StartOffset(delay)
        )
    )

    val d1 by bounce(0)
    val d2 by bounce(150)
    val d3 by bounce(300)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colorStops = arrayOf(
                        0.0f to Color(0xFF2563EB), // blue-600
                        0.42f to Color(0xFF1D4ED8), // blue-700 (via)
                        1.0f to Color(0xFF7C3AED)  // purple (OKLCH compensated)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset.Infinite
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Box(Modifier.size(140.dp), contentAlignment = Alignment.Center) {

                Box(
                    Modifier
                        .size(140.dp)
                        .scale(pulseScale)
                        .alpha(pulseAlpha)
                        .background(Color.White, CircleShape)
                )

                Box(
                    Modifier
                        .size(120.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Shield",
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Text("Guardian", color = Color.White, fontSize = 28.sp)
            Text("Link", color = Color(0xFFD8B4FE), fontSize = 28.sp)

            Spacer(Modifier.height(8.dp))

            Text(
                "Hybrid Emergency Response System",
                color = Color(0xFFDBEAFE),
                fontSize = 14.sp
            )

            Spacer(Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Dot(d1)
                Dot(d2)
                Dot(d3)
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
}}

