package com.mylab.qrscanner.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.mylab.qrscanner.R
import com.mylab.qrscanner.ui.theme.*

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Lottie Animation
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Black),
                contentAlignment = Alignment.Center
            ) {
                // Load animation from local file (extracted from .lottie)
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(R.raw.qr_scan_animation)
                )
                
                val progress by animateLottieCompositionAsState(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    speed = 1.5f,
                    restartOnPlay = true
                )
                
                when {
                    composition != null -> {
                        // Animation loaded successfully
                        LottieAnimation(
                            composition = composition,
                            progress = { progress },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        // Show animated placeholder while loading
                        AnimatedQRPlaceholder()
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Bottom Section with white background
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp))
                    .background(White)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Lab Item Management",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    color = Black,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Scan, manage, and track your laboratory\nitems with ease—fast, secure, and efficient.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    ),
                    color = Gray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Get Started Button
                FloatingActionButton(
                    onClick = onGetStarted,
                    containerColor = OrangeMain,
                    contentColor = Black,
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Get Started",
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun AnimatedQRPlaceholder() {
    // Animated QR Code placeholder with pulsing effect
    var scale by remember { mutableStateOf(1f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            scale = 1.1f
            kotlinx.coroutines.delay(800)
            scale = 1f
            kotlinx.coroutines.delay(800)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(60.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top corners
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                        .size(60.dp)
                        .background(OrangeMain, RoundedCornerShape(8.dp))
                    )
                        Box(
                            modifier = Modifier
                        .size(60.dp)
                        .background(OrangeMain, RoundedCornerShape(8.dp))
                            )
                }
                
            // Center QR pattern
                            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(5) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(5) {
                        Box(
                            modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            if ((it + it) % 2 == 0) OrangeMain else OrangeMain.copy(alpha = 0.3f),
                                            CircleShape
                                        )
                        )
                    }
                }
            }
                }
            }
            
            // Bottom corners
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                        .size(60.dp)
                        .background(OrangeMain, RoundedCornerShape(8.dp))
                    )
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(OrangeMain, RoundedCornerShape(8.dp))
                )
            }
        }
    }
}




