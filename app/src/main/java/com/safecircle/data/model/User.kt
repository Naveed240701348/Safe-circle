package com.safecircle.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * User data model representing a user in the SafeCircle app
 */
@IgnoreExtraProperties
data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val fcmToken: String = "",
    val profileImageUrl: String = "",
    val age: Int = 0,
    val gender: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    // Empty constructor for Firebase
    constructor() : this("", "", "", "", "", 0, "", 0)
}
