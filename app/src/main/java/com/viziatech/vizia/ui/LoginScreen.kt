package com.viziatech.vizia.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch
import androidx.core.content.edit
import com.viziatech.vizia.SupabaseHelper

@Composable
fun LoginScreen(onNavigateToRegister: () -> Unit, onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }

    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]+$".toRegex()
    val isEmailValid = email.isEmpty() || email.matches(emailRegex)

    val passwordRegex = remember { Regex("[A-Za-z0-9!@#$%^&*()_+-=]*") }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("欢迎来到Vizia", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("邮箱") },
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                if (!isEmailValid) {
                    Text(
                        "请输入正确的邮箱格式（如: example@vizia.com）",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { input ->
                if (input.matches(passwordRegex)) {
                    password = input
                }
            },
            label = { Text("密码") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                autoCorrectEnabled = false
            ),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    val icon =
                        if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    Icon(
                        imageVector = icon,
                        contentDescription = if (isPasswordVisible) "隐藏密码" else "显示密码"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            scope.launch {
                try {
                    SupabaseHelper.client.auth.signInWith(Email) {
                        this.email = email
                        this.password = password
                    }
                    val session = SupabaseHelper.client.auth.currentSessionOrNull()
                    if (session != null) {
                        val prefs = context.getSharedPreferences("vizia_auth", Context.MODE_PRIVATE)
                        prefs.edit { putString("access_token", session.accessToken) }
                        prefs.edit { putString("refresh_token", session.refreshToken) }
                    }
                    Log.d("Vizia", "登录成功，当前 Session: ${session?.accessToken?.take(10)}...")
                    onLoginSuccess() // 登录成功回调
                } catch (e: Exception) {
                    Log.e("ViziaAuth", "登录失败", e)
                    Toast.makeText(context, "登录失败: ${e.localizedMessage}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("登录")
        }

        TextButton(onClick = onNavigateToRegister) {
            Text("还没有账号？点击注册")
        }
    }
}