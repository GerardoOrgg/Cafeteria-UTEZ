package mx.edu.utez.cafeteria.data

import mx.edu.utez.cafeteria.data.Categoria.BEBIDAS
import mx.edu.utez.cafeteria.data.Categoria.DIARIO
import mx.edu.utez.cafeteria.data.Categoria.ESPECIAL

/**
 * Datos de ejemplo SOLO para las vistas previas (@Preview) de Android Studio.
 *
 * El menú completo, las cuentas y los pedidos de ejemplo ahora viven en el servidor:
 * api/src/main/java/mx/edu/utez/cafeteriaapi/config/DatosIniciales.java
 * (se cargan en MySQL la primera vez que arranca la API).
 * Para cambiar precios o platillos usa el panel del encargado (Menú & Stock) o edita ese archivo.
 */
object DatosIniciales {

    private const val NIDO = "nido"

    // ---------- Atajos para escribir el menú corto ----------

    private fun o(nombre: String, extra: Int = 0, detalle: String = "") = Opcion(nombre, detalle, extra)

    // Los ids se ponen en orden (1, 2, 3...) para que las vistas previas puedan referirse a ellos
    val productos: List<Producto> = listOf(
        Producto(
            id = 1, cafeteriaId = NIDO, nombre = "Taco Acorazado de Milanesa", descripcion = "Con arroz rojo y frijol de la olla",
            precio = 55, categoria = ESPECIAL, dia = 1, grupoOpciones = "Tipo de milanesa",
            opciones = listOf(
                o("Pollo empanizado", detalle = "Pechuga suave, dorada en freidora"),
                o("Cerdo crujiente", detalle = "Corte tradicional, textura extra crocante")
            ),
            etiqueta = "Favorito", prepMin = 8, prepMax = 12
        ),
        Producto(
            id = 2, cafeteriaId = NIDO, nombre = "Chilaquiles", descripcion = "Con pollo, crema y queso", precio = 55,
            categoria = DIARIO, grupoOpciones = "Salsa", opciones = listOf(o("Verde"), o("Roja")),
            etiqueta = "Favorito", foto = Fotos.recurso("food_chilaquiles"), prepMin = 6, prepMax = 10
        ),
        Producto(
            id = 3, cafeteriaId = NIDO, nombre = "Torta", descripcion = "Con aguacate, frijol y queso", precio = 45,
            categoria = DIARIO, grupoOpciones = "Tipo de torta", opciones = listOf(o("Milanesa"), o("Pechuga"), o("Pierna", 5)),
            etiqueta = "Rápido", foto = Fotos.recurso("food_torta"), prepMin = 5, prepMax = 8
        ),
        Producto(
            id = 4, cafeteriaId = NIDO, nombre = "Agua fresca 500 ml", descripcion = "Del día", precio = 20,
            categoria = BEBIDAS, grupoOpciones = "Sabor", opciones = listOf(o("Horchata"), o("Jamaica"), o("Limón")),
            prepMin = 1, prepMax = 2
        )
    )
}
