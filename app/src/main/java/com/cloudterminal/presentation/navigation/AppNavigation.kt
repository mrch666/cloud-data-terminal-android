package com.cloudterminal.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cloudterminal.presentation.screens.scanner.ScannerScreen
import com.cloudterminal.presentation.screens.scanner.ScannerViewModel
import com.cloudterminal.presentation.screens.inventory.InventoryScreen
import com.cloudterminal.presentation.screens.inventory.InventoryViewModel
import com.cloudterminal.presentation.screens.products.ProductsScreen
import com.cloudterminal.presentation.screens.products.ProductsViewModel
import com.cloudterminal.presentation.screens.settings.SettingsScreen
import com.cloudterminal.presentation.screens.settings.SettingsViewModel
import com.cloudterminal.presentation.screens.reports.ReportsScreen
import com.cloudterminal.presentation.screens.reports.ReportsViewModel

sealed class Screen(val route: String) {
    object Scanner : Screen("scanner")
    object Inventory : Screen("inventory")
    object Products : Screen("products")
    object Settings : Screen("settings")
    object Reports : Screen("reports")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    var isInitialized by remember { mutableStateOf(false) }
    
    NavHost(
        navController = navController,
        startDestination = Screen.Scanner.route
    ) {
        // Экран сканирования
        composable(Screen.Scanner.route) {
            val viewModel: ScannerViewModel = hiltViewModel()
            ScannerScreen(
                viewModel = viewModel,
                onNavigateToInventory = {
                    navController.navigate(Screen.Inventory.route)
                },
                onNavigateToProducts = {
                    navController.navigate(Screen.Products.route)
                }
            )
        }
        
        // Экран инвентаризации
        composable(Screen.Inventory.route) {
            val viewModel: InventoryViewModel = hiltViewModel()
            InventoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Экран справочника товаров
        composable(Screen.Products.route) {
            val viewModel: ProductsViewModel = hiltViewModel()
            ProductsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Экран настроек
        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Экран отчетов
        composable(Screen.Reports.route) {
            val viewModel: ReportsViewModel = hiltViewModel()
            ReportsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}