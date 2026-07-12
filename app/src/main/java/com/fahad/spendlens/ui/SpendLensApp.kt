package com.fahad.spendlens.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fahad.spendlens.ui.screens.HistoryScreen
import com.fahad.spendlens.ui.screens.HomeScreen
import com.fahad.spendlens.ui.screens.InsightsScreen
import com.fahad.spendlens.ui.screens.ScanScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Filled.Home)
    data object Scan : Screen("scan", "Scan", Icons.Filled.AddCircle)
    data object History : Screen("history", "History", Icons.Filled.List)
    data object Insights : Screen("insights", "Insights", Icons.Filled.Info)
}

val bottomNavItems = listOf(Screen.Home, Screen.Scan, Screen.History, Screen.Insights)

@Composable
fun SpendLensApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Scan.route) { ScanScreen() }
            composable(Screen.History.route) { HistoryScreen() }
            composable(Screen.Insights.route) { InsightsScreen() }
        }
    }
}