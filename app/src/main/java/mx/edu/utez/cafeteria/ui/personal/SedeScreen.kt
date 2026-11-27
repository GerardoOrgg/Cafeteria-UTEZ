package mx.edu.utez.cafeteria.ui.personal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.edu.utez.cafeteria.data.Cafeteria
import mx.edu.utez.cafeteria.data.Cafeterias
import mx.edu.utez.cafeteria.ui.AppUiState
import mx.edu.utez.cafeteria.data.TiempoEspera
import mx.edu.utez.cafeteria.ui.components.CajaIcono
import mx.edu.utez.cafeteria.ui.components.Chip
import mx.edu.utez.cafeteria.ui.components.EtiquetaSeccion
import mx.edu.utez.cafeteria.ui.components.Tarjeta
import mx.edu.utez.cafeteria.ui.theme.Cafe
import mx.edu.utez.cafeteria.ui.theme.CafeteriaTheme

@Composable
fun SedeScreen(
    state: AppUiState,
    onCambiarSede: (String) -> Unit,
    onTiempo: (TiempoEspera) -> Unit,
    onSalir: () -> Unit
) {
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
                    Chip("Sincronizado", Cafe.TarjetaClara, Cafe.Texto, punto = Cafe.Ambar)
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Apartment, contentDescription = null, tint = Cafe.AmbarTexto)
                Text("  Sedes del Campus UTEZ", modifier = Modifier.weight(1f), color = Cafe.Texto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${Cafeterias.lista.size} Cafeterías", color = Cafe.TextoSuave, style = MaterialTheme.typography.labelMedium)
            }
        }
        items(Cafeterias.lista, key = { it.id }) { c ->
            TarjetaSede(c, activa = c.id == state.cafeteria.id) { onCambiarSede(c.id) }
        }
        item {
            Tarjeta {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CajaIcono(Icons.Default.Timer)
                    Column(modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)) {
                        Text("Tiempo de Espera", color = Cafe.Texto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Visible en vivo para toda la comunidad", color = Cafe.TextoSuave, style = MaterialTheme.typography.bodySmall)
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
                onClick = onSalir,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Cafe.Borde)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Cafe.Texto)
                Spacer(Modifier.width(8.dp))
                Text("Salir del panel (volver a la app de alumno)", color = Cafe.Texto)
            }
        }
    }
}

@Composable
private fun TarjetaSede(cafeteria: Cafeteria, activa: Boolean, onCambiar: () -> Unit) {
    Surface(
        color = if (activa) Cafe.TarjetaClara else Cafe.Tarjeta,
        shape = RoundedCornerShape(18.dp),
        border = if (activa) BorderStroke(1.dp, Cafe.Ambar.copy(alpha = 0.5f)) else null
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                CajaIcono(
                    if (activa) Icons.Default.Restaurant else Icons.Default.LocalCafe,
                    fondo = if (activa) Cafe.Ambar else Cafe.TarjetaClara,
                    tinte = if (activa) Cafe.SobreAmbar else Cafe.Texto,
                    tamano = 48.dp
                )
                Column(modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(cafeteria.nombreCorto, color = Cafe.Texto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        if (activa) Chip("Activo", Cafe.Ambar, Cafe.SobreAmbar)
                    }
                    Text(cafeteria.ubicacion, color = Cafe.TextoSuave, style = MaterialTheme.typography.bodySmall)
                    Text(cafeteria.especialidad, color = if (activa) Cafe.AmbarTexto else Cafe.TextoSuave, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
                if (activa) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Cafe.Ambar, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Sede activa", tint = Cafe.SobreAmbar, modifier = Modifier.size(18.dp))
                    }
                } else {
                    Text("Sede secundaria", color = Cafe.TextoSuave, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            if (!activa) {
                Surface(onClick = onCambiar, color = Cafe.TarjetaClara, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Cafe.AmbarTexto)
                        Text("  Cambiar turno a ${cafeteria.nombre}", color = Cafe.Texto, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Preview(heightDp = 1100, backgroundColor = 0xFF0B1324, showBackground = true)
@Composable
private fun SedePreview() {
    CafeteriaTheme { SedeScreen(AppUiState(cafeteriaId = "nido"), onCambiarSede = {}, onTiempo = {}, onSalir = {}) }
}
