package com.example.smart_emap.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smart_emap.BuildConfig
import com.example.smart_emap.SmartEmapAppContainer
import com.example.smart_emap.data.model.UserDto
import com.example.smart_emap.ui.auth.LoginScreen
import com.example.smart_emap.ui.auth.LoginViewModel
import com.example.smart_emap.ui.shell.MainShellScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(appContainer: SmartEmapAppContainer) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    var currentUser by remember { mutableStateOf<UserDto?>(null) }

    LaunchedEffect(Unit) {
        currentUser = appContainer.authRepository.getSavedUser()
        if (currentUser != null) {
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
                    scope.launch {
                        currentUser = appContainer.authRepository.getSavedUser()
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(Routes.DASHBOARD) {
            val user = currentUser
            if (user == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                }
            } else {
                MainShellScreen(
                    appContainer = appContainer,
                    user = user,
                    onLogout = {
                        scope.launch {
                            appContainer.authRepository.logout()
                            currentUser = null
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
