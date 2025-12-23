package com.navibharat.data.auth

import android.content.SharedPreferences
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject

data class AuthUser(
    val userId: String,
    val email: String? = null,
    val phone: String? = null,
    val name: String? = null,
    val avatar: String? = null
)

enum class AuthState {
    IDLE,
    LOADING,
    AUTHENTICATED,
    UNAUTHENTICATED,
    ERROR
}

@ActivityScoped
class AuthManager @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {

    private val _authState = MutableStateFlow<AuthState>(AuthState.IDLE)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    companion object {
        private const val PREF_USER_ID = "user_id"
        private const val PREF_USER_NAME = "user_name"
        private const val PREF_USER_EMAIL = "user_email"
        private const val PREF_USER_PHONE = "user_phone"
        private const val PREF_AUTH_TOKEN = "auth_token"
    }

    /**
     * Sign up with Google OAuth
     * In production, integrate with Supabase Auth
     */
    suspend fun signUpWithGoogle(idToken: String): Boolean {
        return try {
            _authState.value = AuthState.LOADING

            // In production, exchange idToken with Supabase
            // val response = supabaseClient.auth.signInWith(IDTokenCredentials(idToken))
            // val user = response.user

            // For now, mock sign up
            val mockUser = AuthUser(
                userId = "user_${System.currentTimeMillis()}",
                email = "user@example.com",
                name = "Driver"
            )

            saveUserLocal(mockUser)
            _currentUser.value = mockUser
            _authState.value = AuthState.AUTHENTICATED

            Timber.i("User signed up with Google: ${mockUser.userId}")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error signing up with Google")
            _authState.value = AuthState.ERROR
            _errorMessage.value = e.message
            false
        }
    }

    /**
     * Sign up with phone OTP
     * In production, integrate with Supabase Auth
     */
    suspend fun signUpWithPhone(phoneNumber: String): Boolean {
        return try {
            _authState.value = AuthState.LOADING

            // In production, call Supabase signInWith(OTP)
            // supabaseClient.auth.signInWith(OTP) { phone = phoneNumber }

            Timber.i("OTP sent to: $phoneNumber")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error sending OTP")
            _authState.value = AuthState.ERROR
            _errorMessage.value = e.message
            false
        }
    }

    /**
     * Verify OTP
     */
    suspend fun verifyOTP(phoneNumber: String, otp: String): Boolean {
        return try {
            _authState.value = AuthState.LOADING

            // In production, call Supabase verification
            // val response = supabaseClient.auth.signInWith(OTP) {
            //     phone = phoneNumber
            //     token = otp
            // }

            val mockUser = AuthUser(
                userId = "user_${System.currentTimeMillis()}",
                phone = phoneNumber,
                name = "Driver"
            )

            saveUserLocal(mockUser)
            _currentUser.value = mockUser
            _authState.value = AuthState.AUTHENTICATED

            Timber.i("User signed up with phone: $phoneNumber")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error verifying OTP")
            _authState.value = AuthState.ERROR
            _errorMessage.value = e.message
            false
        }
    }

    /**
     * Logout user
     */
    fun logout() {
        try {
            // In production, call supabaseClient.auth.signOut()

            sharedPreferences.edit().clear().apply()
            _currentUser.value = null
            _authState.value = AuthState.UNAUTHENTICATED

            Timber.i("User logged out")
        } catch (e: Exception) {
            Timber.e(e, "Error logging out")
            _errorMessage.value = e.message
        }
    }

    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        return _currentUser.value != null
    }

    /**
     * Get current user
     */
    fun getCurrentUser(): AuthUser? {
        return _currentUser.value
    }

    /**
     * Restore session from SharedPreferences
     */
    fun restoreSession() {
        try {
            val userId = sharedPreferences.getString(PREF_USER_ID, null)
            if (userId != null) {
                val user = AuthUser(
                    userId = userId,
                    email = sharedPreferences.getString(PREF_USER_EMAIL, null),
                    phone = sharedPreferences.getString(PREF_USER_PHONE, null),
                    name = sharedPreferences.getString(PREF_USER_NAME, null)
                )
                _currentUser.value = user
                _authState.value = AuthState.AUTHENTICATED
                Timber.i("Session restored for user: $userId")
            } else {
                _authState.value = AuthState.UNAUTHENTICATED
            }
        } catch (e: Exception) {
            Timber.e(e, "Error restoring session")
            _authState.value = AuthState.ERROR
        }
    }

    private fun saveUserLocal(user: AuthUser) {
        sharedPreferences.edit().apply {
            putString(PREF_USER_ID, user.userId)
            putString(PREF_USER_NAME, user.name)
            putString(PREF_USER_EMAIL, user.email)
            putString(PREF_USER_PHONE, user.phone)
            apply()
        }
    }
}
