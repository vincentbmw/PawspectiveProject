package com.ppb.pawspective.data.model

data class Chat(
    val id: String,
    val title: String,
    val preview: String,
    val lastMessage: String?,
    val lastSender: String?,
    val messageCount: Int,
    val createdAt: String?,
    val updatedAt: String?,
    val isStarred: Boolean = false
)

data class ChatResponse(
    val success: Boolean,
    val chats: List<Chat>,
    val totalChats: Int
)

data class CreateChatRequest(
    val title: String
)

data class CreateChatResponse(
    val success: Boolean,
    val message: String,
    val chatId: String
)

data class SendMessageRequest(
    val query: String
)

data class SendMessageResponse(
    val success: Boolean,
    val message: String,
    val response: String?,
    val chatId: String?
)

// New models for chat messages
data class ChatMessage(
    val id: String,
    val sender: String,
    val message: String,
    val timestamp: String?
)

data class ChatMessagesResponse(
    val success: Boolean,
    val messages: List<ChatMessage>
)

data class SendChatMessageRequest(
    val message: String
)

data class SendChatMessageResponse(
    val success: Boolean,
    val message: String,
    val response: String?,
    val chatId: String
)

// New models for rename and star operations
data class RenameChatRequest(
    val title: String
)

data class RenameChatResponse(
    val success: Boolean,
    val message: String
)

data class StarChatRequest(
    val isStarred: Boolean
)

data class StarChatResponse(
    val success: Boolean,
    val message: String
) 