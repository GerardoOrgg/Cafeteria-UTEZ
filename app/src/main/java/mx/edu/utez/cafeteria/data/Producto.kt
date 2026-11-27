package mx.edu.utez.cafeteria.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import mx.edu.utez.cafeteria.R
import java.time.LocalDate
import java.util.Locale

enum class Categoria(val titulo: String, val subtitulo: String, val icono: ImageVector) {
    ESPECIAL("Especial del día", "Cambia de lunes a viernes", Icons.Default.Star),
    DIARIO("Menú del Diario", "Lo que hay todos los días", Icons.Default.Restaurant),
    BEBIDAS("Bebidas", "Aguas, café y jugos", Icons.Default.LocalCafe),
    SNACKS("Snacks", "Fruta, botanas y barras", Icons.Default.Fastfood),
    POSTRES("Postres y pan", "Pan dulce, gelatinas y pays", Icons.Default.Cake)
}

object Dias {
    val nombres = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

    fun hoy(): Int = LocalDate.now().dayOfWeek.value

    fun nombre(dia: Int): String = nombres.getOrElse(dia - 1) { "" }
}

object EspecialesDelDia {
    data class Especial(val titulo: String, val descripcion: String)

    val porDia = mapOf(
        1 to Especial("Tacos Acorazados", "Doble tortilla con arroz rojo y el guisado que elijas, hechos al momento."),
        2 to Especial("Tacos de pastor, bistec y barbacoa", "Órdenes y tacos sueltos con salsa verde, roja y su consomé."),
        3 to Especial("Enchiladas, enmoladas y suizas", "Bañadas en salsa, con crema, queso y cebolla."),
        4 to Especial("Pozole y antojitos", "Pozole chico o grande con tostadas, flautas y todo lo demás."),
        5 to Especial("Hamburguesas", "Hamburguesas al carbón con papas, y hot dogs.")
    )
}

data class Opcion(val nombre: String, val detalle: String = "", val extra: Int = 0)

@Entity(tableName = "productos", indices = [Index("cafeteriaId")])
data class Producto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cafeteriaId: String,
    val nombre: String,
    val descripcion: String,
    val precio: Int,
    val categoria: Categoria,
    val dia: Int? = null,
    val detalle: String = "",
    val etiqueta: String? = null,
    val foto: String? = null,
    val disponible: Boolean = true,
    val prepMin: Int = 5,
    val prepMax: Int = 10,
    val grupoOpciones: String? = null,
    val opciones: List<Opcion> = emptyList(),
    val recomendacion: String? = null,
    val creadoEn: Long = System.currentTimeMillis()
)

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
            especialidad = "Especial del día, antojitos y comida caliente",
            foto = R.drawable.cafe_nido
        ),
        Cafeteria(
            id = "balcon",
            nombre = "Cafetería El Balcón",
            nombreCorto = "El Balcón",
            ubicacion = "Terraza Edificio C / Área de descanso",
            horario = "8:00 AM - 5:00 PM",
            especialidad = "Snacks, panadería, cafetería y bebidas frías",
            foto = R.drawable.cafe_balcon
        )
    )

    fun porId(id: String?): Cafeteria = lista.find { it.id == id } ?: lista.first()
}

enum class Rol { ALUMNO, ENCARGADO }

data class Sesion(
    val id: Int,
    val nombre: String,
    val matricula: String,
    val correo: String,
    val rol: Rol,
    val cafeteriaId: String?
)

data class Usuario(val nombre: String, val matricula: String)

data class ItemCarrito(
    val productoId: Int,
    val opcion: String?,
    val extra: Int,
    val nota: String,
    val cantidad: Int
) {
    val clave: String get() = "$productoId|${opcion.orEmpty()}|$nota"
}

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

data class LineaPedido(
    val productoId: Int,
    val nombre: String,
    val opcion: String?,
    val nota: String?,
    val precio: Int,
    val cantidad: Int,
    val foto: String?,
    val categoria: Categoria
) {
    val subtotal: Int get() = precio * cantidad

    val detalle: String get() = listOfNotNull(opcion, nota?.takeIf { it.isNotBlank() }?.let { "Nota: $it" }).joinToString(" · ")
}

data class Pedido(
    val numero: Int,
    val cliente: Usuario,
    val cafeteriaId: String,
    val lineas: List<LineaPedido>,
    val estado: EstadoPedido,
    val creadoEn: Long,
    val hora: String,
    val fecha: String,
    val deHoy: Boolean,
    val nota: String = "",
    val mostrador: String = "Mostrador 1",
    val minutosPrep: Int? = null,
    val motivo: String? = null
) {
    val total: Int get() = lineas.sumOf { it.subtotal }
}

fun pesos(cantidad: Int): String = "\$$cantidad"

fun pesosDec(cantidad: Int): String = String.format(Locale.US, "\$%,d.00", cantidad)
