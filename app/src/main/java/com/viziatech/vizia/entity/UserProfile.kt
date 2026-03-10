package com.viziatech.vizia.entity

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String? = null,
    val nickname: String,
)