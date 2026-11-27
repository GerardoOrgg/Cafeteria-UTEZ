package mx.edu.utez.cafeteria.ui.personal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import mx.edu.utez.cafeteria.data.Categoria
import mx.edu.utez.cafeteria.data.Dias
import mx.edu.utez.cafeteria.data.Producto
import mx.edu.utez.cafeteria.data.pesos
import mx.edu.utez.cafeteria.ui.AppUiState
import mx.edu.utez.cafeteria.ui.components.BotonAmbar
import mx.edu.utez.cafeteria.ui.components.CajaIcono
import mx.edu.utez.cafeteria.ui.components.EtiquetaSeccion
import mx.edu.utez.cafeteria.ui.components.Miniatura
import mx.edu.utez.cafeteria.ui.theme.Cafe

// Pestaña "Menú & Stock": lista de platillos con disponible/agotado, editar y borrar
@Composable
fun StockScreen(
    state: AppUiState,
    onCambiarDisponible: (Int, Boolean) -> Unit,
    onNuevo: () -> Unit,
    onEditar: (Producto) -> Unit,
    onBorrar: (Producto) -> Unit
) {
    var porBorrar by remember { mutableStateOf<Producto?>(null) }
    val agotados = state.productos.count { !it.disponible }

    porBorrar?.let { p ->
        AlertDialog(
            onDismissRequest = { porBorrar = null },
            containerColor = Cafe.Tarjeta,
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Cafe.Rojo) },
            title = { Text("¿Borrar \"${p.nombre}\"?", color = Cafe.Texto, fontWeight = FontWeight.Bold) },
            text = { Text("Se quita del menú y no se puede deshacer. Los pedidos que ya lo tienen no cambian.", color = Cafe.TextoSuave) },
            confirmButton = {
                TextButton(onClick = { onBorrar(p); porBorrar = null }) { Text("Borrar", color = Cafe.Rojo, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { porBorrar = null }) { Text("Cancelar", color = Cafe.Texto) }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CajaIcono(Icons.Default.Inventory2)
                Column(modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)) {
                    Text("Platillos y Stock · ${state.cafeteria.nombreCorto}", color = Cafe.Texto, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "${state.productos.size} platillos · $agotados agotados",
                        color = Cafe.TextoSuave,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        item { BotonAmbar("Agregar platillo", onNuevo, icono = Icons.Default.Add) }
        item {
            Text(
                "El switch marca si hay o no hay en barra. Toca un platillo para editarlo.",
                color = Cafe.TextoSuave,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Categoria.entries.forEach { categoria ->
            val deCategoria = state.productosDe(categoria)
            if (deCategoria.isNotEmpty()) {
                item(key = "titulo_${categoria.name}") {
                    Column(modifier = Modifier.padding(top = 8.dp)) { EtiquetaSeccion(categoria.titulo, Cafe.TextoSuave) }
                }
                items(deCategoria, key = { it.id }) { p ->
                    FilaPlatillo(
                        producto = p,
                        onCambio = { onCambiarDisponible(p.id, it) },
                        onEditar = { onEditar(p) },
                        onBorrar = { porBorrar = p }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilaPlatillo(
    producto: Producto,
    onCambio: (Boolean) -> Unit,
    onEditar: () -> Unit,
    onBorrar: () -> Unit
) {
    val disponible = producto.disponible
    Surface(
        onClick = onEditar,
        color = if (disponible) Cafe.Tarjeta else Color(0xFF1A1A2A),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Miniatura(producto.foto, producto.categoria, 44.dp)
            Column(modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)) {
                Text(
                    producto.nombre,
                    color = if (disponible) Cafe.Texto else Cafe.TextoSuave,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (disponible) null else TextDecoration.LineThrough
                )
                Text(
                    "${pesos(producto.precio)} · " + (producto.dia?.let { "${Dias.nombre(it)} · " } ?: "") +
                        if (disponible) "Disponible" else "Agotado",
                    color = if (disponible) Cafe.AzulClaro else Cafe.Rojo,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Switch(
                checked = disponible,
                onCheckedChange = onCambio,
                modifier = Modifier.semantics { contentDescription = "Disponible: ${producto.nombre}" },
                thumbContent = {
                    Icon(
                        if (disponible) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize)
                    )
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Cafe.AzulClaro,
                    checkedTrackColor = Color(0xFF1E3556),
                    checkedBorderColor = Color(0xFF2F4C78),
                    checkedIconColor = Cafe.Fondo,
                    uncheckedThumbColor = Color(0xFFFFD6D6),
                    uncheckedTrackColor = Color(0xFF8B1E2B),
                    uncheckedBorderColor = Color(0xFF8B1E2B),
                    uncheckedIconColor = Color(0xFF8B1E2B)
                )
            )
            IconButton(onClick = onEditar) {
                Icon(Icons.Default.Edit, contentDescription = "Editar ${producto.nombre}", tint = Cafe.TextoSuave)
            }
            IconButton(onClick = onBorrar) {
                Icon(Icons.Default.Delete, contentDescription = "Borrar ${producto.nombre}", tint = Cafe.Rojo)
            }
        }
    }
}
