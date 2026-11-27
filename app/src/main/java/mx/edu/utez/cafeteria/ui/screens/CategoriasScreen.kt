package mx.edu.utez.cafeteria.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NoFood
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.edu.utez.cafeteria.data.Categoria
import mx.edu.utez.cafeteria.data.Dias
import mx.edu.utez.cafeteria.data.EspecialesDelDia
import mx.edu.utez.cafeteria.ui.AppUiState
import mx.edu.utez.cafeteria.ui.components.BarraCarrito
import mx.edu.utez.cafeteria.ui.components.EstadoVacio
import mx.edu.utez.cafeteria.ui.components.EtiquetaSeccion
import mx.edu.utez.cafeteria.ui.components.FilaProducto
import mx.edu.utez.cafeteria.ui.theme.Cafe

@Composable
fun CategoriasScreen(
    state: AppUiState,
    onCategoria: (Categoria) -> Unit,
    onAbrir: (Int) -> Unit,
    onAgregar: (Int) -> Unit,
    onQuitar: (Int) -> Unit,
    onVerCarrito: () -> Unit
) {
    val productos = state.productosDe(state.categoria)
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    EtiquetaSeccion(state.cafeteria.nombre)
                    Text("Categorías", color = Cafe.Texto, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(Categoria.entries) { c ->
                        FilterChip(
                            selected = state.categoria == c,
                            onClick = { onCategoria(c) },
                            label = { Text(c.titulo, fontWeight = FontWeight.SemiBold) },
                            leadingIcon = { Icon(c.icono, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Cafe.Tarjeta,
                                labelColor = Cafe.Texto,
                                iconColor = Cafe.AmbarTexto,
                                selectedContainerColor = Cafe.Ambar,
                                selectedLabelColor = Cafe.SobreAmbar,
                                selectedLeadingIconColor = Cafe.SobreAmbar
                            )
                        )
                    }
                }
            }
            item {
                Column {
                    Text(state.categoria.titulo, color = Cafe.Texto, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(state.categoria.subtitulo, color = Cafe.TextoSuave, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (productos.isEmpty()) {
                item { EstadoVacio(Icons.Default.NoFood, "Sin platillos aquí", "${state.cafeteria.nombreCorto} no tiene platillos en esta categoría.") }
            }
            if (state.categoria == Categoria.ESPECIAL) {
                // Especiales agrupados por día; solo los de hoy se pueden pedir
                (1..5).forEach { dia ->
                    val delDia = productos.filter { it.dia == dia }
                    if (delDia.isNotEmpty()) {
                        item(key = "dia_$dia") {
                            val titulo = EspecialesDelDia.porDia[dia]?.titulo.orEmpty()
                            EtiquetaSeccion(
                                "${Dias.nombre(dia)} · $titulo" + if (dia == state.hoy) " · HOY" else "",
                                if (dia == state.hoy) Cafe.AmbarTexto else Cafe.TextoSuave
                            )
                        }
                        items(delDia, key = { it.id }) { p ->
                            FilaProducto(p, state.cantidadDe(p.id), state.motivoNoPedible(p), { onAbrir(p.id) }, { onAgregar(p.id) }, { onQuitar(p.id) })
                        }
                    }
                }
            } else {
                items(productos, key = { it.id }) { p ->
                    FilaProducto(p, state.cantidadDe(p.id), state.motivoNoPedible(p), { onAbrir(p.id) }, { onAgregar(p.id) }, { onQuitar(p.id) })
                }
            }
        }
        BarraCarrito(state.totalPiezas, state.totalCarrito, onVerCarrito)
    }
}
