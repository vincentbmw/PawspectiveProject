package com.ppb.pawspective.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.ppb.pawspective.data.model.User

class UserDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    
    companion object {
        private const val DATABASE_NAME = "pawspective_user.db"
        private const val DATABASE_VERSION = 1
        
        // Table name
        private const val TABLE_USERS = "users"
        
        // Column names
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_FULL_NAME = "full_name"
        private const val COLUMN_PROFILE_COMPLETED = "profile_completed"
        private const val COLUMN_LAST_SYNC = "last_sync_timestamp"
        
        // Create table SQL
        private const val CREATE_TABLE_USERS = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID TEXT PRIMARY KEY,
                $COLUMN_EMAIL TEXT NOT NULL,
                $COLUMN_USERNAME TEXT,
                $COLUMN_FULL_NAME TEXT,
                $COLUMN_PROFILE_COMPLETED INTEGER DEFAULT 0,
                $COLUMN_LAST_SYNC INTEGER DEFAULT 0
            )
        """
    }
    
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_USERS)
    }
    
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }
    
    /**
     * Insert or update user data
     */
    fun saveUser(user: User): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, user.userId)
            put(COLUMN_EMAIL, user.email)
            put(COLUMN_USERNAME, user.username)
            put(COLUMN_FULL_NAME, user.fullName)
            put(COLUMN_PROFILE_COMPLETED, if (user.profileCompleted) 1 else 0)
            put(COLUMN_LAST_SYNC, user.lastSyncTimestamp)
        }
        
        return try {
            val result = db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            result != -1L
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }
    
    /**
     * Get user by ID
     */
    fun getUser(userId: String): User? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COLUMN_USER_ID = ?",
            arrayOf(userId),
            null,
            null,
            null
        )
        
        return try {
            if (cursor.moveToFirst()) {
                User(
                    userId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)) ?: "",
                    fullName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME)) ?: "",
                    profileCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_COMPLETED)) == 1,
                    lastSyncTimestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LAST_SYNC))
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            cursor.close()
            db.close()
        }
    }
    
    /**
     * Update user profile data
     */
    fun updateUserProfile(userId: String, username: String?, fullName: String?): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            username?.let { put(COLUMN_USERNAME, it) }
            fullName?.let { put(COLUMN_FULL_NAME, it) }
            put(COLUMN_PROFILE_COMPLETED, 1)
            put(COLUMN_LAST_SYNC, System.currentTimeMillis())
        }
        
        return try {
            val result = db.update(TABLE_USERS, values, "$COLUMN_USER_ID = ?", arrayOf(userId))
            result > 0
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }
    
    /**
     * Delete user data
     */
    fun deleteUser(userId: String): Boolean {
        val db = this.writableDatabase
        return try {
            val result = db.delete(TABLE_USERS, "$COLUMN_USER_ID = ?", arrayOf(userId))
            result > 0
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }
} 