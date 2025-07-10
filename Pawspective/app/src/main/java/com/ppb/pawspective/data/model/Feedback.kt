package com.ppb.pawspective.data.model

data class FeedbackRequest(
    val feedback: String
)

data class FeedbackResponse(
    val success: Boolean,
    val message: String
) 