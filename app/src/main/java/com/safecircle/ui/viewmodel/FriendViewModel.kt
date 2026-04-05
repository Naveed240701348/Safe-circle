package com.safecircle.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safecircle.data.model.User
import com.safecircle.data.model.FriendRequest
import com.safecircle.repository.FriendRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for friend operations
 */
class FriendViewModel : ViewModel() {
    
    private val friendRepository = FriendRepository()
    
    private val _friends = MutableStateFlow<List<User>>(emptyList())
    val friends: StateFlow<List<User>> = _friends
    
    private val _friendRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val friendRequests: StateFlow<List<FriendRequest>> = _friendRequests
    
    private val _searchResult = MutableStateFlow<User?>(null)
    val searchResult: StateFlow<User?> = _searchResult
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadFriends()
        loadFriendRequests()
    }

    /**
     * Load user's friends
     */
    fun loadFriends() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = friendRepository.getFriends()
            result.fold(
                onSuccess = { friendsList ->
                    _friends.value = friendsList
                    _errorMessage.value = null
                },
                onFailure = { exception ->
                    _errorMessage.value = exception.message ?: "Failed to load friends"
                }
            )
            _isLoading.value = false
        }
    }

    /**
     * Load pending friend requests
     */
    fun loadFriendRequests() {
        viewModelScope.launch {
            val result = friendRepository.getPendingFriendRequests()
            result.fold(
                onSuccess = { requests ->
                    _friendRequests.value = requests
                },
                onFailure = { exception ->
                    _errorMessage.value = exception.message ?: "Failed to load friend requests"
                }
            )
        }
    }

    /**
     * Search user by email
     */
    fun searchUserByEmail(email: String) {
        if (email.isBlank()) {
            _searchResult.value = null
            return
        }

        if (!isValidEmail(email)) {
            _errorMessage.value = "Invalid email format"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val result = friendRepository.searchUserByEmail(email)
            result.fold(
                onSuccess = { user ->
                    _searchResult.value = user
                    _errorMessage.value = null
                },
                onFailure = { exception ->
                    _searchResult.value = null
                    _errorMessage.value = exception.message ?: "User not found"
                }
            )
            _isLoading.value = false
        }
    }

    /**
     * Send friend request
     */
    fun sendFriendRequest(friendId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = friendRepository.sendFriendRequest(friendId)
            result.fold(
                onSuccess = {
                    _errorMessage.value = null
                    _searchResult.value = null
                },
                onFailure = { exception ->
                    _errorMessage.value = exception.message ?: "Failed to send friend request"
                }
            )
            _isLoading.value = false
        }
    }

    /**
     * Accept friend request
     */
    fun acceptFriendRequest(requestId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = friendRepository.acceptFriendRequest(requestId)
            result.fold(
                onSuccess = {
                    _errorMessage.value = null
                    loadFriends() // Refresh friends list
                    loadFriendRequests() // Refresh requests list
                },
                onFailure = { exception ->
                    _errorMessage.value = exception.message ?: "Failed to accept friend request"
                }
            )
            _isLoading.value = false
        }
    }

    /**
     * Decline friend request
     */
    fun declineFriendRequest(requestId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = friendRepository.declineFriendRequest(requestId)
            result.fold(
                onSuccess = {
                    _errorMessage.value = null
                    loadFriendRequests() // Refresh requests list
                },
                onFailure = { exception ->
                    _errorMessage.value = exception.message ?: "Failed to decline friend request"
                }
            )
            _isLoading.value = false
        }
    }

    /**
     * Clear search result
     */
    fun clearSearchResult() {
        _searchResult.value = null
    }

    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Validate email format
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
