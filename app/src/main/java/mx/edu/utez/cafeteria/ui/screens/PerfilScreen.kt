package mx.edu.utez.cafeteria.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import mx.edu.utez.cafeteria.config.Configuracion
import mx.edu.utez.cafeteria.ui.AppUiState
import mx.edu.utez.cafeteria.ui.components.Avatar
import mx.edu.utez.cafeteria.ui.components.BotonAmbar
import mx.edu.utez.cafeteria.ui.components.EtiquetaSeccion
import mx.edu.utez.cafeteria.ui.components.Tarjeta
import mx.edu.utez.cafeteria.ui.components.coloresCampo
import mx.edu.utez.cafeteria.ui.theme.Cafe

@Composable
fun PerfilScreen(
    state: AppUiState,
    onGuardarUsuario: (String, String) -> Unit,
    onCambiarCafeteria: () -> Unit,
    onAbrirPanel: () -> Unit
) {
    var nombre by rememberSaveable(state.usuario) { mutableStateOf(state.usuario.nombre) }
    var matricula by rememberSaveable(state.usuario) { mutableStateOf(state.usuario.matricula) }
    var pidiendoPin by remember { mutableStateOf(false) }
    val hayCambios = nombre.trim() != state.usuario.nombre || matricula.trim().uppercase() != state.usuario.matricula
    val datosValidos = nombre.isNotBlank() && matricula.isNotBlank()

    if (pidiendoPin) {
        DialogoPin(
            onCorrecto = {
                pidiendoPin = false
                onAbrirPanel()
            },
            onCancelar = { pidiendoPin = false }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Avatar(88.dp)
                Spacer(Modifier.height(12.dp))
                Text(state.usuario.nombre, color = Cafe.Texto, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Matrícula: ${state.usuario.matricula}", color = Cafe.TextoSuave, fontFamily = FontFamily.Monospace)
            }
        }
        item {
            Tarjeta {
                EtiquetaSeccion("Tus datos para recoger")
                Text("Con estos datos la cafetería sabe que el pedido es tuyo.", color = Cafe.TextoSuave, style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    shape = RoundedCornerShape(14.dp),
                    colors = coloresCampo()
                )
                OutlinedTextField(
                    value = matricula,
                    onValueChange = { matricula = it.uppercase().take(12) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Matrícula") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    shape = RoundedCornerShape(14.dp),
                    colors = coloresCampo()
                )
                BotonAmbar(
                    "Guardar datos",
                    onClick = { onGuardarUsuario(nombre, matricula) },
                    icono = Icons.Default.Save,
                    enabled = hayCambios && datosValidos
                )
            }
        }
        item {
            Tarjeta {
                EtiquetaSeccion("Tu cafetería")
                Text(state.cafeteria.nombre, color = Cafe.Texto, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${state.cafeteria.ubicacion} · ${state.cafeteria.horario}", color = Cafe.TextoSuave, style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(
                    onClick = onCambiarCafeteria,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Cafe.Borde)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Cafe.AmbarTexto)
                    Spacer(Modifier.width(8.dp))
                    Text("Cambiar de cafetería", color = Cafe.Texto, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        item {
            Tarjeta {
                EtiquetaSeccion("¿Trabajas en la cafetería?")
                Text(
                    "Entra al panel para recibir comandas, marcar pedidos listos y agregar, editar o borrar platillos.",
                    color = Cafe.Texto,
                    style = MaterialTheme.typography.bodyMedium
                )
                BotonAmbar("Abrir panel de cafetería", { pidiendoPin = true }, icono = Icons.Default.AdminPanelSettings)
            }
        }
    }
}

@Composable
private fun DialogoPin(onCorrecto: () -> Unit, onCancelar: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = Cafe.Tarjeta,
        icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Cafe.AmbarTexto) },
        title = { Text("PIN del personal", color = Cafe.Texto, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = pin,
                onValueChange = { nuevo ->
                    pin = nuevo.filter { it.isDigit() }.take(6)
                    error = false
                },
                label = { Text("PIN") },
                singleLine = true,
                isError = error,
                supportingText = { if (error) Text("PIN incorrecto") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                shape = RoundedCornerShape(14.dp),
                colors = coloresCampo()
            )
        },
        confirmButton = {
            TextButton(onClick = { if (pin == Configuracion.PIN_PERSONAL) onCorrecto() else error = true }) {
                Text("Entrar", color = Cafe.AmbarTexto, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar", color = Cafe.Texto) }
        }
    )
}
