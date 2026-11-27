package mx.edu.utez.cafeteria.ui.personal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.edu.utez.cafeteria.data.Rol
import mx.edu.utez.cafeteria.data.Sesion
import mx.edu.utez.cafeteria.data.TiempoEspera
import mx.edu.utez.cafeteria.ui.AppUiState
import mx.edu.utez.cafeteria.ui.components.CajaIcono
import mx.edu.utez.cafeteria.ui.components.Chip
import mx.edu.utez.cafeteria.ui.components.EtiquetaSeccion
import mx.edu.utez.cafeteria.ui.components.Tarjeta
import mx.edu.utez.cafeteria.ui.theme.Cafe
import mx.edu.utez.cafeteria.ui.theme.CafeteriaTheme

@Composable
fun SedeScreen(
    state: AppUiState,
    onTiempo: (TiempoEspera) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val cafeteria = state.cafeteria
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Tarjeta {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CajaIcono(Icons.Default.AdminPanelSettings)
                    Column(modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)) {
                        EtiquetaSeccion("Gestión operativa en turno")
                        Text("Panel de Sede y Despacho", color = Cafe.Texto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Chip("Activo", Cafe.Ambar, Cafe.SobreAmbar)
                }
                Text(
                    "Sesión: ${state.sesion?.nombre.orEmpty()} (${state.sesion?.correo.orEmpty()})",
                    color = Cafe.TextoSuave,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        item {
            Surface(color = Cafe.Tarjeta, shape = RoundedCornerShape(20.dp)) {
                Column {
                    Image(
                        painter = painterResource(cafeteria.foto),
                        contentDescription = "Foto de ${cafeteria.nombre}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    )
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(cafeteria.nombre, color = Cafe.Texto, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Dato(Icons.Default.LocationOn, cafeteria.ubicacion)
                        Dato(Icons.Default.Schedule, cafeteria.horario)
                        Text(cafeteria.especialidad, color = Cafe.AmbarTexto, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Solo ves y atiendes los pedidos y el menú de esta cafetería.",
                            color = Cafe.TextoSuave,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        item {
            Tarjeta {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CajaIcono(Icons.Default.Timer)
                    Column(modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)) {
                        Text("Tiempo de Espera", color = Cafe.Texto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Lo ven los alumnos en el menú de tu cafetería", color = Cafe.TextoSuave, style = MaterialTheme.typography.bodySmall)
                    }
                    Chip(state.tiempoEspera.nivel)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TiempoEspera.entries.forEach { t ->
                        val elegido = t == state.tiempoEspera
                        Surface(
                            onClick = { onTiempo(t) },
                            color = if (elegido) Cafe.Ambar else Cafe.TarjetaClara,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(t.rango, color = if (elegido) Cafe.SobreAmbar else Cafe.Texto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(t.etiqueta, color = if (elegido) Cafe.SobreAmbar else Cafe.TextoSuave, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                                    if (t == TiempoEspera.ALTO) {
                                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = if (elegido) Cafe.SobreAmbar else Cafe.AmbarTexto, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }
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

@Composable
private fun Dato(icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = Cafe.AzulClaro, modifier = Modifier.size(18.dp))
        Text("  $texto", color = Cafe.Texto, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(heightDp = 1000, backgroundColor = 0xFF0B1324, showBackground = true)
@Composable
private fun SedePreview() {
    CafeteriaTheme {
        SedeScreen(
            AppUiState(sesion = Sesion(2, "Encargado Nido de Halcón", "nido@utez.edu.mx", "nido@utez.edu.mx", Rol.ENCARGADO, "nido")),
            onTiempo = {}, onCerrarSesion = {}
        )
    }
}
