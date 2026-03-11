package com.viziatech.vizia.repository

import android.util.Log
import com.viziatech.vizia.SupaBaseHelper
import com.viziatech.vizia.entity.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object UserProfileRepository {
    private val client = SupaBaseHelper.client

    /**
     * 获取当前用户的 Profile 信息
     */
    suspend fun fetchProfile(): UserProfile? = withContext(Dispatchers.IO) {
        try {
            val user = client.auth.currentUserOrNull() ?: return@withContext null
            client.postgrest["profiles"]
                .select { filter { eq("id", user.id) } }
                .decodeSingleOrNull<UserProfile>()
        } catch (e: Exception) {
            Log.e("UserProfileRepo", "获取 Profile 失败", e)
            null
        }
    }

    /**
     * 更新或插入用户昵称
     */
    suspend fun upsertNickname(newNickname: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = client.auth.currentUserOrNull()
                ?: return@withContext Result.failure(Exception("用户未登录"))

            val newProfile = UserProfile(
                id = user.id,
                nickname = newNickname
            )

            client.postgrest["profiles"].upsert(newProfile)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("UserProfileRepo", "更新昵称失败", e)
            Result.failure(e)
        }
    }

    /**
     * 退出登录并清理本地缓存
     */
    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            client.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}