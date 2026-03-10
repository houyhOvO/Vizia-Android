package com.viziatech.vizia.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem (val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "主页", Icons.Default.Home)
    object Post : BottomNavItem("post", "发布", Icons.Default.AddCircle)
    object Profile : BottomNavItem("profile", "我的", Icons.Default.Person)
}