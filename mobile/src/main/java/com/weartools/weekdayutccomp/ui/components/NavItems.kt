package com.weartools.weekdayutccomp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector, val unselectedIcon: ImageVector) {
    data object Home : Screen("Home", "Home", Icons.Default.Home, Icons.Outlined.Home)
    data object About : Screen("About", "About", Icons.Default.Info, Icons.Outlined.Info)
}

