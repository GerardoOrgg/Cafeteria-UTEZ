package mx.edu.utez.cafeteria.ui.screens

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
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.edu.utez.cafeteria.data.Cafeterias
import mx.edu.utez.cafeteria.data.Categoria
import mx.edu.utez.cafeteria.data.EstadoPedido
import mx.edu.utez.cafeteria.data.LineaPedido
import mx.edu.utez.cafeteria.data.Pedido
import mx.edu.utez.cafeteria.data.pesos
import mx.edu.utez.cafeteria.data.Usuario
import mx.edu.utez.cafeteria.data.pesosDec
import mx.edu.utez.cafeteria.ui.AppUiState
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
fun PedidosScreen(
    state: AppUiState,
    onPedirDeNuevo: (Pedido) -> Unit,
    onVolverAlMenu: () -> Unit
) {
    var enCurso by rememberSaveable { mutableStateOf(true) }
    val activos = state.misPedidos.filter { it.estado.activo }
    val anteriores = state.misPedidos.filterNot { it.estado.activo }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Mis Pedidos", modifier = Modifier.weight(1f), color = Cafe.Texto, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                if (activos.isNotEmpty()) {
                    Chip("${activos.size} Activo${if (activos.size > 1) "s" else ""}", Cafe.Tarjeta, Cafe.Texto, punto = Cafe.Ambar)
                }
            }
        }
        item {
            Surface(color = Cafe.Tarjeta, shape = RoundedCornerShape(14.dp)) {
                Row(modifier = Modifier.padding(4.dp)) {
                    Pestana("En curso (${activos.size})", Icons.Default.LunchDining, enCurso, Modifier.weight(1f)) { enCurso = true }
                    Pestana("Historial", Icons.Default.History, !enCurso, Modifier.weight(1f)) { enCurso = false }
                }
            }
        }

        if (enCurso) {
            if (activos.isEmpty()) {
                item {
                    EstadoVacio(Icons.Default.Receipt, "No tienes pedidos en curso", "Cuando pidas algo, aquí verás cuándo está listo para recoger.")
                }
            }
            items(activos, key = { it.numero }) { pedido ->
                TarjetaPedidoActivo(pedido, state.usuario)
            }
            if (anteriores.isNotEmpty()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Pedidos Anteriores", modifier = Modifier.weight(1f), color = Cafe.Texto, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Últimos 7 días", color = Cafe.TextoSuave, style = MaterialTheme.typography.labelMedium)
                    }
                }
                items(anteriores.take(2), key = { "a${it.numero}" }) { pedido ->
                    TarjetaPedidoAnterior(pedido) { onPedirDeNuevo(pedido) }
                }
            }
        } else {
            if (anteriores.isEmpty()) {
                item { EstadoVacio(Icons.Default.History, "Sin historial todavía", "Tus pedidos entregados aparecerán aquí.") }
            }
            items(anteriores, key = { "h${it.numero}" }) { pedido ->
                TarjetaPedidoAnterior(pedido) { onPedirDeNuevo(pedido) }
            }
        }

        item {
            BotonAmbar("Volver al Menú", onVolverAlMenu, icono = Icons.Default.Restaurant)
        }
    }
}

