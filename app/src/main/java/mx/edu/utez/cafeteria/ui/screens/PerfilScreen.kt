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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.edu.utez.cafeteria.ui.AppUiState
import mx.edu.utez.cafeteria.ui.components.Avatar
import mx.edu.utez.cafeteria.ui.components.EtiquetaSeccion
import mx.edu.utez.cafeteria.ui.components.Tarjeta
import mx.edu.utez.cafeteria.ui.theme.Cafe

@Composable
fun PerfilScreen(
    state: AppUiState,
    onCambiarCafeteria: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val sesion = state.sesion
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Avatar(88.dp)
                Spacer(Modifier.height(12.dp))
                Text(sesion?.nombre.orEmpty(), color = Cafe.Texto, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Matrícula: ${sesion?.matricula.orEmpty()}", color = Cafe.TextoSuave, fontFamily = FontFamily.Monospace)
                Text(sesion?.correo.orEmpty(), color = Cafe.TextoSuave, style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            Tarjeta {
                EtiquetaSeccion("Tus pedidos")
                val activos = state.misPedidos.count { it.estado.activo }
                Text(
                    "${state.misPedidos.size} pedidos en total · $activos en curso",
                    color = Cafe.Texto,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Al recoger, di tu nombre y matrícula en el mostrador. Pagas en caja.",
                    color = Cafe.TextoSuave,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        item {
            Tarjeta {
                EtiquetaSeccion("Tu cafetería")
                Text(state.cafeteria.nombre, color = Cafe.Texto, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${state.cafeteria.ubicacion} · ${state.cafeteria.horario}", color = Cafe.TextoSuave, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Cada cafetería tiene su propio menú. Si cambias, se vacía tu carrito.",
                    color = Cafe.TextoSuave,
                    style = MaterialTheme.typography.bodySmall
                )
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
            OutlinedButton(
                onClick = onCerrarSesion,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Cafe.Borde)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Cafe.Rojo)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión", color = Cafe.Texto, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
