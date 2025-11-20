package com.mylab.qrscanner.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mylab.qrscanner.presentation.viewmodel.AuthViewModel
import com.mylab.qrscanner.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToResetPassword: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(state) {
        if (state is com.mylab.qrscanner.presentation.viewmodel.AuthState.Success) {
            onLoginSuccess()
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
                text = "MyLab QR Scanner",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = OrangeMain,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Login untuk melanjutkan",
                style = MaterialTheme.typography.bodyMedium,
                color = GrayLight,
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
                    .padding(bottom = 16.dp),
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
            
            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = GrayLight) },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "Password", tint = OrangeMain)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = GrayLight
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    focusedBorderColor = OrangeMain,
                    unfocusedBorderColor = GrayDark,
                    focusedLabelColor = OrangeMain,
                    unfocusedLabelColor = GrayLight
                ),
                singleLine = true
            )
            
            // Forgot Password
            TextButton(
                onClick = onNavigateToResetPassword,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Lupa Password?",
                    color = OrangeMain,
                    textAlign = TextAlign.End
                )
            }
            
            // Error Message
            if (state is com.mylab.qrscanner.presentation.viewmodel.AuthState.Error) {
                Text(
                    text = (state as com.mylab.qrscanner.presentation.viewmodel.AuthState.Error).message,
                    color = RedError,
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
            
            // Login Button
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        viewModel.login(email, password)
                    }
                },
                enabled = email.isNotBlank() && 
                          password.isNotBlank() &&
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
                        text = "Login",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Register Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Belum punya akun? ",
                    color = GrayLight
                )
                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        text = "Daftar",
                        color = OrangeMain,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

