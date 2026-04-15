package ci.nsu.mobile.main.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ci.nsu.mobile.main.AuthViewModel

@Composable
fun LoginScreen(
    vm: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {

    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(Modifier.padding(16.dp)) {

        TextField(login, { login = it }, label = { Text("Login") })
        TextField(password, { password = it }, label = { Text("Password") })

        Button(onClick = {
            vm.login(login, password, onLoginSuccess)
        }) {
            Text("Войти")
        }

        TextButton(onClick = onRegisterClick) {
            Text("Регистрация")
        }

        if (vm.isLoading) CircularProgressIndicator()

        vm.error?.let { Text(it) }
    }
}