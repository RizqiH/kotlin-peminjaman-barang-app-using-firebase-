package com.mylab.qrscanner.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.mylab.qrscanner.R
import com.mylab.qrscanner.ui.theme.Black
import com.mylab.qrscanner.ui.theme.OrangeMain
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit
) {
    // Load Lego animation
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.lego_animation)
    )
    
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1, // Play once
        speed = 1.0f,
        restartOnPlay = false
    )
    
    // Navigate to onboarding after animation completes or 3 seconds
    LaunchedEffect(composition) {
        if (composition != null) {
            // Wait for animation to complete (or max 3 seconds)
            delay(3000)
            onNavigateToOnboarding()
        } else {
            // If animation fails to load, wait 2 seconds then navigate
            delay(2000)
            onNavigateToOnboarding()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black),
        contentAlignment = Alignment.Center
    ) {
        if (composition != null) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(300.dp)
            )
        } else {
            // Fallback loading indicator
            CircularProgressIndicator(
                color = com.mylab.qrscanner.ui.theme.OrangeMain
            )
        }
    }
}

