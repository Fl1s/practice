package ci.nsu.mobile.auth.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ci.nsu.mobile.auth.TokenManager
import ci.nsu.mobile.auth.ui.AuthViewModel
import ci.nsu.mobile.auth.ui.QrViewModel

@Composable
fun QrProfileScreen(
    authVm: AuthViewModel, qrVm: QrViewModel, onLogout: () -> Unit
) {
    val context = LocalContext.current

    if (qrVm.showQrDialog) {
        QrPreviewDialog(
            vm = qrVm, onDismiss = { qrVm.dismissQrDialog() })
    }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Профиль", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "ID: ${TokenManager.userId ?: "—"}", style = MaterialTheme.typography.labelMedium
                )
                Text(
                    "Авторизован", style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                qrVm.generateQr(
                    login = authVm.lastLogin, password = authVm.lastPassword
                )
            }, modifier = Modifier.fillMaxWidth()
        ) {
            Text("Создать QR-код авторизации")
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onLogout, modifier = Modifier.fillMaxWidth()
        ) {
            Text("Выйти")
        }
    }
}

@Composable
private fun QrPreviewDialog(vm: QrViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val bmp = vm.generatedQr ?: return

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(
                Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("QR-код авторизации", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }

                Image(
                    bitmap = bmp.asImageBitmap(), contentDescription = "QR-код", modifier = Modifier.size(240.dp)
                )

                vm.savedMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.primary)
                }

                Button(
                    onClick = { vm.saveQrToGallery(context) }, modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сохранить в галерею")
                }
            }
        }
    }
}