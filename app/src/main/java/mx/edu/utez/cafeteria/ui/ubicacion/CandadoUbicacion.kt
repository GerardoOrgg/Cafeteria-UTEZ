package mx.edu.utez.cafeteria.ui.ubicacion

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.WrongLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import mx.edu.utez.cafeteria.config.Configuracion
import mx.edu.utez.cafeteria.ui.components.BotonAmbar
import mx.edu.utez.cafeteria.ui.components.CajaIcono
import mx.edu.utez.cafeteria.ui.theme.Cafe
import java.util.Locale

private sealed interface EstadoUbicacion {
    data object Verificando : EstadoUbicacion
    data object SinPermiso : EstadoUbicacion
    data object GpsApagado : EstadoUbicacion
    data object SinSenal : EstadoUbicacion
    data class Fuera(val metros: Float) : EstadoUbicacion
    data object Dentro : EstadoUbicacion
}

@Composable
fun CandadoUbicacion(contenido: @Composable () -> Unit) {
    if (!Configuracion.VALIDAR_UBICACION) {
        contenido()
        return
    }

    val context = LocalContext.current
    var estado by remember { mutableStateOf<EstadoUbicacion>(EstadoUbicacion.Verificando) }
    var revision by remember { mutableIntStateOf(0) }
    var yaSePidio by remember { mutableStateOf(false) }

    val permisos = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        if (resultado.values.any { it }) revision++ else estado = EstadoUbicacion.SinPermiso
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { revision++ }

    LaunchedEffect(revision) {
        if (revision == 0) return@LaunchedEffect
        if (!tienePermiso(context)) {
            estado = EstadoUbicacion.SinPermiso
            if (!yaSePidio) {
                yaSePidio = true
                permisos.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
            return@LaunchedEffect
        }
        if (!gpsEncendido(context)) {
            estado = EstadoUbicacion.GpsApagado
            return@LaunchedEffect
        }
        if (estado != EstadoUbicacion.Dentro) estado = EstadoUbicacion.Verificando

        val ubicacion = obtenerUbicacion(context)
        estado = if (ubicacion == null) {
            EstadoUbicacion.SinSenal
        } else {
            val distancia = FloatArray(1)
            Location.distanceBetween(
                ubicacion.latitude, ubicacion.longitude,
                Configuracion.UTEZ_LATITUD, Configuracion.UTEZ_LONGITUD,
                distancia
            )
            if (distancia[0] <= Configuracion.RADIO_METROS) EstadoUbicacion.Dentro
            else EstadoUbicacion.Fuera(distancia[0])
        }
    }

    when (val e = estado) {
        EstadoUbicacion.Dentro -> contenido()

        EstadoUbicacion.Verificando -> Bloqueo(
            icono = Icons.Default.MyLocation,
            titulo = "Verificando tu ubicación…",
            mensaje = "La app solo funciona dentro del campus de la UTEZ.",
            cargando = true
        )

        EstadoUbicacion.SinPermiso -> Bloqueo(
            icono = Icons.Default.LocationOff,
            titulo = "Necesitamos tu ubicación",
            mensaje = "Para pedir en la cafetería tienes que estar dentro de la UTEZ. Permite el acceso a tu ubicación para continuar.",
            textoBoton = "Dar permiso",
            onBoton = {
                permisos.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            },
            textoSecundario = "Abrir ajustes de la app",
            onSecundario = {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                )
            }
        )

        EstadoUbicacion.GpsApagado -> Bloqueo(
            icono = Icons.Default.LocationOff,
            titulo = "Tu ubicación está apagada",
            mensaje = "Activa la ubicación del teléfono para verificar que estás en la UTEZ.",
            textoBoton = "Activar ubicación",
            onBoton = { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) },
            textoSecundario = "Ya la activé, reintentar",
            onSecundario = { revision++ }
        )

        EstadoUbicacion.SinSenal -> Bloqueo(
            icono = Icons.Default.LocationOff,
            titulo = "No pudimos ubicarte",
            mensaje = "No se obtuvo tu ubicación. Revisa que tengas señal de GPS e inténtalo otra vez.",
            textoBoton = "Reintentar",
            onBoton = { revision++ }
        )

        is EstadoUbicacion.Fuera -> Bloqueo(
            icono = Icons.Default.WrongLocation,
            titulo = "No es posible usar la app aquí",
            mensaje = "La cafetería solo recibe pedidos dentro de ${Configuracion.RADIO_METROS.toInt()} m de la UTEZ. " +
                "Estás a ${distanciaTexto(e.metros)} del campus.",
            textoBoton = "Ya estoy en la UTEZ, reintentar",
            onBoton = { revision++ }
        )
    }
}

@Composable
private fun Bloqueo(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    mensaje: String,
    cargando: Boolean = false,
    textoBoton: String? = null,
    onBoton: () -> Unit = {},
    textoSecundario: String? = null,
    onSecundario: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cafe.Fondo)
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CajaIcono(icono, tamano = 88.dp)
            Text(titulo, color = Cafe.Texto, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(mensaje, color = Cafe.TextoSuave, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            if (cargando) CircularProgressIndicator(color = Cafe.Ambar, modifier = Modifier.size(36.dp))
            if (textoBoton != null) BotonAmbar(textoBoton, onBoton, icono = Icons.Default.LocationOn)
            if (textoSecundario != null) {
                TextButton(onClick = onSecundario) { Text(textoSecundario, color = Cafe.AmbarTexto, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

private fun tienePermiso(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

private fun gpsEncendido(context: Context): Boolean {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return lm.isLocationEnabled
}

@SuppressLint("MissingPermission")
private suspend fun obtenerUbicacion(context: Context): Location? = try {
    val cliente = LocationServices.getFusedLocationProviderClient(context)
    withTimeoutOrNull(15_000) {
        cliente.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token).await()
    } ?: cliente.lastLocation.await()
} catch (e: Exception) {
    null
}

private fun distanciaTexto(metros: Float): String =
    if (metros < 1000) "${metros.toInt()} m" else String.format(Locale.US, "%.1f km", metros / 1000)
