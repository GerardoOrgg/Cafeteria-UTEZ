package mx.edu.utez.cafeteria.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import mx.edu.utez.cafeteria.config.Configuracion
import mx.edu.utez.cafeteria.data.Cafeteria
import mx.edu.utez.cafeteria.data.Categoria
import mx.edu.utez.cafeteria.data.ItemCarrito
import mx.edu.utez.cafeteria.data.Producto
import mx.edu.utez.cafeteria.data.pesosDec
import mx.edu.utez.cafeteria.ui.components.BotonRedondo
import mx.edu.utez.cafeteria.ui.components.Chip
import mx.edu.utez.cafeteria.ui.components.FotoPlatillo
import mx.edu.utez.cafeteria.ui.components.coloresCampo
import mx.edu.utez.cafeteria.ui.theme.Cafe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetallePlatilloSheet(
    producto: Producto,
    cafeteria: Cafeteria,
    motivo: String?,
    onAgregar: (ItemCarrito) -> Unit,
    onCerrar: () -> Unit
) {
    val estadoHoja = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var opcionElegida by rememberSaveable(producto.id) { mutableStateOf(producto.opciones.firstOrNull()?.nombre) }
    var piezas by rememberSaveable(producto.id) { mutableIntStateOf(1) }
    var nota by rememberSaveable(producto.id) { mutableStateOf("") }

    val opcion = producto.opciones.find { it.nombre == opcionElegida }
    val precioPieza = producto.precio + (opcion?.extra ?: 0)
    val faltaOpcion = producto.opciones.isNotEmpty() && opcion == null

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = estadoHoja,
        containerColor = Cafe.Barra
    ) {
        Column {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(18.dp))
                ) {
                    FotoPlatillo(producto.foto, producto.categoria, Modifier.fillMaxWidth().height(220.dp), tamanoIcono = 64.dp)
                    IconButton(
                        onClick = onCerrar,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color(0xCC0B1324), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Cafe.Texto)
                    }
                    val insignia = when {
                        producto.categoria == Categoria.ESPECIAL -> "Especialidad ${cafeteria.nombreCorto}"
                        producto.etiqueta != null -> producto.etiqueta
                        else -> null
                    }
                    if (insignia != null) {
                        Box(modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)) {
                            Chip(insignia, Color(0xE60B1324), Cafe.AmbarTexto, icono = Icons.Default.LocalFireDepartment)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        producto.nombre,
                        modifier = Modifier.weight(1f),
                        color = Cafe.Texto,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(pesosDec(producto.precio), color = Cafe.AmbarTexto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("MXN c/u", color = Cafe.TextoSuave, style = MaterialTheme.typography.labelSmall)
                    }
                }

                Text(producto.detalle.ifBlank { producto.descripcion }, color = Cafe.TextoSuave, style = MaterialTheme.typography.bodyLarge)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = Cafe.AmbarTexto, modifier = Modifier.size(16.dp))
                    Text(
                        " Prep. aproximada: ${producto.prepMin} - ${producto.prepMax} min",
                        color = Cafe.AmbarTexto,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (motivo != null) {
                    Surface(color = Cafe.RojoFondo, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "No se puede pedir: $motivo",
                            modifier = Modifier.padding(12.dp),
                            color = Cafe.Rojo,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (producto.opciones.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(producto.grupoOpciones ?: "Elige una opción", color = Cafe.Texto, style = MaterialTheme.typography.titleMedium)
                        Text(" *", color = Cafe.AmbarTexto, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        Chip("Obligatorio", Cafe.TarjetaClara, Cafe.AmbarTexto)
                    }
                    producto.opciones.forEach { o ->
                        val elegida = o.nombre == opcionElegida
                        Surface(
                            onClick = { opcionElegida = o.nombre },
                            color = if (elegida) Cafe.TarjetaClara else Cafe.Tarjeta,
                            shape = RoundedCornerShape(14.dp),
                            border = if (elegida) BorderStroke(1.dp, Cafe.Ambar.copy(alpha = 0.6f)) else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = elegida,
                                    onClick = { opcionElegida = o.nombre },
                                    colors = RadioButtonDefaults.colors(selectedColor = Cafe.Ambar, unselectedColor = Cafe.TextoSuave)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(o.nombre, color = Cafe.Texto, style = MaterialTheme.typography.titleMedium)
                                    if (o.detalle.isNotBlank()) {
                                        Text(o.detalle, color = Cafe.TextoSuave, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                Chip(if (o.extra == 0) "Incluido" else "+\$${o.extra}", Cafe.Fondo, if (o.extra == 0) Cafe.TextoSuave else Cafe.AmbarTexto)
                                Spacer(Modifier.width(6.dp))
                            }
                        }
                    }
                }

                Surface(color = Cafe.Tarjeta, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Número de piezas", color = Cafe.Texto, style = MaterialTheme.typography.titleMedium)
                            producto.recomendacion?.let {
                                Text(it, color = Cafe.TextoSuave, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Surface(color = Cafe.Fondo, shape = RoundedCornerShape(50)) {
                            Row(modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                BotonRedondo(Icons.Default.Remove, "Quitar una pieza", { if (piezas > 1) piezas-- }, fondo = Cafe.TarjetaClara, tinte = Cafe.Texto, enabled = piezas > 1)
                                Column(modifier = Modifier.widthIn(min = 64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("$piezas", color = Cafe.Texto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(if (piezas == 1) "unidad" else "unidades", color = Cafe.Texto, style = MaterialTheme.typography.labelMedium)
                                }
                                BotonRedondo(Icons.Default.Add, "Agregar una pieza", { if (piezas < 20) piezas++ })
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, tint = Cafe.Texto, modifier = Modifier.size(18.dp))
                    Text("  Notas adicionales para cocina", color = Cafe.Texto, style = MaterialTheme.typography.titleMedium)
                }
                OutlinedTextField(
                    value = nota,
                    onValueChange = { nota = it.take(Configuracion.MAX_NOTA) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej. Salsa aparte, servilletas extra, bien dorado…") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    shape = RoundedCornerShape(14.dp),
                    colors = coloresCampo()
                )
                Text(
                    "${nota.length}/${Configuracion.MAX_NOTA} caracteres",
                    modifier = Modifier.fillMaxWidth(),
                    color = Cafe.TextoSuave,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.End
                )
                Spacer(Modifier.height(4.dp))
            }

            Button(
                onClick = {
                    onAgregar(ItemCarrito(producto.id, opcion?.nombre, opcion?.extra ?: 0, nota.trim(), piezas))
                },
                enabled = motivo == null && !faltaOpcion,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cafe.Ambar,
                    contentColor = Cafe.SobreAmbar,
                    disabledContainerColor = Cafe.TarjetaClara,
                    disabledContentColor = Cafe.TextoSuave
                )
            ) {
                Icon(Icons.Default.ShoppingBag, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Agregar al pedido", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Text("• ${pesosDec(precioPieza * piezas)} MXN", fontWeight = FontWeight.Bold)
            }
        }
    }
}
