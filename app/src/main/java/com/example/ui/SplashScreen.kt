package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val logoScale = remember { Animatable(0.2f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val splashAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Animate Logo
        launch {
            logoAlpha.animateTo(1f, animationSpec = tween(600))
        }
        launch {
            logoScale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        // Animate Main Text
        delay(300)
        textAlpha.animateTo(1f, animationSpec = tween(700))

        // Animate Subtitle
        delay(200)
        subtitleAlpha.animateTo(1f, animationSpec = tween(500))

        // Wait before fade out
        delay(1500)

        // Fade Out Splash
        splashAlpha.animateTo(0f, animationSpec = tween(400))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(splashAlpha.value)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated Logo Container
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDBEAFE))
                    .border(1.dp, Color(0xFF2563EB), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Facebook,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Text: ARAFAT FB CREATTOR
            Text(
                text = "ARAFAT FB CREATTOR",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFF0F172A),
                modifier = Modifier.alpha(textAlpha.value)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Badge line
            Box(
                modifier = Modifier
                    .alpha(subtitleAlpha.value)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9))
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "⚡ AUTOMATED FB CREATION TOOL ⚡",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2563EB),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
