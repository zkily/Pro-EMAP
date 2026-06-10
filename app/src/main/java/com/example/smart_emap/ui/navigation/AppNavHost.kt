package com.example.smart_emap.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smart_emap.SmartEmapAppContainer
import com.example.smart_emap.ui.auth.LoginScreen
import com.example.smart_emap.ui.auth.LoginViewModel
import com.example.smart_emap.ui.shell.MainShellScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(appContainer: SmartEmapAppContainer) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val appNavViewModel: AppNavViewModel = viewModel(
        factory = AppNavViewModel.Factory(appContainer.authRepository),
    )
    val currentUser by appNavViewModel.currentUser.collectAsState()
    val sessionReady by appNavViewModel.sessionReady.collectAsState()

    LaunchedEffect(sessionReady, currentUser) {
        if (!sessionReady) return@LaunchedEffect
        if (currentUser != null && navController.currentDestination?.route != Routes.DASHBOARD) {
            navController.navigate(Routes.DASHBOARD) {
                popUpTo(Routes.LOGIN) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
    ) {
        composable(Routes.LOGIN) {
            val loginViewModel: LoginViewModel = viewModel(
                factory = LoginViewModel.Factory(appContainer.authRepository),
            )
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    appNavViewModel.onLoginSuccess()
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.DASHBOARD) {
            val user = currentUser
            when {
                !sessionReady -> Unit
                user == null -> {
                    LaunchedEffect(Unit) {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.DASHBOARD) { inclusive = true }
                        }
                    }
                }
                else -> {
                    MainShellScreen(
                        appContainer = appContainer,
                        user = user,
                        onLogout = {
                            scope.launch {
                                appContainer.authRepository.logout()
                                appNavViewModel.onLogout()
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(Routes.DASHBOARD) { inclusive = true }
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}
