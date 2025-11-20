package com.mylab.qrscanner.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mylab.qrscanner.presentation.viewmodel.AuthViewModel
import com.mylab.qrscanner.ui.theme.*

@Composable
fun ResetPasswordScreen(
    onResetSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var isEmailSent by remember { mutableStateOf(false) }
    
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(state) {
        if (state is com.mylab.qrscanner.presentation.viewmodel.AuthState.Success) {
            isEmailSent = true
        }
    }
    
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
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Reset Password",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = OrangeMain,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (isEmailSent) {
                Text(
                    text = "Email reset password telah dikirim ke $email",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GreenSuccess,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangeMain,
                        contentColor = Black
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Kembali ke Login",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "Masukkan email Anda untuk menerima link reset password",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayLight,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                
                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = GrayLight) },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = "Email", tint = OrangeMain)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = White,
                        unfocusedTextColor = White,
                        focusedBorderColor = OrangeMain,
                        unfocusedBorderColor = GrayDark,
                        focusedLabelColor = OrangeMain,
                        unfocusedLabelColor = GrayLight
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )
                
                // Error Message
                if (state is com.mylab.qrscanner.presentation.viewmodel.AuthState.Error) {
                    Text(
                        text = (state as com.mylab.qrscanner.presentation.viewmodel.AuthState.Error).message,
                        color = RedError,
                        modifier = Modifier.padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                
                // Send Reset Email Button
                Button(
                    onClick = {
                        if (email.isNotBlank()) {
                            viewModel.resetPassword(email)
                        }
                    },
                    enabled = email.isNotBlank() &&
                              state !is com.mylab.qrscanner.presentation.viewmodel.AuthState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangeMain,
                        contentColor = Black
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (state is com.mylab.qrscanner.presentation.viewmodel.AuthState.Loading) {
                        CircularProgressIndicator(color = Black, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "Kirim Email Reset",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Back Button
                TextButton(onClick = onNavigateBack) {
                    Text(
                        text = "Kembali",
                        color = OrangeMain
                    )
                }
            }
        }
    }
}

