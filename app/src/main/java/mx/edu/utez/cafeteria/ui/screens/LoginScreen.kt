package mx.edu.utez.cafeteria.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import mx.edu.utez.cafeteria.config.Configuracion
import mx.edu.utez.cafeteria.data.ResultadoAuth
import mx.edu.utez.cafeteria.data.Rol
import mx.edu.utez.cafeteria.ui.components.BotonAmbar
import mx.edu.utez.cafeteria.ui.components.Chip
import mx.edu.utez.cafeteria.ui.components.coloresCampo
import mx.edu.utez.cafeteria.ui.theme.Cafe

@Composable
fun LoginScreen(
    onIniciarSesion: (String, String, Rol, (ResultadoAuth) -> Unit) -> Unit,
    onRegistrar: (String, String, String, (ResultadoAuth) -> Unit) -> Unit,
    onRestablecer: (String, String, String, (ResultadoAuth) -> Unit) -> Unit
) {
    var registrando by rememberSaveable { mutableStateOf(false) }
    var rol by rememberSaveable { mutableStateOf(Rol.ALUMNO) }
    var usuario by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var nombre by rememberSaveable { mutableStateOf("") }
    var confirmar by rememberSaveable { mutableStateOf("") }
    var verContrasena by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var olvido by remember { mutableStateOf(false) }

    val terminar: (ResultadoAuth) -> Unit = { r ->
        cargando = false
        error = (r as? ResultadoAuth.Error)?.mensaje
    }

    if (olvido) {
        if (rol == Rol.ALUMNO) {
            DialogoRestablecer(onRestablecer, onCerrar = { olvido = false })
        } else {
            AlertDialog(
                onDismissRequest = { olvido = false },
                containerColor = Cafe.Tarjeta,
                title = { Text("Contraseña de encargado", color = Cafe.Texto, fontWeight = FontWeight.Bold) },
                text = { Text("Las cuentas de encargado viven en el servidor: se crean en api/.../config/DatosIniciales.java (o directo en la tabla usuarios de MySQL).", color = Cafe.TextoSuave) },
                confirmButton = { TextButton(onClick = { olvido = false }) { Text("Entendido", color = Cafe.AmbarTexto) } }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF15254A), Cafe.Fondo, Cafe.Fondo)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Chip("NIDO DE HALCÓN • EL BALCÓN", Cafe.Tarjeta, Cafe.AmbarTexto, icono = Icons.Default.Restaurant)

            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(Color.White, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.LunchDining, contentDescription = "Logo Cafetería UTEZ", tint = Cafe.Fondo, modifier = Modifier.size(48.dp))
                }
                Box(modifier = Modifier.offset(x = 10.dp, y = 8.dp)) {
                    Chip("UTEZ", Cafe.Ambar, Cafe.SobreAmbar)
                }
            }

            Text(
                buildAnnotatedString {
                    append(if (registrando) "Crea tu cuenta en Cafetería " else "Bienvenido a Cafetería ")
                    withStyle(SpanStyle(color = Cafe.AzulClaro)) { append("UTEZ") }
                },
                color = Cafe.Texto,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                if (registrando) "Regístrate con tu matrícula para pedir desde tu celular"
                else "Ingresa con tu cuenta institucional para consultar menús y ordenar en línea",
                color = Cafe.TextoSuave,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            if (!registrando) {
                Surface(color = Cafe.Tarjeta, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        Segmento("Estudiante", Icons.Default.School, rol == Rol.ALUMNO, Modifier.weight(1f)) {
                            rol = Rol.ALUMNO; error = null
                        }
                        Segmento("Encargado", Icons.Default.Badge, rol == Rol.ENCARGADO, Modifier.weight(1f)) {
                            rol = Rol.ENCARGADO; error = null
                        }
                    }
                }
            }

            Surface(color = Cafe.Tarjeta, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (registrando) {
                        Etiqueta("Nombre completo")
                        Campo(nombre, { nombre = it; error = null }, "ej. Juan Pérez", Icons.Default.Person, KeyboardCapitalization.Words)
                    }

                    Etiqueta(if (registrando) "Matrícula" else if (rol == Rol.ALUMNO) "Matrícula o Correo Institucional" else "Correo del encargado", requerido = true)
                    Campo(
                        usuario, { usuario = it.trim(); error = null },
                        when {
                            registrando -> "ej. 20213TI045"
                            rol == Rol.ALUMNO -> "ej. 20213ti045@${Configuracion.DOMINIO_CORREO}"
                            else -> "ej. nido@${Configuracion.DOMINIO_CORREO}"
                        },
                        Icons.Default.Badge,
                        if (registrando) KeyboardCapitalization.Characters else KeyboardCapitalization.None,
                        KeyboardType.Email
                    )

                    Etiqueta("Contraseña")
                    CampoContrasena(contrasena, { contrasena = it; error = null }, "Tu contraseña", verContrasena) { verContrasena = !verContrasena }

                    if (registrando) {
                        Etiqueta("Confirmar contraseña")
                        CampoContrasena(confirmar, { confirmar = it; error = null }, "Escríbela otra vez", verContrasena) { verContrasena = !verContrasena }
                    }

                    error?.let { Text(it, color = Cafe.Rojo, style = MaterialTheme.typography.bodySmall) }

                    Spacer(Modifier.height(4.dp))
                    BotonAmbar(
                        texto = when {
                            cargando -> "Un momento…"
                            registrando -> "Crear cuenta"
                            else -> "Iniciar Sesión"
                        },
                        onClick = {
                            if (registrando) {
                                error = validarRegistro(nombre, usuario, contrasena, confirmar)
                                if (error == null) {
                                    cargando = true
                                    onRegistrar(nombre, usuario, contrasena, terminar)
                                }
                            } else {
                                error = if (usuario.isBlank() || contrasena.isBlank()) "Escribe tu usuario y tu contraseña." else null
                                if (error == null) {
                                    cargando = true
                                    onIniciarSesion(usuario, contrasena, rol, terminar)
                                }
                            }
                        },
                        icono = if (registrando) Icons.Default.PersonAdd else Icons.AutoMirrored.Filled.Login,
                        enabled = !cargando
                    )
                }
            }

            if (!registrando) {
                TextButton(onClick = { olvido = true }) {
                    Text("¿Olvidaste la contraseña?", color = Cafe.AzulClaro, textDecoration = TextDecoration.Underline)
                }
            }
            if (rol == Rol.ALUMNO) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (registrando) "¿Ya tienes cuenta?" else "¿No tienes cuenta?", color = Cafe.TextoSuave)
                    TextButton(onClick = { registrando = !registrando; error = null }) {
                        Text(if (registrando) "Inicia sesión" else "Regístrate", color = Cafe.AmbarTexto, fontWeight = FontWeight.Bold)
                    }
                }
            }

          

            Text(
                "Universidad Tecnológica Emiliano Zapata del Estado de Morelos",
                color = Cafe.TextoSuave,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun validarRegistro(nombre: String, matricula: String, contrasena: String, confirmar: String): String? = when {
    nombre.isBlank() -> "Escribe tu nombre completo."
    matricula.length < 8 || !matricula.all { it.isLetterOrDigit() } -> "Escribe una matrícula válida (letras y números, mínimo 8)."
    contrasena.length < Configuracion.MIN_CONTRASENA -> "La contraseña debe tener al menos ${Configuracion.MIN_CONTRASENA} caracteres."
    contrasena != confirmar -> "Las contraseñas no coinciden."
    else -> null
}

@Composable
private fun Segmento(texto: String, icono: ImageVector, elegido: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (elegido) Cafe.Ambar else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icono, contentDescription = null, tint = if (elegido) Cafe.SobreAmbar else Cafe.Texto, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(texto, color = if (elegido) Cafe.SobreAmbar else Cafe.Texto, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun Etiqueta(texto: String, requerido: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(texto, modifier = Modifier.weight(1f), color = Cafe.Texto, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        if (requerido) Text("Requerido", color = Cafe.AmbarTexto, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Campo(
    valor: String,
    onCambio: (String) -> Unit,
    placeholder: String,
    icono: ImageVector,
    mayusculas: KeyboardCapitalization = KeyboardCapitalization.None,
    teclado: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(icono, contentDescription = null, tint = Cafe.TextoSuave) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = mayusculas, keyboardType = teclado),
        shape = RoundedCornerShape(14.dp),
        colors = coloresCampo()
    )
}

@Composable
private fun CampoContrasena(valor: String, onCambio: (String) -> Unit, placeholder: String, visible: Boolean, onVer: () -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = Cafe.TextoSuave) },
        trailingIcon = {
            IconButton(onClick = onVer) {
                Icon(
                    if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña",
                    tint = Cafe.TextoSuave
                )
            }
        },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        shape = RoundedCornerShape(14.dp),
        colors = coloresCampo()
    )
}

@Composable
private fun DialogoRestablecer(
    onRestablecer: (String, String, String, (ResultadoAuth) -> Unit) -> Unit,
    onCerrar: () -> Unit
) {
    var matricula by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var listo by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = Cafe.Tarjeta,
        icon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = Cafe.AmbarTexto) },
        title = { Text(if (listo) "Contraseña cambiada" else "Nueva contraseña", color = Cafe.Texto, fontWeight = FontWeight.Bold) },
        text = {
            if (listo) {
                Text("Listo. Ya puedes iniciar sesión con tu nueva contraseña.", color = Cafe.TextoSuave)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Escribe tu matrícula y tu nombre tal como te registraste.", color = Cafe.TextoSuave, style = MaterialTheme.typography.bodySmall)
                    Campo(matricula, { matricula = it.trim() }, "Matrícula", Icons.Default.Badge, KeyboardCapitalization.Characters)
                    Campo(nombre, { nombre = it }, "Nombre completo", Icons.Default.Person, KeyboardCapitalization.Words)
                    CampoContrasena(nueva, { nueva = it }, "Nueva contraseña", visible = false) {}
                    mensaje?.let { Text(it, color = Cafe.Rojo, style = MaterialTheme.typography.bodySmall) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when {
                    listo -> onCerrar()
                    nueva.length < Configuracion.MIN_CONTRASENA -> mensaje = "Mínimo ${Configuracion.MIN_CONTRASENA} caracteres."
                    else -> onRestablecer(matricula, nombre, nueva) { r ->
                        if (r is ResultadoAuth.Error) mensaje = r.mensaje else listo = true
                    }
                }
            }) { Text(if (listo) "Iniciar sesión" else "Cambiar", color = Cafe.AmbarTexto, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            if (!listo) TextButton(onClick = onCerrar) { Text("Cancelar", color = Cafe.Texto) }
        }
    )
}
