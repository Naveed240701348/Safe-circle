package com.safecircle.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Friend data model representing a friendship relationship
 */
@IgnoreExtraProperties
data class Friend(
    val userId: String = "",
    val friendId: String = "",
    val addedAt: Long = System.currentTimeMillis()
) {
    // Empty constructor for Firebase
    constructor() : this("", "", 0)
}

/**
 * Friend request data model for pending friend requests
 */
@IgnoreExtraProperties
data class FriendRequest(
    val requestId: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val fromUserName: String = "",
    val fromUserEmail: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    // Empty constructor for Firebase
    constructor() : this("", "", "", "", "", 0)
}
