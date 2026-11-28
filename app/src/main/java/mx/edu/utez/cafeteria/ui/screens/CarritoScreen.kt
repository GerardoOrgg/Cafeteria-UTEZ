package mx.edu.utez.cafeteria.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.edu.utez.cafeteria.data.DatosIniciales
import mx.edu.utez.cafeteria.data.pesosDec
import mx.edu.utez.cafeteria.ui.AppUiState
import mx.edu.utez.cafeteria.ui.components.BotonAmbar
import mx.edu.utez.cafeteria.ui.components.CajaIcono
import mx.edu.utez.cafeteria.ui.components.EstadoVacio
import mx.edu.utez.cafeteria.ui.components.EtiquetaSeccion
import mx.edu.utez.cafeteria.ui.components.Contador
import mx.edu.utez.cafeteria.ui.components.Miniatura
import mx.edu.utez.cafeteria.data.ItemCarrito
import mx.edu.utez.cafeteria.data.Producto
import mx.edu.utez.cafeteria.data.pesos
import androidx.compose.ui.text.style.TextOverflow
import mx.edu.utez.cafeteria.ui.components.Tarjeta
import mx.edu.utez.cafeteria.ui.components.TopBarSimple
import mx.edu.utez.cafeteria.ui.theme.Cafe
import mx.edu.utez.cafeteria.ui.theme.CafeteriaTheme

@Composable
fun CarritoScreen(
    state: AppUiState,
    onSumar: (String) -> Unit,
    onRestar: (String) -> Unit,
    onNota: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCerrar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cafe.Fondo)
    ) {
        TopBarSimple("Tu pedido", onCerrar, Icons.AutoMirrored.Filled.ArrowBack)

        if (state.lineasCarrito.isEmpty()) {
            EstadoVacio(Icons.Default.ShoppingBag, "Tu pedido está vacío", "Agrega algo del menú para hacer tu pedido.") {
                Button(
                    onClick = onCerrar,
                    colors = ButtonDefaults.buttonColors(containerColor = Cafe.Ambar, contentColor = Cafe.SobreAmbar)
                ) { Text("Ver menú", fontWeight = FontWeight.Bold) }
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Tarjeta {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CajaIcono(Icons.Default.Storefront)
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            EtiquetaSeccion("Punto de entrega")
                            Text(state.cafeteria.nombre, color = Cafe.Texto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(state.cafeteria.ubicacion, color = Cafe.TextoSuave, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            item { EtiquetaSeccion("Resumen de alimentos", Cafe.TextoSuave) }
            items(state.lineasCarrito, key = { it.second.clave }) { (producto, item) ->
                FilaCarrito(producto, item, onSumar = { onSumar(item.clave) }, onRestar = { onRestar(item.clave) })
            }
            item {
                OutlinedTextField(
                    value = state.nota,
                    onValueChange = onNota,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nota para cocina (opcional)") },
                    placeholder = { Text("Ej. sin cebolla, salsa aparte") },
                    minLines = 2,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Cafe.Tarjeta,
                        unfocusedContainerColor = Cafe.Tarjeta,
                        focusedBorderColor = Cafe.Ambar,
                        unfocusedBorderColor = Cafe.Borde,
                        focusedTextColor = Cafe.Texto,
                        unfocusedTextColor = Cafe.Texto,
                        focusedLabelColor = Cafe.AmbarTexto,
                        unfocusedLabelColor = Cafe.TextoSuave,
                        focusedPlaceholderColor = Cafe.TextoSuave,
                        unfocusedPlaceholderColor = Cafe.TextoSuave,
                        cursorColor = Cafe.Ambar
                    )
                )
            }
            item {
                Surface(color = Cafe.AzulInfo, shape = RoundedCornerShape(16.dp)) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        CajaIcono(Icons.Default.Payments, fondo = Cafe.Ambar, tinte = Cafe.SobreAmbar, tamano = 40.dp)
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text("Pagas en caja al recoger", color = Cafe.Texto, fontWeight = FontWeight.Bold)
                            Text(
                                "Di tu nombre o tu número de pedido en el mostrador.",
                                color = Cafe.TextoSuave,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .background(Cafe.Barra)
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Total a pagar", color = Cafe.Texto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Pagar en caja", color = Cafe.TextoSuave, style = MaterialTheme.typography.bodySmall)
                }
                Text(pesosDec(state.totalCarrito), color = Cafe.AmbarTexto, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(" MXN", color = Cafe.TextoSuave, style = MaterialTheme.typography.labelSmall)
            }
            BotonAmbar("Confirmar pedido", onConfirmar)
        }
    }
}

@Composable
private fun FilaCarrito(producto: Producto, item: ItemCarrito, onSumar: () -> Unit, onRestar: () -> Unit) {
    Surface(color = Cafe.TarjetaClara, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Miniatura(producto.foto, producto.categoria)
            Column(modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)) {
                Text(producto.nombre, color = Cafe.Texto, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                val detalle = listOfNotNull(item.opcion, item.nota.takeIf { it.isNotBlank() }?.let { "Nota: $it" }).joinToString(" · ")
                if (detalle.isNotBlank()) {
                    Text(detalle, color = Cafe.AzulClaro, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    "${pesos(producto.precio + item.extra)} c/u · ${pesos((producto.precio + item.extra) * item.cantidad)}",
                    color = Cafe.AmbarTexto,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Contador(item.cantidad, onSumar, onRestar)
        }
    }
}

@Preview(heightDp = 900)
@Composable
private fun CarritoPreview() {
    CafeteriaTheme {
        CarritoScreen(
            state = AppUiState(
                cafeteriaElegida = "nido",
                carrito = listOf(ItemCarrito(1, "Pollo empanizado", 0, "Salsa aparte", 2))
            ),
            onSumar = {}, onRestar = {}, onNota = {}, onConfirmar = {}, onCerrar = {}
        )
    }
}
