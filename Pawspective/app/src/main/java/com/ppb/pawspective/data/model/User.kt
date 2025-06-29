package com.ppb.pawspective.data.model

data class User(
    val userId: String,
    val email: String,
    var username: String = "",
    var fullName: String = "",
    var profileCompleted: Boolean = false,
    var lastSyncTimestamp: Long = System.currentTimeMillis()
) 