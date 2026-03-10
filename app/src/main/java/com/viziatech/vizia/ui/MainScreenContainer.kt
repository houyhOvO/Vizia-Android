package com.viziatech.vizia.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun MainScreenContainer(onLogout: () -> Unit) {
    // 内部页面的路由状态
    var selectedTab by remember { mutableStateOf("home") }

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Post,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = selectedTab == item.route,
                        onClick = { selectedTab = item.route }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                "home" -> HomeScreen()
                "post" -> PostScreen()
                "profile" -> ProfileScreen(onLogout = onLogout)
            }
        }
    }
}