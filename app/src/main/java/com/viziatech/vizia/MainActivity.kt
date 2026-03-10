package com.viziatech.vizia

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.viziatech.vizia.ui.HomeScreen
import com.viziatech.vizia.ui.LoginScreen
import com.viziatech.vizia.ui.RegisterScreen
import com.viziatech.vizia.ui.theme.ViziaTheme
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserSession
import kotlin.time.ExperimentalTime

class MainActivity : ComponentActivity() {
    @OptIn(SupabaseInternal::class, ExperimentalTime::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SupabaseHelper.init()
        enableEdgeToEdge()
        setContent {
            ViziaTheme {
                // 1. 定义三种状态：loading (加载中), login (登录页), home (主页), register (注册页)
                var currentScreen by remember { mutableStateOf("loading") }

                // 2. 核心逻辑：App 启动时异步检查 Session
                LaunchedEffect(Unit) {
                    val prefs =
                        applicationContext.getSharedPreferences("vizia_auth", Context.MODE_PRIVATE)
                    val savedToken = prefs.getString("access_token", null)
                    val savedRefresh = prefs.getString("refresh_token", null)

                    if (savedToken != null && savedRefresh != null) {
                        try {
                            // 核心：手动恢复会话，不需要网络请求，直接把 Token 塞进内存
                            SupabaseHelper.client.auth.importSession(
                                UserSession(
                                    accessToken = savedToken,
                                    refreshToken = savedRefresh,
                                    expiresIn = 3600L, // 假设一小时，SDK 会自动刷新
                                    tokenType = "bearer",
                                    user = null
                                )
                            )
                        } catch (e: Exception) {
                            Log.e("Vizia", "Token 恢复失败", e)
                        }
                    }
                    val session = SupabaseHelper.client.auth.currentSessionOrNull()

                    Log.d("Vizia", "启动检查 - Session 是否存在: ${session != null}")

                    currentScreen = if (session != null) "home" else "login"
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            "loading" -> {
                                // 加载中界面，防止直接闪现登录页
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }

                            "login" -> {
                                LoginScreen(
                                    onNavigateToRegister = { currentScreen = "register" },
                                    onLoginSuccess = { currentScreen = "home" }
                                )
                            }

                            "register" -> {
                                RegisterScreen(
                                    onBackToLogin = { currentScreen = "login" }
                                )
                            }

                            "home" -> {
                                HomeScreen(
                                    onLogout = { currentScreen = "login" }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
