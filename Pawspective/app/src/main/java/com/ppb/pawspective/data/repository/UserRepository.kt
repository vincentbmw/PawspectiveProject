package com.ppb.pawspective.data.repository

import android.content.Context
import com.ppb.pawspective.data.local.UserDatabase
import com.ppb.pawspective.data.model.User

class UserRepository(context: Context) {
    private val userDatabase = UserDatabase(context)
    
    /**
     * Save user data to local database
     */
    fun saveUser(user: User): Boolean {
        return userDatabase.saveUser(user)
    }
    
    /**
     * Get user data from local database
     */
    fun getUser(userId: String): User? {
        return userDatabase.getUser(userId)
    }
    
    /**
     * Update user profile information
     */
    fun updateUserProfile(userId: String, username: String?, fullName: String?): Boolean {
        return userDatabase.updateUserProfile(userId, username, fullName)
    }
    
    /**
     * Delete user data from local database
     */
    fun deleteUser(userId: String): Boolean {
        return userDatabase.deleteUser(userId)
    }
    
    /**
     * Create user with basic info (called after login/register)
     */
    fun createUserFromAuth(userId: String, email: String): User {
        val user = User(
            userId = userId,
            email = email,
            username = "",
            fullName = "",
            profileCompleted = false,
            lastSyncTimestamp = System.currentTimeMillis()
        )
        saveUser(user)
        return user
    }
    
    /**
     * Get user with fallback to create if not exists
     */
    fun getUserOrCreate(userId: String, email: String): User {
        return getUser(userId) ?: createUserFromAuth(userId, email)
    }
} 