@Composable
private fun Pestana(texto: String, icono: ImageVector, seleccionada: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (seleccionada) Cafe.AzulInfo else Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icono, contentDescription = null, tint = if (seleccionada) Cafe.AmbarTexto else Cafe.TextoSuave, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(texto, color = if (seleccionada) Cafe.Texto else Cafe.TextoSuave, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TarjetaPedidoActivo(pedido: Pedido, usuario: Usuario) {
    val cafeteria = Cafeterias.porId(pedido.cafeteriaId)
    Tarjeta {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CajaIcono(Icons.Default.Storefront)
            Column(modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)) {
                EtiquetaSeccion("Punto de entrega")
                Text(cafeteria.nombre, color = Cafe.Texto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            ChipEstado(pedido.estado)
        }

        Text("Pedido #${pedido.numero} · ${pedido.hora}", color = Cafe.TextoSuave, style = MaterialTheme.typography.labelMedium)
        ProgresoPedido(pedido.estado)

        EtiquetaSeccion("Resumen de alimentos", Cafe.TextoSuave)
        pedido.lineas.forEach { linea ->
            Surface(color = Cafe.TarjetaClara, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Miniatura(linea.foto, linea.categoria)
                    Column(modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)) {
                        Text("${linea.cantidad}× ${linea.nombreLargo}", color = Cafe.Texto, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                        Text("${pesos(linea.precio)} c/u", color = Cafe.TextoSuave, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Text(pesosDec(linea.subtotal), color = Cafe.Texto, fontWeight = FontWeight.Bold)
                }
            }
        }
        if (pedido.nota.isNotBlank()) {
            Text("Nota: ${pedido.nota}", color = Cafe.TextoSuave, style = MaterialTheme.typography.bodySmall)
        }

        Surface(color = Cafe.TarjetaClara, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Total a pagar", color = Cafe.Texto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Pagar en caja", color = Cafe.TextoSuave, style = MaterialTheme.typography.bodySmall)
                }
                Text(pesosDec(pedido.total), color = Cafe.AmbarTexto, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(" MXN", color = Cafe.TextoSuave, style = MaterialTheme.typography.labelSmall)
            }
        }

        Surface(color = Cafe.AzulInfo, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Cafe.Ambar, RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.HowToReg, contentDescription = null, tint = Cafe.SobreAmbar)
                }
                Column(
                    modifier = Modifier.padding(start = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EtiquetaSeccion(if (pedido.estado == EstadoPedido.LISTO) "¡Ya puedes recogerlo!" else "Paso para recoger")
                    Text(
                        buildAnnotatedString {
                            append("Recoge en ")
                            withStyle(SpanStyle(color = Cafe.AmbarTexto, fontWeight = FontWeight.Bold)) { append(pedido.mostrador) }
                            append(" indicando tu nombre:")
                        },
                        color = Cafe.Texto,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Surface(color = Cafe.Fondo, shape = RoundedCornerShape(10.dp)) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(usuario.nombre, color = Cafe.Texto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Matrícula: ${usuario.matricula}", color = Cafe.TextoSuave, style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgresoPedido(estado: EstadoPedido) {
    val pasos = listOf(EstadoPedido.RECIBIDO to "Recibido", EstadoPedido.PREPARANDO to "Preparando", EstadoPedido.LISTO to "Listo")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        pasos.forEach { (paso, texto) ->
            val alcanzado = paso.ordinal <= estado.ordinal
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(if (alcanzado) Cafe.Ambar else Cafe.TarjetaClara, RoundedCornerShape(3.dp))
                )
                Text(
                    texto,
                    modifier = Modifier.padding(top = 4.dp),
                    color = if (alcanzado) Cafe.Texto else Cafe.TextoSuave,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (paso == estado) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun TarjetaPedidoAnterior(pedido: Pedido, onPedirDeNuevo: () -> Unit) {
    val cafeteria = Cafeterias.porId(pedido.cafeteriaId)
    val primera = pedido.lineas.first()
    val resumen = pedido.lineas.joinToString(" + ") { "${it.cantidad}× ${it.nombre}" }
    Tarjeta(padding = 14.dp, espacio = 10.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Event, contentDescription = null, tint = Cafe.TextoSuave, modifier = Modifier.size(18.dp))
            Text(pedido.fecha, modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp), color = Cafe.Texto, fontWeight = FontWeight.Bold)
            ChipEstado(pedido.estado)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Miniatura(primera.foto, primera.categoria, 40.dp)
            Column(modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)) {
                Text(resumen, color = Cafe.Texto, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(cafeteria.nombre, color = Cafe.TextoSuave, style = MaterialTheme.typography.bodySmall)
            }
            Text(pesosDec(pedido.total), color = Cafe.Texto, fontWeight = FontWeight.Bold)
        }
        if (pedido.estado == EstadoPedido.ENTREGADO) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onPedirDeNuevo) {
                    Icon(Icons.Default.Replay, contentDescription = null, tint = Cafe.AmbarTexto, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Pedir de nuevo", color = Cafe.AmbarTexto, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(heightDp = 1300, backgroundColor = 0xFF0B1324, showBackground = true)
@Composable
private fun PedidosPreview() {
    val activo = Pedido(
        numero = 104,
        cliente = Usuario("Juan Pérez", "20213TI045"),
        cafeteriaId = "nido",
        lineas = listOf(LineaPedido(1, "Milanesa Crujiente", 55, 2, null, Categoria.ESPECIAL)),
        estado = EstadoPedido.PREPARANDO,
        creadoEn = 0L,
        hora = "12:05 PM",
        fecha = "Hoy",
        deHoy = true
    )
    CafeteriaTheme {
        PedidosScreen(state = AppUiState(cafeteriaId = "nido", pedidos = listOf(activo)), onPedirDeNuevo = {}, onVolverAlMenu = {})
    }
}
