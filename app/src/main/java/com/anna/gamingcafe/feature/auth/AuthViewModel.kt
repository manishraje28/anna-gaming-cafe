package com.anna.gamingcafe.feature.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anna.gamingcafe.data.remote.AuthRepository
import com.anna.gamingcafe.data.remote.ProfileRepository
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isOtpSent: Boolean = false,
    val isAuthenticated: Boolean = false,
    val userRole: String? = null,
    val error: String? = null,
    val phone: String = "",
    val otp: String = "",
    val fullName: String = "",
    val isNewUser: Boolean = false
)

class AuthViewModel : ViewModel() {

    var uiState by mutableStateOf(AuthUiState())
        private set

    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            try {
                val userId = AuthRepository.getCurrentUserId()
                if (userId != null) {
                    val profile = ProfileRepository.getProfile(userId)
                    uiState = uiState.copy(
                        isAuthenticated = true,
                        userRole = profile?.role ?: "CUSTOMER"
                    )
                }
            } catch (_: Exception) { }
        }
    }

    fun updatePhone(phone: String) {
        uiState = uiState.copy(phone = phone, error = null)
    }

    fun updateOtp(otp: String) {
        uiState = uiState.copy(otp = otp, error = null)
    }

    fun updateFullName(name: String) {
        uiState = uiState.copy(fullName = name, error = null)
    }

    fun sendOtp() {
        if (uiState.phone.length < 10) {
            uiState = uiState.copy(error = "Enter a valid 10-digit phone number")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val formattedPhone = if (uiState.phone.startsWith("+91")) uiState.phone
                                     else "+91${uiState.phone}"
                AuthRepository.sendOtp(formattedPhone)
                uiState = uiState.copy(isLoading = false, isOtpSent = true)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to send OTP. Please try again."
                )
            }
        }
    }

    fun verifyOtp() {
        if (uiState.otp.length != 6) {
            uiState = uiState.copy(error = "Enter 6-digit OTP")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val formattedPhone = if (uiState.phone.startsWith("+91")) uiState.phone
                                     else "+91${uiState.phone}"
                AuthRepository.verifyOtp(formattedPhone, uiState.otp)

                // Check if profile exists and get role
                val userId = AuthRepository.getCurrentUserId()
                if (userId != null) {
                    val profile = ProfileRepository.getProfile(userId)
                    if (profile != null && profile.fullName.isNotBlank()) {
                        uiState = uiState.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            userRole = profile.role
                        )
                    } else {
                        // New user — need name
                        uiState = uiState.copy(isLoading = false, isNewUser = true)
                    }
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Invalid OTP. Please try again."
                )
            }
        }
    }

    fun completeProfile() {
        if (uiState.fullName.isBlank()) {
            uiState = uiState.copy(error = "Please enter your name")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val userId = AuthRepository.getCurrentUserId() ?: return@launch
                ProfileRepository.updateProfile(userId, uiState.fullName)
                val profile = ProfileRepository.getProfile(userId)
                uiState = uiState.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    userRole = profile?.role ?: "CUSTOMER",
                    isNewUser = false
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save profile"
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                AuthRepository.signOut()
                uiState = AuthUiState()
            } catch (_: Exception) { }
        }
    }
}
