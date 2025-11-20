package com.mylab.qrscanner.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mylab.qrscanner.presentation.screen.*
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onGetStarted = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToResetPassword = {
                    navController.navigate(Screen.ResetPassword.route)
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.ResetPassword.route) {
            ResetPasswordScreen(
                onResetSuccess = {
                    // Email sent, user can go back to login
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToScanner = {
                    navController.navigate(Screen.Scanner.route)
                },
                onNavigateToAddProduct = {
                    navController.navigate(Screen.AddProduct.route)
                },
                onNavigateToProductDetail = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                onNavigateToApproval = {
                    navController.navigate(Screen.Approval.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToReturn = {
                    navController.navigate(Screen.Return.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToActiveBorrowings = {
                    navController.navigate(Screen.ActiveBorrowings.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Scanner.route) {
            ScannerScreen(
                borrowingId = null,
                expectedItemId = null,
                onQRCodeScanned = { qrCode ->
                    val encodedQRCode = URLEncoder.encode(qrCode, StandardCharsets.UTF_8.toString())
                    navController.navigate(Screen.Result.createRoute(encodedQRCode))
                },
                onNavigateBack = {
                    if (!navController.popBackStack()) {
                        // If can't pop back, navigate to onboarding
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                }
            )
        }
        
        composable(
            route = Screen.Result.route,
            arguments = listOf(
                navArgument("qrCode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedQRCode = backStackEntry.arguments?.getString("qrCode") ?: ""
            val qrCode = URLDecoder.decode(encodedQRCode, StandardCharsets.UTF_8.toString())
            
            ResultScreen(
                qrCode = qrCode,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onScanAgain = {
                    navController.navigate(Screen.Scanner.route) {
                        popUpTo(Screen.Scanner.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onHistoryItemClick = { qrCode ->
                    val encodedQRCode = URLEncoder.encode(qrCode, StandardCharsets.UTF_8.toString())
                    navController.navigate(Screen.Result.createRoute(encodedQRCode))
                }
            )
        }
        
        composable(Screen.AddProduct.route) {
            AddProductScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSuccess = { createdItem ->
                    // Navigate to QR code display screen with item data
                    navController.navigate(
                        Screen.QRCodeDisplay.createRoute(
                            itemId = createdItem.id,
                            itemCode = createdItem.itemCode,
                            itemName = createdItem.itemName,
                            category = createdItem.category,
                            condition = createdItem.condition,
                            location = createdItem.location
                        )
                    ) {
                        popUpTo(Screen.AddProduct.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.QRCodeDisplay.route,
            arguments = listOf(
                navArgument("itemId") { type = NavType.StringType },
                navArgument("itemCode") { type = NavType.StringType },
                navArgument("itemName") { type = NavType.StringType },
                navArgument("category") { type = NavType.StringType },
                navArgument("condition") { type = NavType.StringType },
                navArgument("location") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val itemId = URLDecoder.decode(backStackEntry.arguments?.getString("itemId") ?: "", StandardCharsets.UTF_8.toString())
            val itemCode = URLDecoder.decode(backStackEntry.arguments?.getString("itemCode") ?: "", StandardCharsets.UTF_8.toString())
            val itemName = URLDecoder.decode(backStackEntry.arguments?.getString("itemName") ?: "", StandardCharsets.UTF_8.toString())
            val category = URLDecoder.decode(backStackEntry.arguments?.getString("category") ?: "", StandardCharsets.UTF_8.toString())
            val condition = URLDecoder.decode(backStackEntry.arguments?.getString("condition") ?: "", StandardCharsets.UTF_8.toString())
            val location = URLDecoder.decode(backStackEntry.arguments?.getString("location") ?: "", StandardCharsets.UTF_8.toString())
            
            val item = com.mylab.qrscanner.data.model.LabItem(
                id = itemId,
                itemCode = itemCode,
                itemName = itemName,
                category = category,
                condition = condition,
                location = location
            )
            
            QRCodeDisplayScreen(
                item = item,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSave = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(
                navArgument("productId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailScreen(
                productId = productId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { editProductId ->
                    // TODO: Navigate to EditProductScreen
                    // For now, navigate to AddProductScreen with edit mode
                    navController.navigate(Screen.AddProduct.route)
                }
            )
        }
        
        composable(Screen.Borrowing.route) {
            BorrowingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.BorrowingHistory.route) {
            BorrowingHistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.ActiveBorrowings.route) {
            ActiveBorrowingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = com.mylab.qrscanner.navigation.Screen.ReturnVerification.route,
            arguments = listOf(
                navArgument("borrowingId") { type = NavType.StringType },
                navArgument("expectedItemId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val borrowingId = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("borrowingId") ?: "",
                StandardCharsets.UTF_8.toString()
            )
            val expectedItemId = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("expectedItemId") ?: "",
                StandardCharsets.UTF_8.toString()
            )
            
            ScannerScreen(
                borrowingId = borrowingId,
                expectedItemId = expectedItemId,
                onQRCodeScanned = { qrCode ->
                    // Di mode return verification, tidak navigate ke ResultScreen
                    // Verifikasi sudah di-handle di ScannerScreen
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                }
            )
        }
        
        composable(Screen.Approval.route) {
            ApprovalScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToReturnVerification = { borrowingId, expectedItemId ->
                    navController.navigate(
                        Screen.ReturnVerification.createRoute(
                            borrowingId = borrowingId,
                            expectedItemId = expectedItemId
                        )
                    )
                },
                onNavigateToScanner = {
                    // Navigate ke scanner screen untuk verifikasi return
                    navController.navigate(Screen.Scanner.route)
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Return.route) {
            ReturnScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToReturnVerification = { borrowingId, expectedItemId ->
                    navController.navigate(
                        Screen.ReturnVerification.createRoute(
                            borrowingId = borrowingId,
                            expectedItemId = expectedItemId
                        )
                    )
                }
            )
        }
        
        composable(Screen.Chat.route) {
            ChatScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}




