package ci.nsu.mobile.main.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ci.nsu.mobile.main.AuthViewModel
import ci.nsu.mobile.main.data.model.request.*

@Composable
fun RegisterScreen(
    vm: AuthViewModel,
    onSuccess: () -> Unit
) {

    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        vm.loadGroups()
    }

    Column(Modifier.padding(16.dp)) {

        TextField(login, { login = it }, label = { Text("Login") })
        TextField(password, { password = it }, label = { Text("Password") })

        Button(onClick = {

            val request = RegisterRequest(
                login = login,
                password = password,
                email = "",
                phoneNumber = "",
                roleId = 1,
                authAllowed = true,
                person = PersonDto(
                    firstName = "Test",
                    lastName = "Test",
                    middleName = "",
                    birthDate = "2000-01-01",
                    gender = "M",
                    groupId = vm.groups.firstOrNull()?.groupId ?: 1
                )
            )

            vm.register(request, onSuccess)
        }) {
            Text("Зарегистрироваться")
        }

        if (vm.isLoading) CircularProgressIndicator()
        vm.error?.let { Text(text = it, color = androidx.compose.ui.graphics.Color.Red) }
    }
}