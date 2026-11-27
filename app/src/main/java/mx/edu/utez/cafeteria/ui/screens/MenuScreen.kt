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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import mx.edu.utez.cafeteria.data.Categoria
import mx.edu.utez.cafeteria.data.Producto
import mx.edu.utez.cafeteria.data.pesos
import mx.edu.utez.cafeteria.ui.AppUiState
import mx.edu.utez.cafeteria.ui.components.BarraCarrito
import mx.edu.utez.cafeteria.ui.components.CampoBusqueda
import mx.edu.utez.cafeteria.ui.components.Chip
import mx.edu.utez.cafeteria.ui.components.Contador
import mx.edu.utez.cafeteria.ui.components.EtiquetaSeccion
import mx.edu.utez.cafeteria.ui.components.FilaProducto
import mx.edu.utez.cafeteria.ui.components.FotoPlatillo
import mx.edu.utez.cafeteria.ui.sensores.DetectorAgitar
import mx.edu.utez.cafeteria.ui.theme.Cafe
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MenuScreen(
    state: AppUiState,
    onCambiarCafeteria: () -> Unit,
    onBuscar: (String) -> Unit,
    onAgregar: (Int) -> Unit,
    onQuitar: (Int) -> Unit,
    onVerCarrito: () -> Unit,
    onVerCategoria: (Categoria) -> Unit,
    onAgitar: () -> Unit,
    onCerrarSugerencia: () -> Unit
) {
    // Acelerómetro: agita el teléfono para que la app te sugiera un platillo
    DetectorAgitar(onAgitar = onAgitar)

    state.sugerencia?.let { sugerido ->
        DialogoSugerencia(
            producto = sugerido,
            onAgregar = {
                onAgregar(sugerido.id)
                onCerrarSugerencia()
            },
            onOtra = onAgitar,
            onCerrar = onCerrarSugerencia
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        onClick = onCambiarCafeteria,
                        color = Cafe.Tarjeta,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Cafe.AmbarTexto, modifier = Modifier.size(18.dp))
                            Text(
                                state.cafeteria.nombre,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp),
                                color = Cafe.Texto,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2
                            )
                            Text("(CAMBIAR)", color = Cafe.AmbarTexto, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(color = Cafe.Tarjeta, shape = RoundedCornerShape(50)) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = Cafe.TextoSuave, modifier = Modifier.size(16.dp))
                            Text(
                                "Servicio:\n${state.cafeteria.horario}",
                                modifier = Modifier.padding(start = 6.dp),
                                color = Cafe.TextoSuave,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
            item { CampoBusqueda(state.busqueda, onBuscar, "Buscar tacos, guisados, bebidas…") }

            if (state.busqueda.isNotBlank()) {
                val texto = state.busqueda.trim()
                val resultados = state.productos.filter {
                    it.nombre.contains(texto, ignoreCase = true) || it.descripcion.contains(texto, ignoreCase = true)
                }
                item {
                    Text(
                        if (resultados.isEmpty()) "No encontramos “$texto” en el menú." else "Resultados (${resultados.size})",
                        color = Cafe.TextoSuave,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                items(resultados, key = { it.id }) { p ->
                    FilaProducto(p, state.cantidad(p.id), !p.disponible, { onAgregar(p.id) }, { onQuitar(p.id) })
                }
            } else {
                item {
                    Surface(color = Cafe.AzulInfo, shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = Cafe.AmbarTexto)
                            Text(
                                "¿No sabes qué pedir? Agita tu teléfono y te sugerimos algo.",
                                modifier = Modifier.padding(start = 10.dp),
                                color = Cafe.Texto,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                val especiales = state.productosDe(Categoria.ESPECIAL)
                if (especiales.isNotEmpty()) {
                    item { TarjetaEspecial(state, especiales, onAgregar, onQuitar) }
                }
                val diario = state.productosDe(Categoria.DIARIO)
                if (diario.isNotEmpty()) {
                    item {
                        Column {
                            EtiquetaSeccion("Comida corrida & desayunos")
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("Menú del Diario", modifier = Modifier.weight(1f), color = Cafe.Texto, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                Text("Opciones fijas", color = Cafe.TextoSuave, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    items(diario.chunked(2)) { fila ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            fila.forEach { p ->
                                TarjetaPlatillo(
                                    producto = p,
                                    cantidad = state.cantidad(p.id),
                                    onAgregar = { onAgregar(p.id) },
                                    onQuitar = { onQuitar(p.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (fila.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Más para tu descanso…", modifier = Modifier.weight(1f), color = Cafe.Texto, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { onVerCategoria(Categoria.BEBIDAS) }) {
                            Text("Ver catálogo", color = Cafe.AmbarTexto, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(Categoria.BEBIDAS, Categoria.SNACKS, Categoria.POSTRES).forEach { c ->
                            TileCategoria(c, Modifier.weight(1f)) { onVerCategoria(c) }
                        }
                    }
                }
            }
        }
        BarraCarrito(state.totalPiezas, state.totalCarrito, onVerCarrito)
    }
}

@Composable
private fun TarjetaEspecial(
    state: AppUiState,
    especiales: List<Producto>,
    onAgregar: (Int) -> Unit,
    onQuitar: (Int) -> Unit
) {
    val dia = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-MX"))
    Surface(shape = RoundedCornerShape(20.dp), color = Cafe.Tarjeta) {
        Column(
            modifier = Modifier
                .background(Brush.verticalGradient(listOf(Color(0xFF26345A), Cafe.Tarjeta)))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Cafe.AmbarTexto, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        EtiquetaSeccion("Especial del $dia")
                    }
                    Text("Tacos Acorazados UTEZ", color = Cafe.Texto, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "Hechos al momento sobre cama de arroz rojo y frijoles refritos.",
                        color = Cafe.TextoSuave,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Chip("${state.tiempoEspera.rango} min", Cafe.Ambar, Cafe.SobreAmbar, icono = Icons.Default.Bolt)
            }
            especiales.forEach { p ->
                FilaProducto(p, state.cantidad(p.id), !p.disponible, { onAgregar(p.id) }, { onQuitar(p.id) })
            }
        }
    }
}

@Composable
private fun TarjetaPlatillo(
    producto: Producto,
    cantidad: Int,
    onAgregar: () -> Unit,
    onQuitar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val agotado = !producto.disponible
    Surface(color = Cafe.Tarjeta, shape = RoundedCornerShape(16.dp), modifier = modifier) {
        Column {
            Box {
                FotoPlatillo(
                    foto = producto.foto,
                    categoria = producto.categoria,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                )
                if (producto.etiqueta != null) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Chip(producto.etiqueta, Color(0xD90B1324), Cafe.AmbarTexto)
                    }
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(producto.nombre, color = Cafe.Texto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    if (agotado) "Agotado temporalmente" else producto.descripcion,
                    color = if (agotado) Cafe.Rojo else Cafe.TextoSuave,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(pesos(producto.precio), modifier = Modifier.weight(1f), color = Cafe.AmbarTexto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (cantidad == 0) {
                        Surface(onClick = onAgregar, enabled = !agotado, color = Cafe.TarjetaClara, shape = RoundedCornerShape(10.dp)) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("Pedir", color = if (agotado) Cafe.TextoSuave else Cafe.Texto, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(6.dp))
                                Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = Cafe.Texto, modifier = Modifier.size(16.dp))
                            }
                        }
                    } else {
                        Contador(cantidad, onAgregar, onQuitar, tamano = 30.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TileCategoria(categoria: Categoria, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Cafe.Tarjeta, shape = RoundedCornerShape(16.dp), modifier = modifier) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Cafe.TarjetaClara, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(categoria.icono, contentDescription = null, tint = Cafe.AmbarTexto)
            }
            Text(categoria.titulo, color = Cafe.Texto, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(categoria.subtitulo, color = Cafe.TextoSuave, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun DialogoSugerencia(producto: Producto, onAgregar: () -> Unit, onOtra: () -> Unit, onCerrar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = Cafe.Tarjeta,
        icon = { Icon(Icons.Default.Casino, contentDescription = null, tint = Cafe.AmbarTexto) },
        title = { Text("¿Qué tal esto?", color = Cafe.Texto, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FotoPlatillo(
                    foto = producto.foto,
                    categoria = producto.categoria,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    tamanoIcono = 48.dp
                )
                Text(producto.nombre, color = Cafe.Texto, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(producto.descripcion, color = Cafe.TextoSuave)
                Text(pesos(producto.precio), color = Cafe.AmbarTexto, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {
            TextButton(onClick = onAgregar) { Text("Agregar al pedido", color = Cafe.AmbarTexto, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onOtra) { Text("Otra opción", color = Cafe.Texto) }
        }
    )
}
