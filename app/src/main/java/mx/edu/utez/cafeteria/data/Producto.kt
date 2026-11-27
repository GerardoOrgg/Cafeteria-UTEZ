package mx.edu.utez.cafeteria.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey
import mx.edu.utez.cafeteria.R
import java.util.Locale

// ---------------- Categorías (fijas) ----------------

enum class Categoria(val titulo: String, val subtitulo: String, val icono: ImageVector) {
    ESPECIAL("Especial del día", "Tacos Acorazados UTEZ", Icons.Default.Star),
    DIARIO("Menú del Diario", "Comida corrida y desayunos", Icons.Default.Restaurant),
    BEBIDAS("Bebidas", "Aguas, café y jugos", Icons.Default.LocalCafe),
    SNACKS("Snacks", "Fruta, botanas y barras", Icons.Default.Fastfood),
    POSTRES("Postres", "Gelatinas y panecillos", Icons.Default.Cake)
}

// ---------------- Platillo (tabla "productos" en Room) ----------------

@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val precio: Int, // pesos enteros
    val categoria: Categoria,
    val etiqueta: String? = null, // "Favorito", "Rápido", "Fit"...
    // "res:nombre_drawable" para fotos incluidas en la app, o la ruta de un archivo tomado con la cámara
    val foto: String? = null,
    val disponible: Boolean = true,
    val creadoEn: Long = System.currentTimeMillis()
)

fun nombreLargo(nombre: String, categoria: Categoria): String =
    if (categoria == Categoria.ESPECIAL) "Taco Acorazado · $nombre" else nombre

// ---------------- Cafeterías (fijas) ----------------

data class Cafeteria(
    val id: String,
    val nombre: String,
    val nombreCorto: String,
    val ubicacion: String,
    val horario: String,
    val especialidad: String,
    val foto: Int
)

object Cafeterias {
    val lista = listOf(
        Cafeteria(
            id = "nido",
            nombre = "Cafetería Nido de Halcón",
            nombreCorto = "Nido de Halcón",
            ubicacion = "Edificio Central / Explanada de Halcones",
            horario = "7:30 AM - 6:00 PM",
            especialidad = "Tacos Acorazados, Guisados y Comida Caliente",
            foto = R.drawable.cafe_nido
        ),
        Cafeteria(
            id = "balcon",
            nombre = "Cafetería El Balcón",
            nombreCorto = "El Balcón",
            ubicacion = "Terraza Edificio C / Área de descanso",
            horario = "8:00 AM - 5:00 PM",
            especialidad = "Snacks, Panadería, Cafetería & Bebidas Frías",
            foto = R.drawable.cafe_balcon
        )
    )

    fun porId(id: String?): Cafeteria = lista.find { it.id == id } ?: lista.first()
}

// ---------------- Usuario y pedidos ----------------

data class Usuario(val nombre: String, val matricula: String)

enum class EstadoPedido(val texto: String) {
    RECIBIDO("Recibido"),
    PREPARANDO("En preparación"),
    LISTO("Listo para recoger"),
    ENTREGADO("Completado"),
    CANCELADO("Cancelado");

    val activo: Boolean get() = this == RECIBIDO || this == PREPARANDO || this == LISTO
}

enum class TiempoEspera(val rango: String, val etiqueta: String, val nivel: String) {
    BAJO("5-10", "Minutos", "Fluido"),
    MEDIO("10-15", "Minutos", "Moderado"),
    ALTO("15-25", "Alta carga", "Saturado")
}

// Copia del platillo dentro de un pedido (si luego se edita o borra el platillo, el pedido no cambia)
data class LineaPedido(
    val productoId: Int,
    val nombre: String,
    val precio: Int,
    val cantidad: Int,
    val foto: String?,
    val categoria: Categoria
) {
    val subtotal: Int get() = precio * cantidad
    val nombreLargo: String get() = nombreLargo(nombre, categoria)
}

data class Pedido(
    val numero: Int,
    val cliente: Usuario,
    val cafeteriaId: String,
    val lineas: List<LineaPedido>,
    val estado: EstadoPedido,
    val creadoEn: Long,
    val hora: String,
    val fecha: String, // "Hoy", "Ayer", "Jueves"...
    val deHoy: Boolean,
    val nota: String = "",
    val mostrador: String = "Mostrador 1",
    val minutosPrep: Int? = null,
    val motivo: String? = null
) {
    val total: Int get() = lineas.sumOf { it.subtotal }
}

// ---------------- Formato de precios ----------------

fun pesos(cantidad: Int): String = "\$$cantidad"

fun pesosDec(cantidad: Int): String = String.format(Locale.US, "\$%,d.00", cantidad)
