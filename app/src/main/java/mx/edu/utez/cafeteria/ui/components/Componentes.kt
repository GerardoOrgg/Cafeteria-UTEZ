package mx.edu.utez.cafeteria.ui.components

import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.material3.TextFieldColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.edu.utez.cafeteria.data.Producto
import mx.edu.utez.cafeteria.data.pesos
import mx.edu.utez.cafeteria.data.Categoria
import mx.edu.utez.cafeteria.data.EstadoPedido
import mx.edu.utez.cafeteria.data.Fotos
import mx.edu.utez.cafeteria.ui.theme.Cafe

// ---------- Barras ----------

@Composable
fun LogoApp(titulo: String = "Cafetería UTEZ") {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(Cafe.Ambar, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.LunchDining, contentDescription = null, tint = Cafe.SobreAmbar, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Cafe.Texto)
    }
}

@Composable
fun Avatar(tamano: Dp = 36.dp) {
    Box(
        modifier = Modifier
            .size(tamano)
            .background(Cafe.AzulClaro, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Cafe.Fondo, modifier = Modifier.size(tamano * 0.55f))
    }
}

@Composable
fun TopBarAlumno(nombreCafeteria: String, onCafeteria: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Cafe.Barra)
            .statusBarsPadding()
            .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LogoApp()
        Spacer(Modifier.weight(1f))
        Surface(onClick = onCafeteria, color = Color.Transparent, shape = RoundedCornerShape(8.dp)) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Storefront, contentDescription = null, tint = Cafe.AmbarTexto, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    nombreCafeteria,
                    color = Cafe.AmbarTexto,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 110.dp)
                )
                Icon(Icons.Default.ExpandMore, contentDescription = "Cambiar cafetería", tint = Cafe.AmbarTexto, modifier = Modifier.size(18.dp))
            }
        }
        IconButton(onClick = {}) {
            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = Cafe.Texto)
        }
        Avatar()
    }
}

@Composable
fun TopBarSimple(titulo: String, onAtras: (() -> Unit)?, navIcono: ImageVector? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Cafe.Barra)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onAtras != null && navIcono != null) {
            IconButton(onClick = onAtras) {
                Icon(navIcono, contentDescription = "Regresar", tint = Cafe.Texto)
            }
        } else {
            Spacer(Modifier.width(8.dp))
        }
        LogoApp(titulo)
    }
}

data class ItemNav(val titulo: String, val icono: ImageVector, val insignia: Int = 0)

@Composable
fun BarraNavegacion(items: List<ItemNav>, seleccionado: Int, onSeleccionar: (Int) -> Unit) {
    NavigationBar(containerColor = Cafe.Barra) {
        items.forEachIndexed { indice, item ->
            NavigationBarItem(
                selected = indice == seleccionado,
                onClick = { onSeleccionar(indice) },
                icon = {
                    if (item.insignia > 0) {
                        BadgedBox(badge = {
                            Badge(containerColor = Cafe.Ambar, contentColor = Cafe.SobreAmbar) { Text("${item.insignia}") }
                        }) { Icon(item.icono, contentDescription = null) }
                    } else {
                        Icon(item.icono, contentDescription = null)
                    }
                },
                label = { Text(item.titulo, fontWeight = FontWeight.SemiBold, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Cafe.Ambar,
                    selectedTextColor = Cafe.Ambar,
                    indicatorColor = Cafe.AmbarFondo,
                    unselectedIconColor = Cafe.TextoSuave,
                    unselectedTextColor = Cafe.TextoSuave
                )
            )
        }
    }
}

// ---------- Bloques ----------

@Composable
fun Tarjeta(
    modifier: Modifier = Modifier,
    color: Color = Cafe.Tarjeta,
    padding: Dp = 16.dp,
    espacio: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(modifier = modifier.fillMaxWidth(), color = color, shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(padding), verticalArrangement = Arrangement.spacedBy(espacio), content = content)
    }
}

