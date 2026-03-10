package com.viziatech.vizia.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.viziatech.vizia.SupaBaseHelper
import com.viziatech.vizia.entity.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var nickname by remember { mutableStateOf("加载中...") }
    var tempNickname by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val user = SupaBaseHelper.client.auth.currentUserOrNull()
            if (user != null) {
                val profile = SupaBaseHelper.client.postgrest["profiles"]
                    .select { filter { eq("id", user.id) } }
                    .decodeSingleOrNull<UserProfile>()
                nickname = profile?.nickname ?: "点击设置昵称"
                tempNickname = nickname
            }
        } catch (e: Exception) {
            nickname = "获取失败"
            Log.e("Vizia", "获取昵称失败", e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isEditing) {
            OutlinedTextField(
                value = tempNickname,
                onValueChange = { tempNickname = it },
                label = { Text("新昵称") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.padding(top = 8.dp)) {
                Button(onClick = {
                    scope.launch {
                        try {
//                            val userId = SupaBaseHelper.client.auth.currentUserOrNull()?.id ?: ""
//                            val newProfile = UserProfile(id = userId, nickname = tempNickname)
                            val user = SupaBaseHelper.client.auth.currentUserOrNull()

                            // 2. 严谨校验：如果 ID 获取不到，说明登录状态有问题
                            if (user == null || user.id.isBlank()) {
                                Toast.makeText(context, "无法获取用户信息，请重新登录", Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            Log.d("Vizia", "准备更新，用户 ID: ${user.id}")

                            val newProfile = UserProfile(
                                id = user.id, // 这里确保不是 ""
                                nickname = tempNickname
                            )

                            // 使用 upsert: 有则更新，无则插入
                            SupaBaseHelper.client.postgrest["profiles"].upsert(newProfile)

                            nickname = tempNickname
                            isEditing = false
                            Toast.makeText(context, "更新成功", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Log.e("Vizia", "更新昵称失败", e)
                            Toast.makeText(context, "更新失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Text("保存")
                }
                TextButton(onClick = { isEditing = false }) {
                    Text("取消")
                }
            }
        } else {
            Text(text = "当前昵称：$nickname", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = { isEditing = true }) {
                Text("修改昵称")
            }
        }

        Spacer(modifier = Modifier.height(50.dp))
        Button(onClick = {
            scope.launch {
                try {
                    SupaBaseHelper.client.auth.signOut()
                    val prefs = context.getSharedPreferences("vizia_auth", Context.MODE_PRIVATE)
                    prefs.edit { clear() }
                    onLogout()
                } catch (e: Exception) {
                    Log.e("Vizia", "退出登录过程中发生异常", e)
                    Toast.makeText(context, "发生错误，请重试", Toast.LENGTH_SHORT).show()
                }
            }
        }) {
            Text("退出登录")
        }
    }
}