package com.safecircle.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safecircle.data.model.User
import com.safecircle.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.net.Uri

/**
 * ViewModel for authentication operations
 */
class AuthViewModel : ViewModel() {
    
    private val authRepository = AuthRepository()
    
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState
    
    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState
    
    private val _updateProfileState = MutableStateFlow<UpdateProfileState>(UpdateProfileState.Idle)
    val updateProfileState: StateFlow<UpdateProfileState> = _updateProfileState
    
    private val _imageUploadState = MutableStateFlow<ImageUploadState>(ImageUploadState.Idle)
    val imageUploadState: StateFlow<ImageUploadState> = _imageUploadState

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        // Check if user is already logged in
        viewModelScope.launch {
            _currentUser.value = authRepository.getCurrentUser()
        }
    }

    /**
     * Upload profile image
     */
    fun uploadProfileImage(uri: Uri) {
        _imageUploadState.value = ImageUploadState.Loading
        
        viewModelScope.launch {
            val result = authRepository.uploadProfileImage(uri)
            result.fold(
                onSuccess = { url ->
                    _imageUploadState.value = ImageUploadState.Success(url)
                },
                onFailure = { exception ->
                    _imageUploadState.value = ImageUploadState.Error(exception.message ?: "Upload failed")
                }
            )
        }
    }

    /**
     * Reset image upload state
     */
    fun resetImageUploadState() {
        _imageUploadState.value = ImageUploadState.Idle
    }

    /**
     * Update user profile
     */
    fun updateProfile(newName: String, age: Int, gender: String, profileImageUrl: String) {
        if (newName.isBlank()) {
            _updateProfileState.value = UpdateProfileState.Error("Name cannot be empty")
            return
        }
        
        if (newName.length < 3) {
            _updateProfileState.value = UpdateProfileState.Error("Name must be at least 3 characters")
            return
        }

        if (age < 12 || age > 120) {
            _updateProfileState.value = UpdateProfileState.Error("Please enter a valid age (12-120)")
            return
        }

        _updateProfileState.value = UpdateProfileState.Loading
        
        viewModelScope.launch {
            val result = authRepository.updateProfile(newName, age, gender, profileImageUrl)
            result.fold(
                onSuccess = {
                    val updatedUser = _currentUser.value?.copy(
                        name = newName,
                        age = age,
                        gender = gender,
                        profileImageUrl = profileImageUrl
                    )
                    _currentUser.value = updatedUser
                    _updateProfileState.value = UpdateProfileState.Success
                },
                onFailure = { exception ->
                    _updateProfileState.value = UpdateProfileState.Error(exception.message ?: "Update failed")
                }
            )
        }
    }

    /**
     * Reset update profile state
     */
    fun resetUpdateProfileState() {
        _updateProfileState.value = UpdateProfileState.Idle
    }

    /**
     * Login user with email and password
     */
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Email and password cannot be empty")
            return
        }

        if (!isValidEmail(email)) {
            _loginState.value = LoginState.Error("Invalid email format")
            return
        }

        _loginState.value = LoginState.Loading
        
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    _loginState.value = LoginState.Success(user)
                },
                onFailure = { exception ->
                    _loginState.value = LoginState.Error(exception.message ?: "Login failed")
                }
            )
        }
    }

    /**
     * Register new user
     */
    fun register(name: String, email: String, password: String, confirmPassword: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _registerState.value = RegisterState.Error("All fields are required")
            return
        }

        if (!isValidEmail(email)) {
            _registerState.value = RegisterState.Error("Invalid email format")
            return
        }

        if (password.length < 6) {
            _registerState.value = RegisterState.Error("Password must be at least 6 characters")
            return
        }

        if (password != confirmPassword) {
            _registerState.value = RegisterState.Error("Passwords do not match")
            return
        }

        _registerState.value = RegisterState.Loading
        
        viewModelScope.launch {
            val result = authRepository.register(name, email, password)
            result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    _registerState.value = RegisterState.Success(user)
                },
                onFailure = { exception ->
                    _registerState.value = RegisterState.Error(exception.message ?: "Registration failed")
                }
            )
        }
    }

    /**
     * Toggle dark mode
     */
    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
    }

    /**
     * Logout current user
     */
    fun logout() {
        authRepository.logout()
        _currentUser.value = null
        _loginState.value = LoginState.Idle
        _registerState.value = RegisterState.Idle
    }

    /**
     * Check if user is logged in
     */
    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }

    /**
     * Reset login state
     */
    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }

    /**
     * Reset register state
     */
    fun resetRegisterState() {
        _registerState.value = RegisterState.Idle
    }

    /**
     * Validate email format
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

/**
 * Update profile state sealed class
 */
sealed class UpdateProfileState {
    object Idle : UpdateProfileState()
    object Loading : UpdateProfileState()
    object Success : UpdateProfileState()
    data class Error(val message: String) : UpdateProfileState()
}

/**
 * Image upload state sealed class
 */
sealed class ImageUploadState {
    object Idle : ImageUploadState()
    object Loading : ImageUploadState()
    data class Success(val imageUrl: String) : ImageUploadState()
    data class Error(val message: String) : ImageUploadState()
}

/**
 * Login state sealed class
 */
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}

/**
 * Register state sealed class
 */
sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val user: User) : RegisterState()
    data class Error(val message: String) : RegisterState()
}
