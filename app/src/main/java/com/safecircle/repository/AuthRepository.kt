package com.safecircle.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.safecircle.data.model.User
import kotlinx.coroutines.tasks.await

/**
 * Repository handling authentication operations
 */
class AuthRepository {
    
    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val storage: FirebaseStorage = Firebase.storage

    /**
     * Upload profile image to Firebase Storage
     */
    suspend fun uploadProfileImage(imageUri: Uri): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val storageRef = storage.reference.child("profile_images/$userId.jpg")
            
            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Login user with email and password
     */
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = getUserFromFirestore(authResult.user?.uid ?: "")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Register new user
     */
    suspend fun register(name: String, email: String, password: String): Result<User> {
        return try {
            // Create user in Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("Failed to create user")
            
            // Create user document in Firestore
            val user = User(
                userId = userId,
                name = name,
                email = email
            )
            
            firestore.collection("users").document(userId).set(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logout current user
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Get current logged in user
     */
    suspend fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            getUserFromFirestore(firebaseUser.uid)
        } else {
            null
        }
    }

    /**
     * Get user details from Firestore
     */
    private suspend fun getUserFromFirestore(userId: String): User {
        val document = firestore.collection("users").document(userId).get().await()
        return document.toObject(User::class.java) ?: User()
    }

    /**
     * Update user profile in Firestore
     */
    suspend fun updateProfile(name: String, age: Int, gender: String, profileImageUrl: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val updates = mapOf(
                "name" to name,
                "age" to age,
                "gender" to gender,
                "profileImageUrl" to profileImageUrl
            )
            firestore.collection("users").document(userId)
                .update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update user FCM token
     */
    suspend fun updateFCMToken(token: String) {
        try {
            val userId = auth.currentUser?.uid ?: return
            firestore.collection("users").document(userId)
                .update("fcmToken", token).await()
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to update FCM token", e)
        }
    }

    /**
     * Check if user is logged in
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}
