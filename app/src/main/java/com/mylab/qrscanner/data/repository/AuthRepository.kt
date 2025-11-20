package com.mylab.qrscanner.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.mylab.qrscanner.data.model.User
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")
    
    val currentUser: FirebaseUser?
        get() = auth.currentUser
    
    suspend fun loginWithEmail(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val user = getUserFromFirestore(firebaseUser.uid)
                Result.Success(user)
            } else {
                Result.Error("Login failed")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Login failed")
        }
    }
    
    suspend fun registerWithEmail(
        email: String,
        password: String,
        nama: String,
        role: String = "mahasiswa"
    ): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val user = User(
                    uid = firebaseUser.uid,
                    email = email,
                    nama = nama,
                    role = role,
                    createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                )
                // Save user to Firestore
                usersCollection.document(firebaseUser.uid).set(user).await()
                Result.Success(user)
            } else {
                Result.Error("Registration failed")
            }
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("CONFIGURATION_NOT_FOUND") == true -> {
                    "Firebase Authentication belum dikonfigurasi. " +
                    "Silakan tambahkan SHA-1 fingerprint di Firebase Console dan enable Email/Password authentication."
                }
                e.message?.contains("EMAIL_EXISTS") == true -> {
                    "Email sudah terdaftar. Silakan gunakan email lain atau login."
                }
                e.message?.contains("INVALID_EMAIL") == true -> {
                    "Format email tidak valid."
                }
                e.message?.contains("WEAK_PASSWORD") == true -> {
                    "Password terlalu lemah. Minimal 6 karakter."
                }
                else -> e.message ?: "Registrasi gagal. Silakan coba lagi."
            }
            Result.Error(errorMessage)
        }
    }
    
    suspend fun getUserFromFirestore(uid: String): User {
        return try {
            val doc = usersCollection.document(uid).get().await()
            if (doc.exists()) {
                doc.toObject(User::class.java) ?: User(uid = uid)
            } else {
                // Create default user if not exists
                val firebaseUser = auth.currentUser
                val defaultUser = User(
                    uid = uid,
                    email = firebaseUser?.email ?: "",
                    nama = firebaseUser?.displayName ?: "User",
                    role = "mahasiswa",
                    photoUrl = firebaseUser?.photoUrl?.toString(),
                    createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                )
                usersCollection.document(uid).set(defaultUser).await()
                defaultUser
            }
        } catch (e: Exception) {
            User(uid = uid)
        }
    }
    
    suspend fun getCurrentUser(): User? {
        return currentUser?.let { getUserFromFirestore(it.uid) }
    }
    
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to send reset password email")
        }
    }
    
    suspend fun updateUserRole(uid: String, role: String): Result<Unit> {
        return try {
            usersCollection.document(uid).update("role", role).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update user role")
        }
    }
    
    fun logout() {
        auth.signOut()
    }
    
    fun isLoggedIn(): Boolean = currentUser != null
}

