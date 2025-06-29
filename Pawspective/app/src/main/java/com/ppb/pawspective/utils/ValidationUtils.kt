package com.ppb.pawspective.utils

import android.util.Patterns
import java.util.regex.Pattern

object ValidationUtils {
    
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )
    
    /**
     * Validates email address
     * Checks for: empty, format, length
     */
    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult(false, "Email is required")
            email.length > 254 -> ValidationResult(false, "Email is too long")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> ValidationResult(false, "Please enter a valid email address")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates password with comprehensive rules
     * Checks for: length, complexity, common patterns
     */
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult(false, "Password is required")
            password.length < 8 -> ValidationResult(false, "Password must be at least 8 characters long")
            password.length > 128 -> ValidationResult(false, "Password is too long (max 128 characters)")
            !password.any { it.isUpperCase() } -> ValidationResult(false, "Password must contain at least one uppercase letter")
            !password.any { it.isLowerCase() } -> ValidationResult(false, "Password must contain at least one lowercase letter")
            !password.any { it.isDigit() } -> ValidationResult(false, "Password must contain at least one number")
            !containsSpecialCharacter(password) -> ValidationResult(false, "Password must contain at least one special character (!@#$%^&*)")
            isCommonPassword(password) -> ValidationResult(false, "This password is too common. Please choose a stronger password")
            containsSequentialChars(password) -> ValidationResult(false, "Password cannot contain sequential characters (e.g., 123, abc)")
            containsRepeatingChars(password) -> ValidationResult(false, "Password cannot contain more than 2 repeating characters")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates password confirmation
     */
    fun validatePasswordConfirmation(password: String, confirmPassword: String): ValidationResult {
        return when {
            confirmPassword.isBlank() -> ValidationResult(false, "Please confirm your password")
            password != confirmPassword -> ValidationResult(false, "Passwords do not match")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Check if password contains special characters
     */
    private fun containsSpecialCharacter(password: String): Boolean {
        val specialCharPattern = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]")
        return specialCharPattern.matcher(password).find()
    }
    
    /**
     * Check for common/weak passwords
     */
    private fun isCommonPassword(password: String): Boolean {
        val commonPasswords = listOf(
            "password", "12345678", "qwerty123", "abc123456", "password123",
            "admin123", "letmein123", "welcome123", "monkey123", "dragon123",
            "sunshine123", "iloveyou123", "princess123", "rockyou123", "123456789"
        )
        return commonPasswords.any { it.equals(password, ignoreCase = true) }
    }
    
    /**
     * Check for sequential characters (123, abc, etc.)
     */
    private fun containsSequentialChars(password: String): Boolean {
        val sequences = listOf(
            "123456789", "abcdefghijklmnopqrstuvwxyz", "qwertyuiop", "asdfghjkl", "zxcvbnm"
        )
        
        for (sequence in sequences) {
            for (i in 0..sequence.length - 4) {
                val subSeq = sequence.substring(i, i + 4)
                if (password.contains(subSeq, ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }
    
    /**
     * Check for more than 2 repeating characters
     */
    private fun containsRepeatingChars(password: String): Boolean {
        var count = 1
        for (i in 1 until password.length) {
            if (password[i] == password[i - 1]) {
                count++
                if (count > 2) return true
            } else {
                count = 1
            }
        }
        return false
    }
    
    /**
     * Get password strength level
     */
    fun getPasswordStrength(password: String): PasswordStrength {
        if (password.length < 8) return PasswordStrength.WEAK
        
        var score = 0
        
        // Length bonus
        if (password.length >= 12) score += 2
        else if (password.length >= 10) score += 1
        
        // Character variety
        if (password.any { it.isUpperCase() }) score += 1
        if (password.any { it.isLowerCase() }) score += 1
        if (password.any { it.isDigit() }) score += 1
        if (containsSpecialCharacter(password)) score += 1
        
        // Complexity bonus
        if (password.length >= 16 && score >= 4) score += 1
        
        return when {
            score >= 7 -> PasswordStrength.VERY_STRONG
            score >= 5 -> PasswordStrength.STRONG
            score >= 3 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
    }
    
    enum class PasswordStrength(val label: String, val color: androidx.compose.ui.graphics.Color) {
        WEAK("Weak", androidx.compose.ui.graphics.Color.Red),
        MEDIUM("Medium", androidx.compose.ui.graphics.Color(0xFFFF9800)),
        STRONG("Strong", androidx.compose.ui.graphics.Color(0xFF4CAF50)),
        VERY_STRONG("Very Strong", androidx.compose.ui.graphics.Color(0xFF2E7D32))
    }
} 