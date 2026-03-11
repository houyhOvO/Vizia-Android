package com.viziatech.vizia.ui

import android.content.Context
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
import com.viziatech.vizia.repository.UserProfileRepository
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var nickname by remember { mutableStateOf("加载中...") }
    var tempNickname by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val profile = UserProfileRepository.fetchProfile()
        nickname = profile?.nickname ?: "点击设置昵称"
        tempNickname = nickname
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
                        val result = UserProfileRepository.upsertNickname(tempNickname)

                        if (result.isSuccess) {
                            nickname = tempNickname
                            isEditing = false
                            Toast.makeText(context, "更新成功", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(
                                context,
                                "更新失败: ${result.exceptionOrNull()?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
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
                val result = UserProfileRepository.signOut()
                if (result.isSuccess) {
                    val prefs = context.getSharedPreferences("vizia_auth", Context.MODE_PRIVATE)
                    prefs.edit { clear() }
                    onLogout()
                } else {
                    Toast.makeText(context, "退出失败", Toast.LENGTH_SHORT).show()
                }
            }
        }) {
            Text("退出登录")
        }
    }
}