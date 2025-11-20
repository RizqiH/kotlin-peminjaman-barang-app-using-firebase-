package com.mylab.qrscanner.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object ResetPassword : Screen("reset_password")
    object Main : Screen("main")
    object Scanner : Screen("scanner")
    object Result : Screen("result/{qrCode}") {
        fun createRoute(qrCode: String) = "result/$qrCode"
    }
    object History : Screen("history")
    object Products : Screen("products")
    object AddProduct : Screen("add_product")
    object QRCodeDisplay : Screen("qr_display/{itemId}/{itemCode}/{itemName}/{category}/{condition}/{location}") {
        fun createRoute(
            itemId: String,
            itemCode: String,
            itemName: String,
            category: String,
            condition: String,
            location: String
        ) = "qr_display/${java.net.URLEncoder.encode(itemId, "UTF-8")}/${java.net.URLEncoder.encode(itemCode, "UTF-8")}/${java.net.URLEncoder.encode(itemName, "UTF-8")}/${java.net.URLEncoder.encode(category, "UTF-8")}/${java.net.URLEncoder.encode(condition, "UTF-8")}/${java.net.URLEncoder.encode(location, "UTF-8")}"
    }
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
    object Borrowing : Screen("borrowing")
    object BorrowingHistory : Screen("borrowing_history")
    object ActiveBorrowings : Screen("active_borrowings")
    object ReturnVerification : Screen("return_verification/{borrowingId}/{expectedItemId}") {
        fun createRoute(borrowingId: String, expectedItemId: String) = 
            "return_verification/${java.net.URLEncoder.encode(borrowingId, "UTF-8")}/${java.net.URLEncoder.encode(expectedItemId, "UTF-8")}"
    }
    object Approval : Screen("approval")
    object Profile : Screen("profile")
    object Return : Screen("return")
    object Chat : Screen("chat")
}




