package com.virt92.consolecollector.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.virt92.consolecollector.di.AppContainer
import com.virt92.consolecollector.ui.navigation.AppNavGraph
import com.virt92.consolecollector.ui.navigation.NavRoutes

@Composable
fun AppRoot(
    container: AppContainer,
    initialLoggedIn: Boolean,
) {
    val navController: NavHostController = rememberNavController()
    val tokenState by container.authStore.tokenFlow.collectAsState(initial = null)

    LaunchedEffect(tokenState) {
        if (tokenState == null) {
            navController.navigate(NavRoutes.Auth) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    AppNavGraph(
        navController = navController,
        container = container,
        startDestination = if (initialLoggedIn) NavRoutes.Main else NavRoutes.Auth,
    )
}
