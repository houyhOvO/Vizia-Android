package com.viziatech.vizia.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import com.viziatech.vizia.SupaBaseHelper
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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