@Composable
fun EtiquetaSeccion(texto: String, color: Color = Cafe.AmbarTexto) {
    Text(
        texto.uppercase(),
        color = color,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}

@Composable
fun Chip(
    texto: String,
    fondo: Color = Cafe.AmbarFondo,
    colorTexto: Color = Cafe.AmbarTexto,
    icono: ImageVector? = null,
    punto: Color? = null
) {
    Surface(color = fondo, shape = RoundedCornerShape(50)) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (punto != null) {
                Box(Modifier.size(8.dp).background(punto, CircleShape))
                Spacer(Modifier.width(6.dp))
            }
            if (icono != null) {
                Icon(icono, contentDescription = null, tint = colorTexto, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
            }
            Text(texto, color = colorTexto, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
fun ChipEstado(estado: EstadoPedido) {
    when (estado) {
        EstadoPedido.RECIBIDO -> Chip("Recibido", punto = Cafe.Ambar)
        EstadoPedido.PREPARANDO -> Chip("En preparación", Cafe.VerdeFondo, Cafe.Verde, punto = Cafe.Verde)
        EstadoPedido.LISTO -> Chip("Listo para recoger", Cafe.Verde, Cafe.Fondo)
        EstadoPedido.ENTREGADO -> Chip("Completado", Cafe.VerdeFondo, Cafe.Verde, icono = Icons.Default.DoneAll)
        EstadoPedido.CANCELADO -> Chip("Cancelado", Cafe.RojoFondo, Cafe.Rojo)
    }
}

@Composable
fun CajaIcono(icono: ImageVector, fondo: Color = Cafe.TarjetaClara, tinte: Color = Cafe.AmbarTexto, tamano: Dp = 44.dp) {
    Box(
        modifier = Modifier
            .size(tamano)
            .background(fondo, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, contentDescription = null, tint = tinte, modifier = Modifier.size(tamano * 0.5f))
    }
}

@Composable
fun BotonAmbar(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    iconoAlFinal: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Cafe.Ambar,
            contentColor = Cafe.SobreAmbar,
            disabledContainerColor = Cafe.TarjetaClara,
            disabledContentColor = Cafe.TextoSuave
        )
    ) {
        if (icono != null && !iconoAlFinal) {
            Icon(icono, contentDescription = null)
            Spacer(Modifier.width(8.dp))
        }
        Text(texto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (icono != null && iconoAlFinal) {
            Spacer(Modifier.width(8.dp))
            Icon(icono, contentDescription = null)
        }
    }
}

@Composable
fun BotonRedondo(
    icono: ImageVector,
    descripcion: String,
    onClick: () -> Unit,
    fondo: Color = Cafe.Ambar,
    tinte: Color = Cafe.SobreAmbar,
    tamano: Dp = 40.dp,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = if (enabled) fondo else Cafe.TarjetaClara,
        modifier = Modifier.size(tamano)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icono, contentDescription = descripcion, tint = if (enabled) tinte else Cafe.TextoSuave, modifier = Modifier.size(tamano * 0.55f))
        }
    }
}

@Composable
fun Contador(cantidad: Int, onMas: () -> Unit, onMenos: () -> Unit, tamano: Dp = 32.dp) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        BotonRedondo(Icons.Default.Remove, "Quitar uno", onMenos, fondo = Cafe.TarjetaClara, tinte = Cafe.Texto, tamano = tamano)
        Text(
            "$cantidad",
            modifier = Modifier.widthIn(min = 26.dp),
            textAlign = TextAlign.Center,
            color = Cafe.Texto,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        BotonRedondo(Icons.Default.Add, "Agregar uno", onMas, tamano = tamano)
    }
}

