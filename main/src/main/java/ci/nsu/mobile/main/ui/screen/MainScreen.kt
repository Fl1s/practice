package ci.nsu.mobile.main.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ci.nsu.mobile.main.AuthViewModel

@Composable
fun MainScreen(
    vm: AuthViewModel,
    onLogout: () -> Unit
) {

    LaunchedEffect(Unit) {
        vm.loadUsers()
    }

    Column(Modifier.padding(16.dp)) {

        Button(onClick = onLogout) {
            Text("Выйти")
        }

        if (vm.isLoading) {
            CircularProgressIndicator()
        }

        LazyColumn {
            items(vm.users) {
                Text("${it.id} ${it.login}")
            }
        }
    }
}