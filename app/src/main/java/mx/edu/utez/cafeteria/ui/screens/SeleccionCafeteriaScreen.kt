package mx.edu.utez.cafeteria.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.edu.utez.cafeteria.data.Cafeteria
import mx.edu.utez.cafeteria.data.Cafeterias
import mx.edu.utez.cafeteria.ui.components.BotonAmbar
import mx.edu.utez.cafeteria.ui.components.Chip
import mx.edu.utez.cafeteria.ui.components.TopBarSimple
import mx.edu.utez.cafeteria.ui.theme.Cafe
import mx.edu.utez.cafeteria.ui.theme.CafeteriaTheme

@Composable
fun SeleccionCafeteriaScreen(
    cafeterias: List<Cafeteria>,
    onElegir: (String) -> Unit,
    onAtras: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cafe.Fondo)
    ) {
        TopBarSimple("Monedero Halcón", onAtras, Icons.AutoMirrored.Filled.ArrowBack)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Chip("CAMPUS UTEZ • EMILIANO ZAPATA", Cafe.Tarjeta, Cafe.AmbarTexto, icono = Icons.Default.School)
                    Text(
                        "¿En qué cafetería deseas ordenar hoy?",
                        color = Cafe.Texto,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(color = Cafe.Tarjeta, shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Cafe.AmbarTexto, modifier = Modifier.size(20.dp))
                            Text(
                                "Puedes cambiar de cafetería en cualquier momento desde el menú superior.",
                                modifier = Modifier.padding(start = 10.dp),
                                color = Cafe.TextoSuave,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            items(cafeterias, key = { it.id }) { cafeteria ->
                TarjetaCafeteria(cafeteria, onElegir = { onElegir(cafeteria.id) })
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = Cafe.AmbarTexto, modifier = Modifier.size(18.dp))
                        Text(
                            "  SERVICIO OFICIAL INSTITUCIONAL",
                            color = Cafe.TextoSuave,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        "Tus pedidos llegan directo a la cafetería para que los recojas en barra sin hacer fila.",
                        modifier = Modifier.padding(top = 4.dp),
                        color = Cafe.Texto,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaCafeteria(cafeteria: Cafeteria, onElegir: () -> Unit) {
    Surface(color = Cafe.Tarjeta, shape = RoundedCornerShape(20.dp)) {
        Column {
            Box {
                Image(
                    painter = painterResource(cafeteria.foto),
                    contentDescription = "Foto de ${cafeteria.nombre}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                )
                Box(modifier = Modifier.padding(14.dp)) {
                    Chip("Abierto ahora", Color(0xE60B1324), Cafe.Verde, punto = Cafe.Verde)
                }
            }
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(cafeteria.nombre, color = Cafe.Texto, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Surface(color = Cafe.Fondo, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilaDato(Icons.Default.LocationOn, cafeteria.ubicacion)
                        FilaDato(Icons.Default.Schedule, cafeteria.horario)
                    }
                }
                BotonAmbar(
                    texto = "Elegir ${cafeteria.nombreCorto}",
                    onClick = onElegir,
                    icono = Icons.AutoMirrored.Filled.ArrowForward,
                    iconoAlFinal = true
                )
            }
        }
    }
}

@Composable
private fun FilaDato(icono: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = Cafe.AzulClaro, modifier = Modifier.size(20.dp))
        Text(texto, modifier = Modifier.padding(start = 10.dp), color = Cafe.Texto, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(heightDp = 1100)
@Composable
private fun SeleccionPreview() {
    CafeteriaTheme { SeleccionCafeteriaScreen(Cafeterias.lista, onElegir = {}, onAtras = {}) }
}
