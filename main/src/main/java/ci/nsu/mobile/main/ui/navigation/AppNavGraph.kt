package ci.nsu.mobile.main.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import ci.nsu.mobile.main.AuthViewModel
import ci.nsu.mobile.main.TokenManager
import ci.nsu.mobile.main.ui.screen.*

@Composable
fun AppNavGraph(vm: AuthViewModel) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (TokenManager.token != null) "main" else "login"
    ) {

        composable("login") {
            LoginScreen(
                vm = vm,
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                vm = vm,
                onSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable("main") {
            MainScreen(
                vm = vm,
                onLogout = {
                    vm.logout()
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}