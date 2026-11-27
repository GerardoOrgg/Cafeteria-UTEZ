package mx.edu.utez.cafeteria.ui.personal

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.edu.utez.cafeteria.config.Configuracion
import mx.edu.utez.cafeteria.data.Categoria
import mx.edu.utez.cafeteria.data.Dias
import mx.edu.utez.cafeteria.data.Opcion
import mx.edu.utez.cafeteria.data.Fotos
import mx.edu.utez.cafeteria.data.Producto
import mx.edu.utez.cafeteria.ui.components.BotonAmbar
import mx.edu.utez.cafeteria.ui.components.EtiquetaSeccion
import mx.edu.utez.cafeteria.ui.components.FotoPlatillo
import mx.edu.utez.cafeteria.ui.components.TopBarSimple
import mx.edu.utez.cafeteria.ui.components.coloresCampo
import mx.edu.utez.cafeteria.ui.theme.Cafe
import java.io.File

/**
 * Formulario para crear (original == null) o editar un platillo.
 * onGuardar recibe el platillo listo y la foto que tenía antes (para borrar el archivo viejo).
 */
@Composable
fun EditarPlatilloScreen(
    original: Producto?,
    onGuardar: (Producto, String?) -> Unit,
    onCancelar: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nombre by rememberSaveable { mutableStateOf(original?.nombre ?: "") }
    var descripcion by rememberSaveable { mutableStateOf(original?.descripcion ?: "") }
    var precio by rememberSaveable { mutableStateOf(original?.precio?.toString() ?: "") }
    var categoria by rememberSaveable { mutableStateOf(original?.categoria ?: Categoria.DIARIO) }
    var etiqueta by rememberSaveable { mutableStateOf(original?.etiqueta) }
    var foto by rememberSaveable { mutableStateOf(original?.foto) }
    var disponible by rememberSaveable { mutableStateOf(original?.disponible ?: true) }
    var detalle by rememberSaveable { mutableStateOf(original?.detalle ?: "") }
    var dia by rememberSaveable { mutableStateOf(original?.dia) }
    var prepMin by rememberSaveable { mutableStateOf((original?.prepMin ?: 5).toString()) }
    var prepMax by rememberSaveable { mutableStateOf((original?.prepMax ?: 10).toString()) }
    var grupo by rememberSaveable { mutableStateOf(original?.grupoOpciones ?: "") }
    var recomendacion by rememberSaveable { mutableStateOf(original?.recomendacion ?: "") }
    // Opciones: nombre, detalle y precio extra (texto para poder editarlo)
    val opciones = remember { mutableStateListOf<OpcionEditable>().apply { original?.opciones?.forEach { add(OpcionEditable(it.nombre, it.detalle, it.extra.toString())) } } }
    var archivoPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    var intentoGuardar by rememberSaveable { mutableStateOf(false) }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }

    // Cambiar de foto: si la anterior la tomó este formulario, se borra
    fun ponerFoto(nueva: String?) {
        if (foto != original?.foto) Fotos.borrar(foto)
        foto = nueva
    }

    // ---------- Cámara ----------
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val ruta = archivoPendiente
        if (ok && ruta != null) ponerFoto(ruta) else ruta?.let { File(it).delete() }
        archivoPendiente = null
    }

    val abrirCamara: () -> Unit = {
        val archivo = Fotos.nuevoArchivo(context)
        archivoPendiente = archivo.absolutePath
        camara.launch(Fotos.uriPara(context, archivo))
    }

    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) abrirCamara() else aviso = "Sin permiso de cámara no se puede tomar la foto. Puedes elegir una de la galería."
    }

    // ---------- Galería ----------
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val ruta = withContext(Dispatchers.IO) { Fotos.copiarDeGaleria(context, uri) }
                if (ruta != null) ponerFoto(ruta) else aviso = "No se pudo copiar la imagen."
            }
        }
    }

    // ---------- Validación ----------
    val precioNumero = precio.toIntOrNull()
    val errorNombre = if (nombre.isBlank()) "Escribe el nombre del platillo" else null
    val errorPrecio = when {
        precioNumero == null -> "Escribe un precio en pesos (solo números)"
        precioNumero <= 0 -> "El precio debe ser mayor a 0"
        precioNumero > Configuracion.PRECIO_MAXIMO -> "El precio máximo es \$${Configuracion.PRECIO_MAXIMO}"
        else -> null
    }
    val minimo = prepMin.toIntOrNull()
    val maximo = prepMax.toIntOrNull()
    val opcionesLlenas = opciones.filter { it.nombre.isNotBlank() }
    val errorExtra = when {
        categoria == Categoria.ESPECIAL && dia == null -> "Elige qué día se sirve este especial"
        minimo == null || maximo == null || minimo < 1 || maximo < minimo -> "Revisa el tiempo de preparación (mínimo ≤ máximo)"
        opcionesLlenas.isNotEmpty() && grupo.isBlank() -> "Ponle nombre al grupo de opciones (ej. Tipo de milanesa)"
        else -> null
    }

    val guardar: () -> Unit = {
        intentoGuardar = true
        if (errorNombre == null && errorPrecio == null && errorExtra == null && precioNumero != null && minimo != null && maximo != null) {
            val producto = (original ?: Producto(cafeteriaId = "", nombre = "", descripcion = "", precio = 0, categoria = categoria)).copy(
                nombre = nombre.trim(),
                descripcion = descripcion.trim(),
                detalle = detalle.trim(),
                precio = precioNumero,
                categoria = categoria,
                dia = if (categoria == Categoria.ESPECIAL) dia else null,
                etiqueta = etiqueta,
                foto = foto,
                disponible = disponible,
                prepMin = minimo,
                prepMax = maximo,
                grupoOpciones = grupo.trim().ifBlank { null },
                opciones = opcionesLlenas.map { Opcion(it.nombre.trim(), it.detalle.trim(), it.extra.toIntOrNull() ?: 0) },
                recomendacion = recomendacion.trim().ifBlank { null }
            )
            onGuardar(producto, original?.foto)
        }
    }

    val cancelar: () -> Unit = {
        // Si se tomó una foto nueva y no se guardó, se borra el archivo
        if (foto != original?.foto) Fotos.borrar(foto)
        onCancelar()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cafe.Fondo)
    ) {
        TopBarSimple(if (original == null) "Nuevo platillo" else "Editar platillo", cancelar, Icons.AutoMirrored.Filled.ArrowBack)

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Foto
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    EtiquetaSeccion("Foto")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(18.dp))
                    ) {
                        FotoPlatillo(foto, categoria, Modifier.fillMaxSize(), tamanoIcono = 56.dp)
                        if (foto == null) {
                            Text(
                                "Sin foto: se mostrará el ícono de la categoría",
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(10.dp),
                                color = Cafe.TextoSuave,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BotonSecundario("Cámara", Icons.Default.PhotoCamera, Modifier.weight(1f)) {
                            val tienePermiso = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                            if (tienePermiso) abrirCamara() else permisoCamara.launch(Manifest.permission.CAMERA)
                        }
                        BotonSecundario("Galería", Icons.Default.PhotoLibrary, Modifier.weight(1f)) {
                            galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                        if (foto != null) {
                            BotonSecundario("Quitar", Icons.Default.DeleteOutline, Modifier.weight(1f)) { ponerFoto(null) }
                        }
                    }
                    aviso?.let { Text(it, color = Cafe.Rojo, style = MaterialTheme.typography.bodySmall) }
                }
            }

            // Datos
            item {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre del platillo") },
                    placeholder = { Text("Ej. Torta de milanesa") },
                    singleLine = true,
                    isError = intentoGuardar && errorNombre != null,
                    supportingText = { if (intentoGuardar && errorNombre != null) Text(errorNombre) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    shape = RoundedCornerShape(14.dp),
                    colors = coloresCampo()
                )
            }
            item {
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Descripción corta (tarjetas)") },
                    placeholder = { Text("Ej. Con aguacate, frijol y queso") },
                    minLines = 2,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    shape = RoundedCornerShape(14.dp),
                    colors = coloresCampo()
                )
            }
            item {
                OutlinedTextField(
                    value = precio,
                    onValueChange = { nuevo -> precio = nuevo.filter { it.isDigit() }.take(5) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Precio en pesos") },
                    prefix = { Text("$ ") },
                    singleLine = true,
                    isError = intentoGuardar && errorPrecio != null,
                    supportingText = { if (intentoGuardar && errorPrecio != null) Text(errorPrecio) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    colors = coloresCampo()
                )
            }

            // Categoría
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EtiquetaSeccion("Categoría")
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Categoria.entries.forEach { c ->
                            OpcionChip(c.titulo, categoria == c) { categoria = c }
                        }
                    }
                }
            }

            // Día del especial
            if (categoria == Categoria.ESPECIAL) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        EtiquetaSeccion("¿Qué día se sirve?")
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            (1..5).forEach { d -> OpcionChip(Dias.nombre(d), dia == d) { dia = d } }
                        }
                    }
                }
            }

            // Descripción larga (hoja de detalle)
            item {
                OutlinedTextField(
                    value = detalle,
                    onValueChange = { detalle = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Descripción larga (al tocar el platillo)") },
                    placeholder = { Text("Ej. Doble tortilla de comal con cama de arroz rojo…") },
                    minLines = 3,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    shape = RoundedCornerShape(14.dp),
                    colors = coloresCampo()
                )
            }

            // Tiempo de preparación
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EtiquetaSeccion("Tiempo de preparación (minutos)")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = prepMin, onValueChange = { prepMin = it.filter { c -> c.isDigit() }.take(3) },
                            modifier = Modifier.weight(1f), label = { Text("Mínimo") }, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp), colors = coloresCampo()
                        )
                        OutlinedTextField(
                            value = prepMax, onValueChange = { prepMax = it.filter { c -> c.isDigit() }.take(3) },
                            modifier = Modifier.weight(1f), label = { Text("Máximo") }, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp), colors = coloresCampo()
                        )
                    }
                }
            }

            // Opciones (ej. Tipo de milanesa: Pollo / Cerdo)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EtiquetaSeccion("Opciones (el alumno elige una)")
                    Text(
                        "Déjalo vacío si el platillo no tiene opciones. El extra se suma al precio (0 = incluido).",
                        color = Cafe.TextoSuave,
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = grupo, onValueChange = { grupo = it },
                        modifier = Modifier.fillMaxWidth(), label = { Text("Nombre del grupo") },
                        placeholder = { Text("Ej. Tipo de milanesa") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        shape = RoundedCornerShape(14.dp), colors = coloresCampo()
                    )
                    opciones.forEachIndexed { i, op ->
                        Surface(color = Cafe.Tarjeta, shape = RoundedCornerShape(14.dp)) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = op.nombre, onValueChange = { opciones[i] = op.copy(nombre = it) },
                                        modifier = Modifier.weight(1f), label = { Text("Opción ${i + 1}") }, singleLine = true,
                                        shape = RoundedCornerShape(12.dp), colors = coloresCampo()
                                    )
                                    OutlinedTextField(
                                        value = op.extra, onValueChange = { opciones[i] = op.copy(extra = it.filter { c -> c.isDigit() }.take(4)) },
                                        modifier = Modifier.width(96.dp), label = { Text("Extra $") }, singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(12.dp), colors = coloresCampo()
                                    )
                                    IconButton(onClick = { opciones.removeAt(i) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Quitar opción ${i + 1}", tint = Cafe.Rojo)
                                    }
                                }
                                OutlinedTextField(
                                    value = op.detalle, onValueChange = { opciones[i] = op.copy(detalle = it) },
                                    modifier = Modifier.fillMaxWidth(), label = { Text("Detalle (opcional)") }, singleLine = true,
                                    shape = RoundedCornerShape(12.dp), colors = coloresCampo()
                                )
                            }
                        }
                    }
                    BotonSecundario("Agregar opción", Icons.Default.Add, Modifier.fillMaxWidth()) {
                        opciones.add(OpcionEditable("", "", "0"))
                    }
                    OutlinedTextField(
                        value = recomendacion, onValueChange = { recomendacion = it },
                        modifier = Modifier.fillMaxWidth(), label = { Text("Recomendación de piezas (opcional)") },
                        placeholder = { Text("Ej. Porción individual recomendada: 1–2 pzas") }, singleLine = true,
                        shape = RoundedCornerShape(14.dp), colors = coloresCampo()
                    )
                    if (intentoGuardar && errorExtra != null) {
                        Text(errorExtra, color = Cafe.Rojo, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Etiqueta
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EtiquetaSeccion("Etiqueta en la foto (opcional)")
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OpcionChip("Ninguna", etiqueta == null) { etiqueta = null }
                        Configuracion.ETIQUETAS.forEach { e ->
                            OpcionChip(e, etiqueta == e) { etiqueta = e }
                        }
                    }
                }
            }

            // Disponible
            item {
                Surface(color = Cafe.Tarjeta, shape = RoundedCornerShape(14.dp)) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Disponible en barra", color = Cafe.Texto, fontWeight = FontWeight.Bold)
                            Text(
                                if (disponible) "Los alumnos lo pueden pedir" else "Aparece como agotado",
                                color = Cafe.TextoSuave,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Switch(
                            checked = disponible,
                            onCheckedChange = { disponible = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Cafe.SobreAmbar, checkedTrackColor = Cafe.Ambar)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .background(Cafe.Barra)
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            BotonAmbar(if (original == null) "Agregar al menú" else "Guardar cambios", guardar, icono = Icons.Default.Save)
        }
    }
}

@Composable
private fun OpcionChip(texto: String, elegida: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = elegida,
        onClick = onClick,
        label = { Text(texto, fontWeight = FontWeight.SemiBold) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Cafe.Tarjeta,
            labelColor = Cafe.Texto,
            selectedContainerColor = Cafe.Ambar,
            selectedLabelColor = Cafe.SobreAmbar
        )
    )
}

@Composable
private fun BotonSecundario(texto: String, icono: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Cafe.Borde),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Icon(icono, contentDescription = null, tint = Cafe.AmbarTexto, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(texto, color = Cafe.Texto, style = MaterialTheme.typography.labelLarge)
    }
}

// Una opción mientras se edita (el extra es texto para poder borrarlo y escribirlo)
private data class OpcionEditable(val nombre: String, val detalle: String, val extra: String)
