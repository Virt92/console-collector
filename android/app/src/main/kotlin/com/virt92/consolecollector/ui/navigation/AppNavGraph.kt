package com.virt92.consolecollector.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.virt92.consolecollector.di.AppContainer
import com.virt92.consolecollector.ui.auth.LoginScreen
import com.virt92.consolecollector.ui.auth.RegisterScreen
import com.virt92.consolecollector.ui.collection.ItemDetailScreen
import com.virt92.consolecollector.ui.main.MainScaffold
import com.virt92.consolecollector.ui.scan.ScanCameraScreen
import com.virt92.consolecollector.ui.scan.ScanConfirmScreen
import com.virt92.consolecollector.ui.scan.ScanViewModel
import com.virt92.consolecollector.ui.viewmodel.viewModelFactory

@Composable
fun AppNavGraph(
    navController: NavHostController,
    container: AppContainer,
    startDestination: String,
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(NavRoutes.Auth) {
            LoginScreen(
                container = container,
                onLoggedIn = {
                    navController.navigate(NavRoutes.Main) {
                        popUpTo(NavRoutes.Auth) { inclusive = true }
                    }
                },
                onGoToRegister = { navController.navigate(NavRoutes.Register) },
            )
        }
        composable(NavRoutes.Register) {
            RegisterScreen(
                container = container,
                onRegistered = {
                    navController.navigate(NavRoutes.Main) {
                        popUpTo(NavRoutes.Auth) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(NavRoutes.Main) {
            val scanVm: ScanViewModel = viewModel(factory = viewModelFactory { ScanViewModel(container) })
            MainScaffold(
                container = container,
                scanViewModel = scanVm,
                onOpenScan = { navController.navigate(NavRoutes.ScanCamera) },
                onOpenItem = { id -> navController.navigate(NavRoutes.itemDetail(id)) },
            )
        }
        composable(NavRoutes.ScanCamera) { entry ->
            val parent = navController.getBackStackEntry(NavRoutes.Main)
            val scanVm: ScanViewModel = viewModel(parent, factory = viewModelFactory { ScanViewModel(container) })
            ScanCameraScreen(
                viewModel = scanVm,
                onPhotosReady = { navController.navigate(NavRoutes.ScanConfirm) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(NavRoutes.ScanConfirm) {
            val parent = navController.getBackStackEntry(NavRoutes.Main)
            val scanVm: ScanViewModel = viewModel(parent, factory = viewModelFactory { ScanViewModel(container) })
            ScanConfirmScreen(
                viewModel = scanVm,
                onConfirmed = {
                    navController.popBackStack(NavRoutes.Main, inclusive = false)
                },
                onCancel = { navController.popBackStack() },
            )
        }
        composable(NavRoutes.ItemDetail) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            ItemDetailScreen(
                container = container,
                itemId = id,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
