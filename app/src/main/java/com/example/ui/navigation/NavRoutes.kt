package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object EmailVerification : Screen("email_verification")
    object ForgotPassword : Screen("forgot_password")
    object Main : Screen("main")
    object Chat : Screen("chat/{otherUid}?convId={convId}") {
        fun createRoute(otherUid: String, convId: String = ""): String {
            return "chat/$otherUid?convId=$convId"
        }
    }
    object UserProfileDetail : Screen("user_profile/{targetUid}") {
        fun createRoute(targetUid: String): String {
            return "user_profile/$targetUid"
        }
    }
    object Settings : Screen("settings")
    object NotificationSettings : Screen("settings/notifications")
    object PrivacySettings : Screen("settings/privacy")
    object BlockedUsers : Screen("settings/blocked_users")
    object ChangePassword : Screen("settings/change_password")
    object ChangeEmail : Screen("settings/change_email")
    object About : Screen("settings/about")
    object PrivacyPolicy : Screen("settings/privacy_policy")
    object HelpSupport : Screen("settings/help_support")
}
