package com.viziatech.vizia.ui

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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.viziatech.vizia.SupaBaseHelper
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(onBackToLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    var passwordHasFocused by remember { mutableStateOf(false) }
    var passwordIsFocused by remember { mutableStateOf(false) }

    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]+$".toRegex()
    val isEmailValid = email.isEmpty() || email.matches(emailRegex)

    // 密码正则：包含字母和数字，至少8位
    val passwordValidationRegex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+-=]{8,}$".toRegex()
    val passwordInputFilter = remember { Regex("[A-Za-z0-9!@#$%^&*()_+-=]*") }

    val isPasswordFormatValid = password.matches(passwordValidationRegex)
    val isPasswordMatch = password == confirmPassword && password.isNotEmpty()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // 按钮启用逻辑：邮箱合法 + 密码格式合法 + 两次输入一致
    val canSubmit =
        email.isNotEmpty() && email.matches(emailRegex) && isPasswordFormatValid && isPasswordMatch

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("注册账号", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // --- 邮箱输入框 (逻辑不变) ---
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("请输入邮箱") },
            modifier = Modifier.fillMaxWidth(),
            isError = !isEmailValid,
            supportingText = {
                if (!isEmailValid) Text(
                    "请输入正确的邮箱格式",
                    color = MaterialTheme.colorScheme.error
                )
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        // --- 密码输入框 ---
        OutlinedTextField(
            value = password,
            onValueChange = { input ->
                if (input.matches(passwordInputFilter)) password = input
            },
            label = { Text("设置密码") },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    passwordIsFocused = focusState.isFocused
                    if (focusState.isFocused) {
                        passwordHasFocused = true // 用户点击过
                    }
                },
            // 只有当用户点击过、现在没在输入、且格式不对时才变红
            isError = passwordHasFocused && !passwordIsFocused && password.isNotEmpty() && !isPasswordFormatValid,
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
            },
            supportingText = {
                // 同样的逻辑：不在焦点时才显示错误提示，平时显示引导文字
                if (passwordHasFocused && !passwordIsFocused && password.isNotEmpty() && !isPasswordFormatValid) {
                    Text("密码需包含字母和数字，且至少8位", color = MaterialTheme.colorScheme.error)
                } else {
                    Text("必须包含字母和数字，至少8位")
                }
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        // --- 确认密码输入框 ---
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { input ->
                if (input.matches(passwordInputFilter)) confirmPassword = input
            },
            label = { Text("确认密码") },
            modifier = Modifier.fillMaxWidth(),
            isError = confirmPassword.isNotEmpty() && !isPasswordMatch,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                autoCorrectEnabled = false
            ),
            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                    val icon =
                        if (isConfirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    Icon(
                        imageVector = icon,
                        contentDescription = if (isPasswordVisible) "隐藏密码" else "显示密码"
                    )
                }
            },
            supportingText = {
                if (confirmPassword.isNotEmpty() && !isPasswordMatch) {
                    Text("两次输入的密码不一致", color = MaterialTheme.colorScheme.error)
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        // 调用 Supabase 注册
                        SupaBaseHelper.client.auth.signUpWith(Email) {
                            this.email = email
                            this.password = password
                        }
                        Log.d("ViziaAuth", "注册成功: $email")
                        Toast.makeText(context, "注册成功！", Toast.LENGTH_LONG).show()
                        onBackToLogin() // 注册成功返回登录页
                    } catch (e: Exception) {
                        Log.e("ViziaAuth", "注册过程中发生异常", e)
                        Toast.makeText(
                            context,
                            "注册失败: ${e.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = canSubmit
        ) {
            Text("立即注册")
        }

        TextButton(onClick = onBackToLogin) {
            Text("返回登录")
        }
    }
}