// Foto del platillo: la de la cámara, la incluida en la app o el ícono de su categoría
@Composable
fun FotoPlatillo(foto: String?, categoria: Categoria, modifier: Modifier = Modifier, tamanoIcono: Dp = 32.dp) {
    val context = LocalContext.current
    val modelo = Fotos.modelo(context, foto)
    if (modelo != null) {
        AsyncImage(
            model = modelo,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(modifier = modifier.background(Cafe.TarjetaClara), contentAlignment = Alignment.Center) {
            Icon(categoria.icono, contentDescription = null, tint = Cafe.AmbarTexto, modifier = Modifier.size(tamanoIcono))
        }
    }
}

@Composable
fun Miniatura(foto: String?, categoria: Categoria, tamano: Dp = 44.dp) {
    FotoPlatillo(
        foto = foto,
        categoria = categoria,
        modifier = Modifier
            .size(tamano)
            .clip(RoundedCornerShape(10.dp)),
        tamanoIcono = tamano * 0.5f
    )
}

// Fila de platillo: especial del día, búsqueda y categorías. Tocarla abre la hoja de detalle.
// motivo != null significa que no se puede pedir (agotado, o especial de otro día)
@Composable
fun FilaProducto(
    producto: Producto,
    cantidad: Int,
    motivo: String?,
    onAbrir: () -> Unit,
    onAgregar: () -> Unit,
    onQuitar: () -> Unit
) {
    val bloqueado = motivo != null
    Surface(onClick = onAbrir, color = Cafe.TarjetaClara, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Miniatura(producto.foto, producto.categoria)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    producto.nombre,
                    color = if (bloqueado) Cafe.TextoSuave else Cafe.Texto,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (!producto.disponible) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    motivo ?: producto.descripcion,
                    color = if (bloqueado) Cafe.Rojo else Cafe.TextoSuave,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(pesos(producto.precio), color = Cafe.AmbarTexto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (cantidad == 0) {
                BotonRedondo(Icons.Default.Add, "Agregar ${producto.nombre}", onAgregar, enabled = !bloqueado)
            } else {
                Contador(cantidad, onAgregar, onQuitar)
            }
        }
    }
}

@Composable
fun CampoBusqueda(valor: String, onCambio: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (valor.isNotEmpty()) {
                IconButton(onClick = { onCambio("") }) { Icon(Icons.Default.Close, contentDescription = "Borrar búsqueda") }
            } else {
                Icon(Icons.Default.Tune, contentDescription = null)
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Cafe.Tarjeta,
            unfocusedContainerColor = Cafe.Tarjeta,
            focusedBorderColor = Cafe.Ambar,
            unfocusedBorderColor = Cafe.Borde,
            focusedTextColor = Cafe.Texto,
            unfocusedTextColor = Cafe.Texto,
            cursorColor = Cafe.Ambar,
            focusedLeadingIconColor = Cafe.TextoSuave,
            unfocusedLeadingIconColor = Cafe.TextoSuave,
            focusedTrailingIconColor = Cafe.TextoSuave,
            unfocusedTrailingIconColor = Cafe.TextoSuave,
            focusedPlaceholderColor = Cafe.TextoSuave,
            unfocusedPlaceholderColor = Cafe.TextoSuave
        )
    )
}

@Composable
fun BarraCarrito(piezas: Int, total: Int, onVerCarrito: () -> Unit) {
    if (piezas == 0) return
    Surface(color = Cafe.Barra) {
        Button(
            onClick = onVerCarrito,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Cafe.Ambar, contentColor = Cafe.SobreAmbar)
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text("Ver mi pedido ($piezas)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            Text(pesos(total), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun EstadoVacio(icono: ImageVector, titulo: String, mensaje: String, accion: (@Composable () -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CajaIcono(icono, tamano = 80.dp)
        Text(titulo, color = Cafe.Texto, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(mensaje, color = Cafe.TextoSuave, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        if (accion != null) {
            Spacer(Modifier.height(4.dp))
            accion()
        }
    }
}

// Espacio para que el contenido no quede bajo la barra de navegación del sistema
@Composable
fun EspacioInferior() {
    Spacer(Modifier.navigationBarsPadding())
}

// Colores de los campos de texto en tema oscuro
@Composable
fun coloresCampo(): TextFieldColors = OutlinedTextFieldDefaults.colors(
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
    cursorColor = Cafe.Ambar,
    errorContainerColor = Cafe.Tarjeta,
    errorTextColor = Cafe.Texto
)
