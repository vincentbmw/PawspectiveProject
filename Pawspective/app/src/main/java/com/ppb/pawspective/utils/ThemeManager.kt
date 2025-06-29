package com.ppb.pawspective.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class ThemeManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_IS_DARK_MODE = "is_dark_mode"
        
        @Volatile
        private var INSTANCE: ThemeManager? = null
        
        fun getInstance(context: Context): ThemeManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThemeManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    // Mutable state for theme
    var isDarkMode by mutableStateOf(getDarkMode())
        private set
    
    /**
     * Get current dark mode preference
     */
    fun getDarkMode(): Boolean {
        return prefs.getBoolean(KEY_IS_DARK_MODE, false) // Default to light mode
    }
    
    /**
     * Update dark mode preference
     */
    fun updateDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean(KEY_IS_DARK_MODE, isDark).apply()
        isDarkMode = isDark
    }
    
    /**
     * Toggle between dark and light mode
     */
    fun toggleTheme() {
        updateDarkMode(!isDarkMode)
    }
    
    /**
     * Clear theme preferences (reset to default)
     */
    fun clearThemePreferences() {
        prefs.edit().remove(KEY_IS_DARK_MODE).apply()
        isDarkMode = false
    }
} 