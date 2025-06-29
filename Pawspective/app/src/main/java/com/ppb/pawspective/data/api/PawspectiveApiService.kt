package com.ppb.pawspective.data.api

import com.ppb.pawspective.data.model.ChatResponse
import com.ppb.pawspective.data.model.CreateChatRequest
import com.ppb.pawspective.data.model.CreateChatResponse
import com.ppb.pawspective.data.model.SendMessageRequest
import com.ppb.pawspective.data.model.SendMessageResponse
import com.ppb.pawspective.data.model.ChatMessagesResponse
import com.ppb.pawspective.data.model.SendChatMessageRequest
import com.ppb.pawspective.data.model.SendChatMessageResponse
import com.ppb.pawspective.data.model.RenameChatRequest
import com.ppb.pawspective.data.model.RenameChatResponse
import com.ppb.pawspective.data.model.StarChatRequest
import com.ppb.pawspective.data.model.StarChatResponse
import retrofit2.Response
import retrofit2.http.*

interface PawspectiveApiService {
    
    @GET("api/chats/{user_id}")
    suspend fun getUserChats(@Path("user_id") userId: String): Response<ChatResponse>
    
    @POST("api/chats/{user_id}")
    suspend fun createChat(
        @Path("user_id") userId: String,
        @Body request: CreateChatRequest
    ): Response<CreateChatResponse>
    
    @DELETE("api/chats/{user_id}/{chat_id}")
    suspend fun deleteChat(
        @Path("user_id") userId: String,
        @Path("chat_id") chatId: String
    ): Response<Void>
    
    @PUT("api/chats/{user_id}/{chat_id}/rename")
    suspend fun renameChat(
        @Path("user_id") userId: String,
        @Path("chat_id") chatId: String,
        @Body request: RenameChatRequest
    ): Response<RenameChatResponse>
    
    @PUT("api/chats/{user_id}/{chat_id}/star")
    suspend fun starChat(
        @Path("user_id") userId: String,
        @Path("chat_id") chatId: String,
        @Body request: StarChatRequest
    ): Response<StarChatResponse>
    
    @GET("api/chats/{user_id}/{chat_id}/messages")
    suspend fun getChatMessages(
        @Path("user_id") userId: String,
        @Path("chat_id") chatId: String
    ): Response<ChatMessagesResponse>
    
    @POST("api/chats/{user_id}/{chat_id}/messages")
    suspend fun sendChatMessage(
        @Path("user_id") userId: String,
        @Path("chat_id") chatId: String,
        @Body request: SendChatMessageRequest
    ): Response<SendChatMessageResponse>
    
    @POST("api/query/{user_id}")
    suspend fun sendMessage(
        @Path("user_id") userId: String,
        @Body request: SendMessageRequest
    ): Response<SendMessageResponse>
} 