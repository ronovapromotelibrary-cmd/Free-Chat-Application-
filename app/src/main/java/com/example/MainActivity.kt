package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.repository.ChatRepository
import com.example.data.repository.UserRepository
import com.example.ui.navigation.Screen
import com.example.ui.screens.auth.AuthViewModel
import com.example.ui.screens.auth.EmailVerificationScreen
import com.example.ui.screens.auth.ForgotPasswordScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.auth.SplashScreen
import com.example.ui.screens.chat.ChatScreen
import com.example.ui.screens.chat.ChatViewModel
import com.example.ui.screens.main.MainContainerScreen
import com.example.ui.screens.main.MainViewModel
import com.example.ui.screens.settings.AboutScreen
import com.example.ui.screens.settings.BlockedUsersScreen
import com.example.ui.screens.settings.ChangeEmailScreen
import com.example.ui.screens.settings.ChangePasswordScreen
import com.example.ui.screens.settings.HelpSupportScreen
import com.example.ui.screens.settings.NotificationSettingsScreen
import com.example.ui.screens.settings.PrivacyPolicyScreen
import com.example.ui.screens.settings.PrivacySettingsScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.screens.user.UserProfileDetailScreen
import com.example.ui.theme.FreeChatTheme
import com.example.ui.theme.SoftBackground

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FreeChatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SoftBackground
                ) {
                    FreeChatApp()
                }
            }
        }
    }
}

@Composable
fun FreeChatApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Splash
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToVerify = {
                    navController.navigate(Screen.EmailVerification.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Login
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onNavigateToVerify = { navController.navigate(Screen.EmailVerification.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Register
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToVerify = {
                    navController.navigate(Screen.EmailVerification.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        // Email Verification
        composable(Screen.EmailVerification.route) {
            EmailVerificationScreen(
                viewModel = authViewModel,
                onVerifiedSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.EmailVerification.route) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.EmailVerification.route) { inclusive = true }
                    }
                }
            )
        }

        // Forgot Password
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onBackToLogin = { navController.popBackStack() }
            )
        }

        // Main Container (Chats, Friends, Profile)
        composable(Screen.Main.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val mainViewModel = remember { MainViewModel(context) }

            MainContainerScreen(
                viewModel = mainViewModel,
                onOpenChat = { otherUid, convId ->
                    navController.navigate(Screen.Chat.createRoute(otherUid, convId))
                },
                onOpenUserProfile = { targetUid ->
                    navController.navigate(Screen.UserProfileDetail.createRoute(targetUid))
                },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToNotificationSettings = { navController.navigate(Screen.NotificationSettings.route) },
                onNavigateToPrivacySettings = { navController.navigate(Screen.PrivacySettings.route) },
                onLoggedOut = {
                    mainViewModel.signOut {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Chat Detail
        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("otherUid") { type = NavType.StringType },
                navArgument("convId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val otherUid = backStackEntry.arguments?.getString("otherUid") ?: ""
            val context = androidx.compose.ui.platform.LocalContext.current
            val chatViewModel = remember(otherUid) {
                ChatViewModel(context = context, otherUid = otherUid)
            }

            ChatScreen(
                viewModel = chatViewModel,
                onBackClick = { navController.popBackStack() },
                onOpenUserProfile = { targetUid ->
                    navController.navigate(Screen.UserProfileDetail.createRoute(targetUid))
                }
            )
        }

        // User Profile Detail
        composable(
            route = Screen.UserProfileDetail.route,
            arguments = listOf(navArgument("targetUid") { type = NavType.StringType })
        ) { backStackEntry ->
            val targetUid = backStackEntry.arguments?.getString("targetUid") ?: ""
            val context = androidx.compose.ui.platform.LocalContext.current
            val userRepository = remember { UserRepository() }
            val chatRepository = remember { ChatRepository(context) }
            val mainAuth = remember { com.example.data.repository.AuthRepository() }

            UserProfileDetailScreen(
                targetUid = targetUid,
                currentUid = mainAuth.currentUid ?: "",
                userRepository = userRepository,
                chatRepository = chatRepository,
                onBackClick = { navController.popBackStack() },
                onOpenChat = { uid ->
                    navController.navigate(Screen.Chat.createRoute(uid))
                }
            )
        }

        // Settings Root
        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel()
            val context = androidx.compose.ui.platform.LocalContext.current
            val mainViewModel = remember { MainViewModel(context) }

            SettingsScreen(
                viewModel = settingsViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToNotifications = { navController.navigate(Screen.NotificationSettings.route) },
                onNavigateToPrivacy = { navController.navigate(Screen.PrivacySettings.route) },
                onNavigateToBlockedUsers = { navController.navigate(Screen.BlockedUsers.route) },
                onNavigateToChangePassword = { navController.navigate(Screen.ChangePassword.route) },
                onNavigateToChangeEmail = { navController.navigate(Screen.ChangeEmail.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) },
                onNavigateToPrivacyPolicy = { navController.navigate(Screen.PrivacyPolicy.route) },
                onNavigateToHelpSupport = { navController.navigate(Screen.HelpSupport.route) },
                onLoggedOut = {
                    mainViewModel.signOut {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Notification Settings
        composable(Screen.NotificationSettings.route) {
            val settingsViewModel: SettingsViewModel = viewModel()
            NotificationSettingsScreen(
                viewModel = settingsViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Privacy Settings
        composable(Screen.PrivacySettings.route) {
            val settingsViewModel: SettingsViewModel = viewModel()
            PrivacySettingsScreen(
                viewModel = settingsViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Blocked Users
        composable(Screen.BlockedUsers.route) {
            val settingsViewModel: SettingsViewModel = viewModel()
            BlockedUsersScreen(
                viewModel = settingsViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Change Password
        composable(Screen.ChangePassword.route) {
            val settingsViewModel: SettingsViewModel = viewModel()
            ChangePasswordScreen(
                viewModel = settingsViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Change Email
        composable(Screen.ChangeEmail.route) {
            val settingsViewModel: SettingsViewModel = viewModel()
            ChangeEmailScreen(
                viewModel = settingsViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // About
        composable(Screen.About.route) {
            AboutScreen(onBackClick = { navController.popBackStack() })
        }

        // Privacy Policy
        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen(onBackClick = { navController.popBackStack() })
        }

        // Help & Support
        composable(Screen.HelpSupport.route) {
            HelpSupportScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
