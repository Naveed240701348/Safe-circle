package com.safecircle.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safecircle.data.model.User
import com.safecircle.repository.FriendRepository
import com.safecircle.repository.SOSRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    private val friendRepository = FriendRepository()
    private val sosRepository = SOSRepository()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val friendsResult = friendRepository.getFriends()
                val sosHistoryResult = sosRepository.getSOSHistory()

                val friends = friendsResult.getOrDefault(emptyList())
                val sosHistory = sosHistoryResult.getOrDefault(emptyList())

                _uiState.value = DashboardUiState(
                    friends = friends,
                    alertCount = sosHistory.size,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load dashboard"
                )
            }
        }
    }
}

data class DashboardUiState(
    val friends: List<User> = emptyList(),
    val alertCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)
