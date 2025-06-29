package com.ppb.pawspective.data.repository

import android.content.Context
import android.util.Log
import com.ppb.pawspective.data.api.PawspectiveApiService
import com.ppb.pawspective.data.local.ChatDatabase
import com.ppb.pawspective.data.model.Chat
import com.ppb.pawspective.data.model.CreateChatRequest
import com.ppb.pawspective.data.model.SendMessageRequest
import com.ppb.pawspective.data.model.SendMessageResponse
import com.ppb.pawspective.data.model.ChatMessage
import com.ppb.pawspective.data.model.SendChatMessageRequest
import com.ppb.pawspective.data.model.SendChatMessageResponse
import com.ppb.pawspective.data.model.RenameChatRequest
import com.ppb.pawspective.data.model.StarChatRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatRepository(
    private val apiService: PawspectiveApiService,
    private val context: Context
) {
    
    companion object {
        private const val TAG = "ChatRepository"
    }
    
    private val chatDatabase by lazy { ChatDatabase(context) }
    
    sealed class Result<T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error<T>(val message: String) : Result<T>()
        data class Loading<T>(val isLoading: Boolean = true) : Result<T>()
    }
    
    suspend fun getUserChats(userId: String, forceRefresh: Boolean = false): Result<List<Chat>> {
        return withContext(Dispatchers.IO) {
            try {
                // Check if we should use local data first
                if (!forceRefresh && chatDatabase.hasLocalChats(userId)) {
                    Log.d(TAG, "getUserChats: Loading from local database")
                    val localChats = chatDatabase.getUserChats(userId)
                    if (localChats.isNotEmpty()) {
                        Log.d(TAG, "getUserChats: Loaded ${localChats.size} chats from local database")
                        return@withContext Result.Success(localChats)
                    }
                }
                
                // Fetch from API
                Log.d(TAG, "getUserChats: Fetching chats from API for user: $userId")
                val response = apiService.getUserChats(userId)
                
                Log.d(TAG, "getUserChats: Response received - Code: ${response.code()}, Success: ${response.isSuccessful}")
                
                if (response.isSuccessful) {
                    val chatResponse = response.body()
                    Log.d(TAG, "getUserChats: Response body: $chatResponse")
                    
                    if (chatResponse != null && chatResponse.success) {
                        Log.d(TAG, "getUserChats: Successfully fetched ${chatResponse.chats.size} chats from API")
                        
                        // Save to local database
                        val saveSuccess = chatDatabase.saveChats(userId, chatResponse.chats)
                        Log.d(TAG, "getUserChats: Local save ${if (saveSuccess) "successful" else "failed"}")
                        
                        Result.Success(chatResponse.chats)
                    } else {
                        Log.e(TAG, "getUserChats: API returned success=false or null response")
                        
                        // Fallback to local data if API fails
                        val localChats = chatDatabase.getUserChats(userId)
                        if (localChats.isNotEmpty()) {
                            Log.d(TAG, "getUserChats: Using local fallback data")
                            Result.Success(localChats)
                        } else {
                            Result.Error("Failed to fetch chats: API returned success=false")
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "getUserChats: HTTP error ${response.code()}: ${response.message()}")
                    
                    // Fallback to local data if API fails
                    val localChats = chatDatabase.getUserChats(userId)
                    if (localChats.isNotEmpty()) {
                        Log.d(TAG, "getUserChats: Using local fallback data due to API error")
                        Result.Success(localChats)
                    } else {
                        Result.Error("Network error: ${response.code()} ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "getUserChats: Exception occurred", e)
                
                // Fallback to local data on exception
                try {
                    val localChats = chatDatabase.getUserChats(userId)
                    if (localChats.isNotEmpty()) {
                        Log.d(TAG, "getUserChats: Using local fallback data due to exception")
                        Result.Success(localChats)
                    } else {
                        Result.Error("Network error: ${e.message}")
                    }
                } catch (localException: Exception) {
                    Log.e(TAG, "getUserChats: Local fallback also failed", localException)
                    Result.Error("Network error: ${e.message}")
                }
            }
        }
    }
    
    suspend fun createChat(userId: String, title: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "createChat: Creating chat for user: $userId with title: $title")
                
                val request = CreateChatRequest(title)
                val response = apiService.createChat(userId, request)
                
                if (response.isSuccessful) {
                    val createResponse = response.body()
                    if (createResponse != null && createResponse.success) {
                        Log.d(TAG, "createChat: Successfully created chat with ID: ${createResponse.chatId}")
                        
                        // Refresh local cache after creating new chat
                        getUserChats(userId, forceRefresh = true)
                        
                        Result.Success(createResponse.chatId)
                    } else {
                        Log.e(TAG, "createChat: API returned success=false")
                        Result.Error("Failed to create chat")
                    }
                } else {
                    Log.e(TAG, "createChat: HTTP error ${response.code()}: ${response.message()}")
                    Result.Error("Network error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "createChat: Exception occurred", e)
                Result.Error("Network error: ${e.message}")
            }
        }
    }
    
    suspend fun deleteChat(userId: String, chatId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "deleteChat: Deleting chat $chatId for user: $userId")
                
                val response = apiService.deleteChat(userId, chatId)
                
                if (response.isSuccessful) {
                    Log.d(TAG, "deleteChat: Successfully deleted chat from API")
                    
                    // Delete from local database
                    val localDeleteSuccess = chatDatabase.deleteChat(chatId)
                    Log.d(TAG, "deleteChat: Local delete ${if (localDeleteSuccess) "successful" else "failed"}")
                    
                    Result.Success(Unit)
                } else {
                    Log.e(TAG, "deleteChat: HTTP error ${response.code()}: ${response.message()}")
                    Result.Error("Failed to delete chat: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "deleteChat: Exception occurred", e)
                Result.Error("Network error: ${e.message}")
            }
        }
    }
    
    suspend fun renameChat(userId: String, chatId: String, newTitle: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "renameChat: Renaming chat $chatId for user: $userId to: $newTitle")
                
                // Update local database first for immediate UI feedback
                val localUpdateSuccess = chatDatabase.updateChatTitle(chatId, newTitle)
                Log.d(TAG, "renameChat: Local update ${if (localUpdateSuccess) "successful" else "failed"}")
                
                // Then update on server
                val request = RenameChatRequest(newTitle)
                val response = apiService.renameChat(userId, chatId, request)
                
                if (response.isSuccessful) {
                    val renameResponse = response.body()
                    if (renameResponse != null && renameResponse.success) {
                        Log.d(TAG, "renameChat: Successfully renamed chat on server")
                        Result.Success(Unit)
                    } else {
                        Log.e(TAG, "renameChat: API returned success=false")
                        // Revert local change if API fails
                        // Note: We'd need the original title to revert properly
                        Result.Error("Failed to rename chat")
                    }
                } else {
                    Log.e(TAG, "renameChat: HTTP error ${response.code()}: ${response.message()}")
                    // Local change remains for offline functionality
                    Result.Success(Unit) // Return success since local update worked
                }
            } catch (e: Exception) {
                Log.e(TAG, "renameChat: Exception occurred", e)
                // Local change remains for offline functionality
                Result.Success(Unit) // Return success since local update worked
            }
        }
    }
    
    suspend fun starChat(userId: String, chatId: String, isStarred: Boolean): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "starChat: ${if (isStarred) "Starring" else "Unstarring"} chat $chatId for user: $userId")
                
                // Update local database first for immediate UI feedback
                val localUpdateSuccess = chatDatabase.updateChatStarStatus(chatId, isStarred)
                Log.d(TAG, "starChat: Local update ${if (localUpdateSuccess) "successful" else "failed"}")
                
                // Then update on server
                val request = StarChatRequest(isStarred)
                val response = apiService.starChat(userId, chatId, request)
                
                if (response.isSuccessful) {
                    val starResponse = response.body()
                    if (starResponse != null && starResponse.success) {
                        Log.d(TAG, "starChat: Successfully ${if (isStarred) "starred" else "unstarred"} chat on server")
                        Result.Success(Unit)
                    } else {
                        Log.e(TAG, "starChat: API returned success=false")
                        // Revert local change if API fails
                        chatDatabase.updateChatStarStatus(chatId, !isStarred)
                        Result.Error("Failed to ${if (isStarred) "star" else "unstar"} chat")
                    }
                } else {
                    Log.e(TAG, "starChat: HTTP error ${response.code()}: ${response.message()}")
                    // Local change remains for offline functionality
                    Result.Success(Unit) // Return success since local update worked
                }
            } catch (e: Exception) {
                Log.e(TAG, "starChat: Exception occurred", e)
                // Local change remains for offline functionality
                Result.Success(Unit) // Return success since local update worked
            }
        }
    }
    
    suspend fun getChatMessages(userId: String, chatId: String): Result<List<ChatMessage>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "getChatMessages: Fetching messages for chat $chatId, user: $userId")
                
                val response = apiService.getChatMessages(userId, chatId)
                
                if (response.isSuccessful) {
                    val messagesResponse = response.body()
                    if (messagesResponse != null && messagesResponse.success) {
                        Log.d(TAG, "getChatMessages: Successfully fetched ${messagesResponse.messages.size} messages")
                        Result.Success(messagesResponse.messages)
                    } else {
                        Log.e(TAG, "getChatMessages: API returned success=false")
                        Result.Error("Failed to fetch messages")
                    }
                } else {
                    Log.e(TAG, "getChatMessages: HTTP error ${response.code()}: ${response.message()}")
                    Result.Error("Network error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "getChatMessages: Exception occurred", e)
                Result.Error("Network error: ${e.message}")
            }
        }
    }
    
    suspend fun sendChatMessage(userId: String, chatId: String, message: String): Result<SendChatMessageResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "sendChatMessage: Sending message to chat $chatId for user: $userId")
                
                val request = SendChatMessageRequest(message)
                val response = apiService.sendChatMessage(userId, chatId, request)
                
                if (response.isSuccessful) {
                    val messageResponse = response.body()
                    if (messageResponse != null && messageResponse.success) {
                        Log.d(TAG, "sendChatMessage: Successfully sent message to existing chat")
                        
                        // Update local chat data after sending message
                        getUserChats(userId, forceRefresh = true)
                        
                        Result.Success(messageResponse)
                    } else {
                        Log.e(TAG, "sendChatMessage: API returned success=false")
                        Result.Error("Failed to send message")
                    }
                } else {
                    Log.e(TAG, "sendChatMessage: HTTP error ${response.code()}: ${response.message()}")
                    Result.Error("Network error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "sendChatMessage: Exception occurred", e)
                Result.Error("Network error: ${e.message}")
            }
        }
    }
    
    suspend fun sendMessage(userId: String, query: String): Result<SendMessageResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "sendMessage: Sending query for user: $userId")
                
                val request = SendMessageRequest(query)
                val response = apiService.sendMessage(userId, request)
                
                if (response.isSuccessful) {
                    val messageResponse = response.body()
                    if (messageResponse != null && messageResponse.success) {
                        Log.d(TAG, "sendMessage: Successfully sent query")
                        
                        // Refresh local cache after creating new chat
                        getUserChats(userId, forceRefresh = true)
                        
                        Result.Success(messageResponse)
                    } else {
                        Log.e(TAG, "sendMessage: API returned success=false")
                        Result.Error("Failed to send query")
                    }
                } else {
                    Log.e(TAG, "sendMessage: HTTP error ${response.code()}: ${response.message()}")
                    Result.Error("Network error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "sendMessage: Exception occurred", e)
                Result.Error("Network error: ${e.message}")
            }
        }
    }
    
    /**
     * Clear local cache for user (useful for logout)
     */
    suspend fun clearLocalCache(userId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                chatDatabase.deleteAllUserChats(userId)
            } catch (e: Exception) {
                Log.e(TAG, "clearLocalCache: Exception occurred", e)
                false
            }
        }
    }
} 