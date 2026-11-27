package mx.edu.utez.cafeteria.ui.personal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.edu.utez.cafeteria.data.pesosDec
import mx.edu.utez.cafeteria.ui.AppUiState
import mx.edu.utez.cafeteria.data.EstadoPedido
import mx.edu.utez.cafeteria.data.Pedido
import mx.edu.utez.cafeteria.ui.components.BotonAmbar
import mx.edu.utez.cafeteria.ui.components.CajaIcono
import mx.edu.utez.cafeteria.ui.components.Chip
import mx.edu.utez.cafeteria.ui.components.ChipEstado
import mx.edu.utez.cafeteria.ui.components.EstadoVacio
import mx.edu.utez.cafeteria.ui.components.EtiquetaSeccion
import mx.edu.utez.cafeteria.ui.components.Miniatura
import mx.edu.utez.cafeteria.ui.components.Tarjeta
import mx.edu.utez.cafeteria.ui.theme.Cafe
import mx.edu.utez.cafeteria.ui.theme.CafeteriaTheme

@Composable
fun ComandasScreen(
    state: AppUiState,
    onAvanzar: (Int) -> Unit,
    onCancelar: (Int) -> Unit
) {
    val comandas = state.comandasActivas
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Tarjeta {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CajaIcono(Icons.Default.Receipt)
                    Column(modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)) {
                        EtiquetaSeccion("Comandas en vivo")
                        Text(state.cafeteria.nombre, color = Cafe.Texto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Chip("Espera ${state.tiempoEspera.rango} min", Cafe.TarjetaClara, Cafe.Texto, icono = Icons.Default.Schedule)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Contador(Icons.Default.Inbox, comandas.count { it.estado == EstadoPedido.RECIBIDO }, "Nuevas", Modifier.weight(1f))
                    Contador(Icons.Default.LocalFireDepartment, comandas.count { it.estado == EstadoPedido.PREPARANDO }, "En cocina", Modifier.weight(1f))
                    Contador(Icons.Default.DoneAll, comandas.count { it.estado == EstadoPedido.LISTO }, "Listas", Modifier.weight(1f))
                }
            }
        }
        if (comandas.isEmpty()) {
            item {
                EstadoVacio(Icons.Default.CheckCircle, "Sin comandas pendientes", "Cuando un alumno haga un pedido, aparecerá aquí al instante.")
            }
        }
        items(comandas, key = { it.numero }) { pedido ->
            TarjetaComanda(pedido, onAvanzar = { onAvanzar(pedido.numero) }, onCancelar = { onCancelar(pedido.numero) })
        }
    }
}

@Composable
private fun Contador(icono: ImageVector, valor: Int, texto: String, modifier: Modifier) {
    Surface(color = Cafe.TarjetaClara, shape = RoundedCornerShape(14.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icono, contentDescription = null, tint = Cafe.AmbarTexto, modifier = Modifier.size(18.dp))
            Text("$valor", color = Cafe.Texto, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(texto, color = Cafe.TextoSuave, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun NumeroComanda(numero: Int, fondo: Color = Cafe.AmbarFondo, color: Color = Cafe.AmbarTexto) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .background(fondo, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("#$numero", color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FilasProductos(pedido: Pedido) {
    Surface(color = Cafe.TarjetaClara, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            pedido.lineas.forEach { linea ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Miniatura(linea.foto, linea.categoria, 32.dp)
                    Text("${linea.cantidad}×", modifier = Modifier.padding(start = 10.dp), color = Cafe.AmbarTexto, fontWeight = FontWeight.Bold)
                    Column(modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)) {
                        Text(linea.nombre, color = Cafe.Texto, style = MaterialTheme.typography.bodyMedium)
                        if (linea.detalle.isNotBlank()) {
                            Text(linea.detalle, color = Cafe.AzulClaro, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Text(pesosDec(linea.subtotal), color = Cafe.Texto, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun TarjetaComanda(pedido: Pedido, onAvanzar: () -> Unit, onCancelar: () -> Unit) {
    val accion = when (pedido.estado) {
        EstadoPedido.RECIBIDO -> "Empezar a preparar"
        EstadoPedido.PREPARANDO -> "Marcar como listo"
        else -> "Entregar en ${pedido.mostrador}"
    }
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
                    Text(" ${pedido.hora} • Pago en caja", color = Cafe.TextoSuave, style = MaterialTheme.typography.labelMedium)
                }
            }
            Text(pesosDec(pedido.total), color = Cafe.Texto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        FilasProductos(pedido)
        if (pedido.nota.isNotBlank()) {
            Text("Nota: ${pedido.nota}", color = Cafe.AmbarTexto, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        ChipEstado(pedido.estado)
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onCancelar) {
                Text("Cancelar", color = Cafe.Rojo, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
            BotonAmbar(accion, onAvanzar, modifier = Modifier.weight(1f))
        }
    }
}

@Preview(heightDp = 1200, backgroundColor = 0xFF0B1324, showBackground = true)
@Composable
private fun ComandasPreview() {
    CafeteriaTheme { ComandasScreen(AppUiState(cafeteriaElegida = "nido"), onAvanzar = {}, onCancelar = {}) }
}
