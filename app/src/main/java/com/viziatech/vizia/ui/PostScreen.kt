package com.viziatech.vizia.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.viziatech.vizia.BuildConfig
import com.viziatech.vizia.service.BigModelService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen() {
    val scope = rememberCoroutineScope()
    var requestText by remember { mutableStateOf("") }

    var imageUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 初始化服务 (建议从 BuildConfig 获取 API Key)
    val bigModelService = remember { BigModelService(BuildConfig.BIG_MODEL_API_KEY) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("AI 图片生成") })
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.imePadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = requestText,
                        onValueChange = { requestText = it },
                        placeholder = { Text("描述你想要的图片...") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        enabled = !isLoading // 加载时禁用输入
                    )

                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    } else {
                        FilledIconButton(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    imageUrl = null

                                    try {
                                        val result = bigModelService.generateImage(requestText)
                                        if (result != null) {
                                            imageUrl = result
                                        } else {
                                            errorMessage = "服务器返回空结果"
                                        }
                                    } catch (e: io.ktor.client.plugins.HttpRequestTimeoutException) {
                                        errorMessage = "生成超时了，AI 还在努力构思，请稍后再试"
                                    } catch (e: Exception) {
                                        errorMessage = "网络错误: ${e.localizedMessage}"
                                        Log.e("Vizia", "API Error", e)
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            enabled = requestText.isNotBlank()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "生成")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (imageUrl != null) {
                Text("生成成功！图片 URL：", style = MaterialTheme.typography.titleSmall)
                SelectionContainer { // 允许用户长按复制 URL
                    Text(
                        text = imageUrl!!,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                // TODO: 显示图片
            } else if (errorMessage != null) {
                Text(errorMessage!!, color = Color.Red)
            } else if (!isLoading) {
                Text("在下方输入描述词并点击发送", color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}