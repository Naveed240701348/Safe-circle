package com.safecircle.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.safecircle.data.model.Friend
import com.safecircle.data.model.FriendRequest
import com.safecircle.data.model.User
import kotlinx.coroutines.tasks.await

/**
 * Repository handling friend operations
 */
class FriendRepository {
    
    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = Firebase.firestore

    /**
     * Search user by email
     */
    suspend fun searchUserByEmail(email: String): Result<User> {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()
            
            if (snapshot.documents.isNotEmpty()) {
                val user = snapshot.documents.first().toObject(User::class.java) ?: User()
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send friend request
     */
    suspend fun sendFriendRequest(friendId: String): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val currentUser = getCurrentUser()
            val requestId = "${currentUserId}_${friendId}"
            
            val friendRequest = FriendRequest(
                requestId = requestId,
                fromUserId = currentUserId,
                toUserId = friendId,
                fromUserName = currentUser.name,
                fromUserEmail = currentUser.email
            )
            
            firestore.collection("friendRequests")
                .document(requestId)
                .set(friendRequest)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Accept friend request
     */
    suspend fun acceptFriendRequest(requestId: String): Result<Unit> {
        return try {
            val requestDoc = firestore.collection("friendRequests").document(requestId).get().await()
            val request = requestDoc.toObject(FriendRequest::class.java) ?: throw Exception("Invalid request")
            
            // Add friendship for both users
            val friend1 = Friend(userId = request.fromUserId, friendId = request.toUserId)
            val friend2 = Friend(userId = request.toUserId, friendId = request.fromUserId)
            
            firestore.collection("friends")
                .document("${request.fromUserId}_${request.toUserId}")
                .set(friend1)
                .await()
            
            firestore.collection("friends")
                .document("${request.toUserId}_${request.fromUserId}")
                .set(friend2)
                .await()
            
            // Delete the friend request
            firestore.collection("friendRequests").document(requestId).delete().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Decline (delete) friend request
     */
    suspend fun declineFriendRequest(requestId: String): Result<Unit> {
        return try {
            firestore.collection("friendRequests")
                .document(requestId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get current user's friends
     */
    suspend fun getFriends(): Result<List<User>> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            
            val snapshot = firestore.collection("friends")
                .whereEqualTo("userId", currentUserId)
                .get()
                .await()
            
            val friendIds = snapshot.documents.map { it.getString("friendId") ?: "" }
            
            if (friendIds.isNotEmpty()) {
                val friendsSnapshot = firestore.collection("users")
                    .whereIn("userId", friendIds)
                    .get()
                    .await()
                
                val friends = friendsSnapshot.documents.mapNotNull { 
                    it.toObject(User::class.java) 
                }
                Result.success(friends)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get pending friend requests
     */
    suspend fun getPendingFriendRequests(): Result<List<FriendRequest>> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            
            val snapshot = firestore.collection("friendRequests")
                .whereEqualTo("toUserId", currentUserId)
                .get()
                .await()
            
            val requests = snapshot.documents.mapNotNull { 
                it.toObject(FriendRequest::class.java) 
            }
            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
     * Get friends' FCM tokens for sending notifications
     */
    suspend fun getFriendsFCMTokens(): Result<List<String>> {
        return try {
            val friendsResult = getFriends()
            if (friendsResult.isSuccess) {
                val tokens = friendsResult.getOrNull()
                    ?.map { it.fcmToken }
                    ?.filter { it.isNotEmpty() }
                    ?: emptyList()
                Result.success(tokens)
            } else {
                Result.failure(friendsResult.exceptionOrNull() ?: Exception("Failed to get friends"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
