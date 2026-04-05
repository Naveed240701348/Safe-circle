package com.safecircle.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.safecircle.ui.screen.*
import com.safecircle.ui.viewmodel.*

/**
 * Navigation setup for SafeCircle app
 */
@Composable
fun SafeCircleNavigation(
    authViewModel: AuthViewModel,
    sosViewModel: SOSViewModel = viewModel(),
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Check initial login state
    val startDestination = if (authViewModel.isUserLoggedIn()) {
        sosViewModel.setContext(context)
        sosViewModel.startListeningForAlerts()
        "dashboard"
    } else {
        "login"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Login Screen
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    sosViewModel.setContext(context)
                    sosViewModel.startListeningForAlerts()
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        // Register Screen
        composable("register") {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    sosViewModel.setContext(context)
                    sosViewModel.startListeningForAlerts()
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Dashboard Screen
        composable("dashboard") {
            val dashboardViewModel: DashboardViewModel = viewModel()
            // Refresh data whenever we navigate to dashboard
            LaunchedEffect(Unit) {
                dashboardViewModel.loadDashboardData()
            }
            
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToSOS = {
                    navController.navigate("sos")
                },
                onNavigateToFriends = {
                    navController.navigate("friends")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }

        // SOS Screen
        composable("sos") {
            SOSScreen(
                sosViewModel = sosViewModel,
                onNavigateToFriends = {
                    navController.navigate("friends")
                },
                onNavigateBack = {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("sos") { inclusive = true }
                    }
                }
            )
        }

        // Friends List Screen
        composable("friends") {
            val friendViewModel: FriendViewModel = viewModel()
            
            FriendListScreen(
                friendViewModel = friendViewModel,
                onNavigateToAddFriend = {
                    navController.navigate("add_friend")
                },
                onNavigateToSOS = {
                    navController.popBackStack()
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("friends") { inclusive = true }
                    }
                }
            )
        }

        // Add Friend Screen
        composable("add_friend") {
            val friendViewModel: FriendViewModel = viewModel()
            
            AddFriendScreen(
                friendViewModel = friendViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Profile Screen
        composable("profile") {
            ProfileScreen(
                authViewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
