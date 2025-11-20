package com.mylab.qrscanner.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mylab.qrscanner.presentation.viewmodel.AuthViewModel
import com.mylab.qrscanner.ui.theme.*

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var nama by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var role by remember { mutableStateOf("mahasiswa") }
    
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(state) {
        if (state is com.mylab.qrscanner.presentation.viewmodel.AuthState.Success) {
            onRegisterSuccess()
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
                text = "Daftar Akun",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = OrangeMain,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // Nama Field
            OutlinedTextField(
                value = nama,
                onValueChange = { nama = it },
                label = { Text("Nama Lengkap", color = GrayLight) },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = "Nama", tint = OrangeMain)
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
                singleLine = true
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
                    .padding(bottom = 16.dp),
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
            
            // Confirm Password Field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Konfirmasi Password", color = GrayLight) },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "Confirm Password", tint = OrangeMain)
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                            tint = GrayLight
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                singleLine = true
            )
            
            // Role Selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = role == "mahasiswa",
                    onClick = { role = "mahasiswa" },
                    label = { Text("Mahasiswa") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrangeMain,
                        selectedLabelColor = Black
                    )
                )
                FilterChip(
                    selected = role == "petugas",
                    onClick = { role = "petugas" },
                    label = { Text("Petugas") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrangeMain,
                        selectedLabelColor = Black
                    )
                )
            }
            
            // Error Message
            if (state is com.mylab.qrscanner.presentation.viewmodel.AuthState.Error) {
                Text(
                    text = (state as com.mylab.qrscanner.presentation.viewmodel.AuthState.Error).message,
                    color = RedError,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // Register Button
            Button(
                onClick = {
                    if (nama.isNotBlank() && 
                        email.isNotBlank() && 
                        password.isNotBlank() && 
                        password == confirmPassword &&
                        password.length >= 6) {
                        viewModel.register(email, password, nama, role)
                    }
                },
                enabled = nama.isNotBlank() && 
                          email.isNotBlank() && 
                          password.isNotBlank() && 
                          password == confirmPassword &&
                          password.length >= 6 &&
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
                        text = "Daftar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Back to Login
            TextButton(onClick = onNavigateBack) {
                Text(
                    text = "Kembali ke Login",
                    color = OrangeMain
                )
            }
        }
    }
}

