package mx.edu.utez.cafeteria.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta de las vistas: azul marino oscuro con acentos ámbar
object Cafe {
    val Fondo = Color(0xFF0B1324)
    val Barra = Color(0xFF101A2E)
    val Tarjeta = Color(0xFF18223A)
    val TarjetaClara = Color(0xFF212C45)
    val Borde = Color(0xFF2A3654)
    val Ambar = Color(0xFFFDB45C)
    val AmbarTexto = Color(0xFFF5A623)
    val AmbarFondo = Color(0xFF3A2D14)
    val SobreAmbar = Color(0xFF2B1A00)
    val Texto = Color(0xFFE8ECF5)
    val TextoSuave = Color(0xFF9AA6BF)
    val Verde = Color(0xFF34D399)
    val VerdeFondo = Color(0xFF113A2C)
    val Rojo = Color(0xFFF87171)
    val RojoFondo = Color(0xFF3B1A22)
    val AzulInfo = Color(0xFF0E2A4D)
    val AzulClaro = Color(0xFFBFD4F2)
}

private val ColoresCafe = darkColorScheme(
    primary = Cafe.Ambar,
    onPrimary = Cafe.SobreAmbar,
    secondary = Cafe.AzulClaro,
    secondaryContainer = Cafe.TarjetaClara,
    onSecondaryContainer = Cafe.Texto,
    background = Cafe.Fondo,
    onBackground = Cafe.Texto,
    surface = Cafe.Tarjeta,
    onSurface = Cafe.Texto,
    surfaceVariant = Cafe.TarjetaClara,
    onSurfaceVariant = Cafe.TextoSuave,
    outline = Cafe.Borde,
    error = Cafe.Rojo
)

@Composable
fun CafeteriaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColoresCafe,
        typography = Typography(),
        content = content
    )
}
