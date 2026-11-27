package mx.edu.utez.cafeteria.ui.personal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.edu.utez.cafeteria.data.pesosDec
import mx.edu.utez.cafeteria.ui.AppUiState
import mx.edu.utez.cafeteria.data.EstadoPedido
import mx.edu.utez.cafeteria.data.Pedido
import mx.edu.utez.cafeteria.ui.components.CajaIcono
import mx.edu.utez.cafeteria.ui.components.CampoBusqueda
import mx.edu.utez.cafeteria.ui.components.Chip
import mx.edu.utez.cafeteria.ui.components.EstadoVacio
import mx.edu.utez.cafeteria.ui.components.EtiquetaSeccion
import mx.edu.utez.cafeteria.ui.components.Tarjeta
import mx.edu.utez.cafeteria.ui.theme.Cafe
import mx.edu.utez.cafeteria.ui.theme.CafeteriaTheme

@Composable
fun EntregadosScreen(state: AppUiState) {
    var busqueda by rememberSaveable { mutableStateOf("") }
    val despachados = state.despachadosHoy
    val entregados = despachados.filter { it.estado == EstadoPedido.ENTREGADO }
    val cancelados = despachados.filter { it.estado == EstadoPedido.CANCELADO }
    val texto = busqueda.trim().removePrefix("#")
    val filtrados = if (texto.isEmpty()) despachados else despachados.filter {
        it.numero.toString().contains(texto) || it.cliente.matricula.contains(texto, ignoreCase = true) ||
            it.cliente.nombre.contains(texto, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Tarjeta {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CajaIcono(Icons.Default.Verified)
                    Column(modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)) {
                        EtiquetaSeccion("Turno matutino")
                        Text(state.cafeteria.nombre, color = Cafe.Texto, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Chip("7:30 - 14:00", Cafe.TarjetaClara, Cafe.Texto, icono = Icons.Default.Schedule)
                }
                Surface(color = Cafe.Fondo, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Venta total del turno", color = Cafe.TextoSuave, style = MaterialTheme.typography.bodyMedium)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(pesosDec(entregados.sumOf { it.total }), color = Cafe.AmbarTexto, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                            Text(" MXN", modifier = Modifier.padding(bottom = 6.dp), color = Cafe.TextoSuave, style = MaterialTheme.typography.labelMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = Cafe.AzulClaro, modifier = Modifier.size(14.dp))
                            Text("  Cobrado en caja al entregar", color = Cafe.AzulClaro, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Dato(Icons.Default.DoneAll, entregados.size, "Entregados", Modifier.weight(1f))
                    Dato(Icons.Default.CheckCircle, cancelados.size, "Cancelados", Modifier.weight(1f))
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(Cafe.Ambar, CircleShape))
                Text("  Turno en curso", color = Cafe.Texto, style = MaterialTheme.typography.labelLarge)
            }
        }
        item { CampoBusqueda(busqueda, { busqueda = it }, "Buscar #comanda (ej. 101) o matrícula…") }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Comandas despachadas  ", color = Cafe.Texto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Chip("${filtrados.size} recientes")
            }
        }
        if (filtrados.isEmpty()) {
            item { EstadoVacio(Icons.Default.Info, "Nada por aquí", "No hay comandas que coincidan con tu búsqueda.") }
        }
        items(filtrados, key = { it.numero }) { pedido ->
            if (pedido.estado == EstadoPedido.CANCELADO) TarjetaCancelada(pedido) else TarjetaEntregada(pedido)
        }
    }
}

@Composable
private fun Dato(icono: ImageVector, valor: Int, texto: String, modifier: Modifier) {
    Surface(color = Cafe.TarjetaClara, shape = RoundedCornerShape(14.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icono, contentDescription = null, tint = Cafe.AmbarTexto, modifier = Modifier.size(20.dp))
            Text("$valor", color = Cafe.Texto, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(texto, color = Cafe.TextoSuave, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun TarjetaEntregada(pedido: Pedido) {
    Tarjeta {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NumeroComanda(pedido.numero)
            Column(modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)) {
                Text(pedido.cliente.nombre, color = Cafe.Texto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(pedido.cliente.matricula, color = Cafe.TextoSuave, style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Monospace)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Cafe.TextoSuave, modifier = Modifier.size(14.dp))
                    Text(" ${pedido.hora} • ", color = Cafe.TextoSuave, style = MaterialTheme.typography.labelMedium)
                    Text("Prep: ${pedido.minutosPrep ?: "-"} min", color = Cafe.AmbarTexto, style = MaterialTheme.typography.labelMedium)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(pesosDec(pedido.total), color = Cafe.Texto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Pagado en caja", color = Cafe.TextoSuave, style = MaterialTheme.typography.labelSmall)
            }
        }
        FilasProductos(pedido)
        Chip("Entregado en ${pedido.mostrador}", icono = Icons.Default.CheckCircle)
    }
}

@Composable
private fun TarjetaCancelada(pedido: Pedido) {
    Tarjeta {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NumeroComanda(pedido.numero, Cafe.RojoFondo, Cafe.Rojo)
            Column(modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)) {
                Text(
                    "Comanda cancelada",
                    color = Cafe.TextoSuave,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.LineThrough
                )
                Text(pedido.cliente.matricula, color = Cafe.TextoSuave, style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Monospace)
                Text("${pedido.hora} • No se cobró", color = Cafe.Rojo, style = MaterialTheme.typography.labelMedium)
            }
            Text(pesosDec(pedido.total), color = Cafe.Rojo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.LineThrough)
        }
        Surface(color = Cafe.RojoFondo, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Cafe.Rojo, modifier = Modifier.size(16.dp))
                    Text("  Motivo: ${pedido.motivo ?: "Sin especificar"}", color = Cafe.Rojo, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
                Text(
                    "Como el pago es al recoger, al alumno no se le cobró nada. Se le avisó en su app.",
                    modifier = Modifier.padding(top = 4.dp),
                    color = Cafe.Texto,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Chip("Cancelado", Cafe.RojoFondo, Cafe.Rojo, icono = Icons.Default.Replay)
    }
}

@Preview(heightDp = 1600, backgroundColor = 0xFF0B1324, showBackground = true)
@Composable
private fun EntregadosPreview() {
    CafeteriaTheme { EntregadosScreen(AppUiState(cafeteriaId = "nido")) }
}
