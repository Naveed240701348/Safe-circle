package com.safecircle.repository

import android.location.Location
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.safecircle.data.model.User
import kotlinx.coroutines.tasks.await

/**
 * Repository handling SOS operations and emergency notifications
 */
class SOSRepository {
    
    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val messaging = Firebase.messaging

    /**
     * Send SOS alert to all friends
     */
    suspend fun sendSOSAlert(location: Location): Result<Unit> {
        return try {
            val currentUser = getCurrentUser()
            val friendsResult = FriendRepository().getFriends()
            
            if (friendsResult.isFailure) {
                throw Exception("Failed to get friends list")
            }
            
            val friends = friendsResult.getOrNull() ?: emptyList()
            if (friends.isEmpty()) {
                throw Exception("No friends found to notify. Add friends first!")
            }
            val mapsLink = generateMapsLink(location)
            
            // Send notification to each friend
            for (friend in friends) {
                sendEmergencyNotification(friend.email, currentUser.name, mapsLink)
            }
            
            // Log SOS event in Firestore (optional)
            logSOSEvent(currentUser.userId, location, friends.size)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send emergency notification via FCM (this would typically be done via backend)
     * For demo purposes, we'll store the notification request in Firestore
     */
    private suspend fun sendEmergencyNotification(receiverEmail: String, userName: String, locationLink: String) {
        val notificationData = mapOf(
            "token" to receiverEmail,
            "title" to "🚨 EMERGENCY ALERT",
            "body" to "Your friend $userName is in danger! Location: $locationLink",
            "locationLink" to locationLink,
            "timestamp" to System.currentTimeMillis()
        )
        
        // In a real app, this would send to your backend server
        // For now, we'll store it in a collection for demonstration
        firestore.collection("pendingNotifications")
            .add(notificationData)
            .await()
    }

    /**
     * Log SOS event for analytics/history
     */
    private suspend fun logSOSEvent(userId: String, location: Location, friendsNotified: Int) {
        val sosEvent = mapOf(
            "userId" to userId,
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "locationLink" to generateMapsLink(location),
            "friendsNotified" to friendsNotified,
            "timestamp" to System.currentTimeMillis()
        )
        
        firestore.collection("sosEvents")
            .add(sosEvent)
            .await()
    }

    /**
     * Generate Google Maps link from location
     */
    private fun generateMapsLink(location: Location): String {
        return "https://maps.google.com/?q=${location.latitude},${location.longitude}"
    }

    /**
     * Get current user details
     */
    private fun getCurrentUser(): User {
        val firebaseUser = auth.currentUser ?: throw Exception("User not logged in")
        return User(
            userId = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            name = firebaseUser.displayName ?: ""
        )
    }

    /**
     * Get SOS history for current user
     */
    suspend fun getSOSHistory(): Result<List<Map<String, Any>>> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            
            val snapshot = firestore.collection("sosEvents")
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val events = snapshot.documents.map { it.data ?: emptyMap<String, Any>() }
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Listen for incoming SOS alerts for the current user
     */
    fun listenForIncomingAlerts(onAlertReceived: (Map<String, Any>) -> Unit): com.google.firebase.firestore.ListenerRegistration? {
        val currentUserEmail = auth.currentUser?.email ?: return null
        
        // In a real app we'd use tokens, but for this demo we'll listen for notifications 
        // that contain the current user's email in the 'token' field (which we used as a placeholder)
        // or we can search by the 'body' containing the email.
        
        // Better: Let's look for any new document in 'pendingNotifications' 
        // where the 'token' field matches the current user's email (as a simple filter)
        return firestore.collection("pendingNotifications")
            .whereEqualTo("token", currentUserEmail)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                
                for (change in snapshot.documentChanges) {
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        onAlertReceived(change.document.data)
                    }
                }
            }
    }
}
