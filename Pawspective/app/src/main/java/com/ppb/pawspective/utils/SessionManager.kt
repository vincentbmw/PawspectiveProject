package com.ppb.pawspective.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class SessionManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()
    
    companion object {
        private const val PREF_NAME = "pawspective_session"
        private const val TAG = "SessionManager"
        
        // Session Keys
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_LOGIN_METHOD = "login_method"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_LAST_LOGIN_TIME = "last_login_time"
        private const val KEY_SESSION_TIMEOUT = "session_timeout"
        
        // App Preferences Keys
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_FIRST_TIME_HOME = "first_time_home"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        private const val KEY_AUTO_BACKUP = "auto_backup"
        
        // Login Methods
        const val LOGIN_METHOD_EMAIL = "email"
        const val LOGIN_METHOD_GOOGLE = "google"
        
        // Session timeout (7 days in milliseconds)
        private const val SESSION_TIMEOUT_DURATION = 7 * 24 * 60 * 60 * 1000L
    }
    
    /**
     * Create login session
     */
    fun createLoginSession(
        userId: String,
        email: String,
        name: String? = null,
        loginMethod: String = LOGIN_METHOD_EMAIL,
        rememberMe: Boolean = true
    ) {
        Log.d(TAG, "Creating login session for user: $email")
        
        editor.apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, name ?: "")
            putString(KEY_LOGIN_METHOD, loginMethod)
            putBoolean(KEY_REMEMBER_ME, rememberMe)
            putLong(KEY_LAST_LOGIN_TIME, System.currentTimeMillis())
            putLong(KEY_SESSION_TIMEOUT, System.currentTimeMillis() + SESSION_TIMEOUT_DURATION)
            apply()
        }
        
        Log.d(TAG, "Login session created successfully")
    }
    
    /**
     * Check if user is logged in and session is valid
     */
    fun isLoggedIn(): Boolean {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val rememberMe = prefs.getBoolean(KEY_REMEMBER_ME, false)
        val sessionTimeout = prefs.getLong(KEY_SESSION_TIMEOUT, 0)
        val currentTime = System.currentTimeMillis()
        
        Log.d(TAG, "Checking login status - isLoggedIn: $isLoggedIn, rememberMe: $rememberMe")
        
        return if (isLoggedIn) {
            if (rememberMe) {
                // If remember me is enabled, check session timeout
                if (currentTime < sessionTimeout) {
                    Log.d(TAG, "Session is valid (remember me enabled)")
                    true
                } else {
                    Log.d(TAG, "Session expired, clearing session")
                    clearSession()
                    false
                }
            } else {
                // If remember me is disabled, session is only valid for current app session
                Log.d(TAG, "Session valid for current app session only")
                true
            }
        } else {
            Log.d(TAG, "User not logged in")
            false
        }
    }
    
    /**
     * Get user details from session
     */
    fun getUserDetails(): UserSession? {
        return if (isLoggedIn()) {
            UserSession(
                userId = prefs.getString(KEY_USER_ID, "") ?: "",
                email = prefs.getString(KEY_USER_EMAIL, "") ?: "",
                name = prefs.getString(KEY_USER_NAME, "") ?: "",
                loginMethod = prefs.getString(KEY_LOGIN_METHOD, LOGIN_METHOD_EMAIL) ?: LOGIN_METHOD_EMAIL,
                rememberMe = prefs.getBoolean(KEY_REMEMBER_ME, false),
                lastLoginTime = prefs.getLong(KEY_LAST_LOGIN_TIME, 0)
            )
        } else {
            null
        }
    }
    
    /**
     * Update user session data
     */
    fun updateUserSession(
        name: String? = null,
        email: String? = null
    ) {
        Log.d(TAG, "Updating user session data")
        
        editor.apply {
            name?.let { putString(KEY_USER_NAME, it) }
            email?.let { putString(KEY_USER_EMAIL, it) }
            apply()
        }
    }
    
    /**
     * Extend session timeout (when user is active)
     */
    fun extendSession() {
        if (isLoggedIn() && prefs.getBoolean(KEY_REMEMBER_ME, false)) {
            Log.d(TAG, "Extending session timeout")
            editor.putLong(KEY_SESSION_TIMEOUT, System.currentTimeMillis() + SESSION_TIMEOUT_DURATION)
            editor.apply()
        }
    }
    
    /**
     * Clear login session
     */
    fun clearSession() {
        Log.d(TAG, "Clearing login session")
        
        editor.apply {
            remove(KEY_IS_LOGGED_IN)
            remove(KEY_USER_ID)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_NAME)
            remove(KEY_LOGIN_METHOD)
            remove(KEY_REMEMBER_ME)
            remove(KEY_LAST_LOGIN_TIME)
            remove(KEY_SESSION_TIMEOUT)
            apply()
        }
        
        Log.d(TAG, "Login session cleared")
    }
    
    /**
     * Logout user completely
     */
    fun logout() {
        Log.d(TAG, "Logging out user")
        clearSession()
        // Keep app preferences like onboarding status
    }
    
    // ==================== APP PREFERENCES ====================
    
    /**
     * Set onboarding completion status
     */
    fun setOnboardingCompleted(completed: Boolean) {
        Log.d(TAG, "Setting onboarding completed: $completed")
        editor.putBoolean(KEY_ONBOARDING_COMPLETED, completed)
        editor.apply()
    }
    
    /**
     * Check if onboarding is completed
     */
    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }
    
    /**
     * Set first time home visit
     */
    fun setFirstTimeHome(isFirstTime: Boolean) {
        editor.putBoolean(KEY_FIRST_TIME_HOME, isFirstTime)
        editor.apply()
    }
    
    /**
     * Check if it's first time visiting home
     */
    fun isFirstTimeHome(): Boolean {
        return prefs.getBoolean(KEY_FIRST_TIME_HOME, true)
    }
    
    /**
     * Set theme mode preference
     */
    fun setThemeMode(themeMode: String) {
        editor.putString(KEY_THEME_MODE, themeMode)
        editor.apply()
    }
    
    /**
     * Get theme mode preference
     */
    fun getThemeMode(): String {
        return prefs.getString(KEY_THEME_MODE, "system") ?: "system"
    }
    
    /**
     * Set notification preference
     */
    fun setNotificationEnabled(enabled: Boolean) {
        editor.putBoolean(KEY_NOTIFICATION_ENABLED, enabled)
        editor.apply()
    }
    
    /**
     * Check if notifications are enabled
     */
    fun isNotificationEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATION_ENABLED, true)
    }
    
    /**
     * Set auto backup preference
     */
    fun setAutoBackupEnabled(enabled: Boolean) {
        editor.putBoolean(KEY_AUTO_BACKUP, enabled)
        editor.apply()
    }
    
    /**
     * Check if auto backup is enabled
     */
    fun isAutoBackupEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_BACKUP, true)
    }
    
    /**
     * Clear all preferences (for app reset)
     */
    fun clearAllPreferences() {
        Log.d(TAG, "Clearing all preferences")
        editor.clear()
        editor.apply()
    }
    
    /**
     * Get session info for debugging
     */
    fun getSessionInfo(): String {
        val userSession = getUserDetails()
        return if (userSession != null) {
            """
            Session Info:
            - User ID: ${userSession.userId}
            - Email: ${userSession.email}
            - Name: ${userSession.name}
            - Login Method: ${userSession.loginMethod}
            - Remember Me: ${userSession.rememberMe}
            - Last Login: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(userSession.lastLoginTime))}
            - Session Valid: ${isLoggedIn()}
            """.trimIndent()
        } else {
            "No active session"
        }
    }
}

/**
 * Data class for user session information
 */
data class UserSession(
    val userId: String,
    val email: String,
    val name: String,
    val loginMethod: String,
    val rememberMe: Boolean,
    val lastLoginTime: Long
) 