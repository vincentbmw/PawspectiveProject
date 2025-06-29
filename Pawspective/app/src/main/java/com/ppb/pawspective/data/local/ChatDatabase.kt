package com.ppb.pawspective.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.ppb.pawspective.data.model.Chat

class ChatDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    
    companion object {
        private const val DATABASE_NAME = "pawspective_chats.db"
        private const val DATABASE_VERSION = 1
        
        // Table name
        private const val TABLE_CHATS = "chats"
        
        // Column names
        private const val COLUMN_ID = "id"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_PREVIEW = "preview"
        private const val COLUMN_LAST_MESSAGE = "last_message"
        private const val COLUMN_LAST_SENDER = "last_sender"
        private const val COLUMN_MESSAGE_COUNT = "message_count"
        private const val COLUMN_CREATED_AT = "created_at"
        private const val COLUMN_UPDATED_AT = "updated_at"
        private const val COLUMN_IS_STARRED = "is_starred"
        private const val COLUMN_LAST_SYNC = "last_sync_timestamp"
        
        // Create table SQL
        private const val CREATE_TABLE_CHATS = """
            CREATE TABLE $TABLE_CHATS (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_USER_ID TEXT NOT NULL,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_PREVIEW TEXT,
                $COLUMN_LAST_MESSAGE TEXT,
                $COLUMN_LAST_SENDER TEXT,
                $COLUMN_MESSAGE_COUNT INTEGER DEFAULT 0,
                $COLUMN_CREATED_AT TEXT,
                $COLUMN_UPDATED_AT TEXT,
                $COLUMN_IS_STARRED INTEGER DEFAULT 0,
                $COLUMN_LAST_SYNC INTEGER DEFAULT 0
            )
        """
        
        // Create index for faster queries
        private const val CREATE_INDEX_USER_ID = """
            CREATE INDEX idx_user_id ON $TABLE_CHATS($COLUMN_USER_ID)
        """
        
        private const val CREATE_INDEX_UPDATED_AT = """
            CREATE INDEX idx_updated_at ON $TABLE_CHATS($COLUMN_UPDATED_AT)
        """
    }
    
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_CHATS)
        db.execSQL(CREATE_INDEX_USER_ID)
        db.execSQL(CREATE_INDEX_UPDATED_AT)
    }
    
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHATS")
        onCreate(db)
    }
    
    /**
     * Save or update a chat
     */
    fun saveChat(userId: String, chat: Chat): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, chat.id)
            put(COLUMN_USER_ID, userId)
            put(COLUMN_TITLE, chat.title)
            put(COLUMN_PREVIEW, chat.preview)
            put(COLUMN_LAST_MESSAGE, chat.lastMessage)
            put(COLUMN_LAST_SENDER, chat.lastSender)
            put(COLUMN_MESSAGE_COUNT, chat.messageCount)
            put(COLUMN_CREATED_AT, chat.createdAt)
            put(COLUMN_UPDATED_AT, chat.updatedAt)
            put(COLUMN_IS_STARRED, if (chat.isStarred) 1 else 0)
            put(COLUMN_LAST_SYNC, System.currentTimeMillis())
        }
        
        return try {
            val result = db.insertWithOnConflict(TABLE_CHATS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            result != -1L
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }
    
    /**
     * Save multiple chats (batch operation)
     */
    fun saveChats(userId: String, chats: List<Chat>): Boolean {
        val db = this.writableDatabase
        db.beginTransaction()
        
        return try {
            chats.forEach { chat ->
                val values = ContentValues().apply {
                    put(COLUMN_ID, chat.id)
                    put(COLUMN_USER_ID, userId)
                    put(COLUMN_TITLE, chat.title)
                    put(COLUMN_PREVIEW, chat.preview)
                    put(COLUMN_LAST_MESSAGE, chat.lastMessage)
                    put(COLUMN_LAST_SENDER, chat.lastSender)
                    put(COLUMN_MESSAGE_COUNT, chat.messageCount)
                    put(COLUMN_CREATED_AT, chat.createdAt)
                    put(COLUMN_UPDATED_AT, chat.updatedAt)
                    put(COLUMN_IS_STARRED, if (chat.isStarred) 1 else 0)
                    put(COLUMN_LAST_SYNC, System.currentTimeMillis())
                }
                db.insertWithOnConflict(TABLE_CHATS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            false
        } finally {
            db.endTransaction()
            db.close()
        }
    }
    
    /**
     * Get all chats for a user, ordered by updated_at descending
     */
    fun getUserChats(userId: String): List<Chat> {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_CHATS,
            null,
            "$COLUMN_USER_ID = ?",
            arrayOf(userId),
            null,
            null,
            "$COLUMN_UPDATED_AT DESC"
        )
        
        val chats = mutableListOf<Chat>()
        
        try {
            while (cursor.moveToNext()) {
                val chat = Chat(
                    id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                    preview = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PREVIEW)) ?: "",
                    lastMessage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_MESSAGE)),
                    lastSender = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_SENDER)),
                    messageCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_COUNT)),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                    updatedAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT)),
                    isStarred = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_STARRED)) == 1
                )
                chats.add(chat)
            }
        } catch (e: Exception) {
            // Return empty list on error
        } finally {
            cursor.close()
            db.close()
        }
        
        return chats
    }
    
    /**
     * Get a specific chat by ID
     */
    fun getChat(chatId: String): Chat? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_CHATS,
            null,
            "$COLUMN_ID = ?",
            arrayOf(chatId),
            null,
            null,
            null
        )
        
        return try {
            if (cursor.moveToFirst()) {
                Chat(
                    id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                    preview = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PREVIEW)) ?: "",
                    lastMessage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_MESSAGE)),
                    lastSender = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_SENDER)),
                    messageCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_COUNT)),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                    updatedAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT)),
                    isStarred = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_STARRED)) == 1
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
     * Update chat title
     */
    fun updateChatTitle(chatId: String, newTitle: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, newTitle)
            put(COLUMN_UPDATED_AT, getCurrentTimestamp())
            put(COLUMN_LAST_SYNC, System.currentTimeMillis())
        }
        
        return try {
            val result = db.update(TABLE_CHATS, values, "$COLUMN_ID = ?", arrayOf(chatId))
            result > 0
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }
    
    /**
     * Update chat star status
     */
    fun updateChatStarStatus(chatId: String, isStarred: Boolean): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_STARRED, if (isStarred) 1 else 0)
            put(COLUMN_UPDATED_AT, getCurrentTimestamp())
            put(COLUMN_LAST_SYNC, System.currentTimeMillis())
        }
        
        return try {
            val result = db.update(TABLE_CHATS, values, "$COLUMN_ID = ?", arrayOf(chatId))
            result > 0
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }
    
    /**
     * Delete a chat
     */
    fun deleteChat(chatId: String): Boolean {
        val db = this.writableDatabase
        return try {
            val result = db.delete(TABLE_CHATS, "$COLUMN_ID = ?", arrayOf(chatId))
            result > 0
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }
    
    /**
     * Delete all chats for a user
     */
    fun deleteAllUserChats(userId: String): Boolean {
        val db = this.writableDatabase
        return try {
            val result = db.delete(TABLE_CHATS, "$COLUMN_USER_ID = ?", arrayOf(userId))
            result >= 0
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }
    
    /**
     * Check if local data exists for user
     */
    fun hasLocalChats(userId: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_CHATS,
            arrayOf("COUNT(*)"),
            "$COLUMN_USER_ID = ?",
            arrayOf(userId),
            null,
            null,
            null
        )
        
        return try {
            cursor.moveToFirst()
            cursor.getInt(0) > 0
        } catch (e: Exception) {
            false
        } finally {
            cursor.close()
            db.close()
        }
    }
    
    /**
     * Get current timestamp in ISO format
     */
    private fun getCurrentTimestamp(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(java.util.Date())
    }
} 