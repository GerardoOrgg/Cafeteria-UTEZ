package mx.edu.utez.cafeteria.data

import mx.edu.utez.cafeteria.data.local.LineaEntity
import mx.edu.utez.cafeteria.data.local.PedidoEntity
import java.util.Calendar

// Lo que trae la app la primera vez que se abre (después todo se edita desde el panel)
object DatosIniciales {

    const val ID_CHILE_POBLANO = 3

    val productos = listOf(
        // Especial del día: Tacos Acorazados
        Producto(1, "Milanesa Crujiente", "Con arroz rojo y frijol de la olla", 55, Categoria.ESPECIAL),
        Producto(2, "Huevo Duro Tradicional", "Doble ración con arroz y frijoles", 55, Categoria.ESPECIAL),
        Producto(ID_CHILE_POBLANO, "Chile Poblano Relleno", "Relleno de queso panela fresco", 55, Categoria.ESPECIAL, disponible = false),
        Producto(4, "Papa con Longaniza", "Guisado casero de Yecapixtla", 55, Categoria.ESPECIAL),
        // Menú del diario
        Producto(5, "Chilaquiles verdes con pollo", "Totopos, crema y queso", 55, Categoria.DIARIO, "Favorito", Fotos.recurso("food_chilaquiles")),
        Producto(6, "Torta de pechuga", "Aguacate, frijol y queso", 45, Categoria.DIARIO, "Rápido", Fotos.recurso("food_torta")),
        Producto(7, "Enchiladas suizas", "3 piezas gratinadas con crema", 55, Categoria.DIARIO, null, Fotos.recurso("food_enchiladas")),
        Producto(8, "Huevos al gusto con frijoles", "Mexicana, jamón o salsa", 48, Categoria.DIARIO, null, Fotos.recurso("food_huevos")),
        Producto(9, "Pechuga asada con ensalada", "Ligera, sazonada al comal", 60, Categoria.DIARIO, "Fit", Fotos.recurso("food_pechuga")),
        Producto(10, "Sopa del día con guisado", "Incluye pasta o consomé", 50, Categoria.DIARIO, null, Fotos.recurso("food_sopa")),
        // Bebidas
        Producto(11, "Horchata 500 ml", "Artesanal con canela fresca", 20, Categoria.BEBIDAS),
        Producto(12, "Agua de jamaica 500 ml", "Natural, poco dulce", 20, Categoria.BEBIDAS),
        Producto(13, "Café americano 12 oz", "Recién hecho", 10, Categoria.BEBIDAS),
        Producto(14, "Refresco 600 ml", "Varios sabores", 20, Categoria.BEBIDAS),
        // Snacks
        Producto(15, "Fruta picada", "Vaso con fruta de temporada", 25, Categoria.SNACKS),
        Producto(16, "Papas y botanas", "Bolsa individual", 18, Categoria.SNACKS),
        Producto(17, "Barra de granola", "Avena y miel", 15, Categoria.SNACKS),
        // Postres
        Producto(18, "Gelatina", "De leche o de agua", 15, Categoria.POSTRES),
        Producto(19, "Panecillo dulce", "Concha, oreja o dona", 12, Categoria.POSTRES),
        Producto(20, "Arroz con leche", "Con canela", 18, Categoria.POSTRES)
    )

    private fun producto(id: Int) = productos.first { it.id == id }

    private fun linea(idProducto: Int, cantidad: Int = 1): LineaEntity {
        val p = producto(idProducto)
        return LineaEntity(pedidoNumero = 0, productoId = p.id, nombre = p.nombre, precio = p.precio, cantidad = cantidad, foto = p.foto, categoria = p.categoria)
    }

    // Hora de hoy (o de hace unos días) en milisegundos
    private fun momento(hora: Int, minuto: Int, diasAtras: Int = 0): Long =
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -diasAtras)
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
        }.timeInMillis

    // Pedidos de ejemplo para que el panel no se vea vacío
    fun pedidos(alumno: Usuario): List<Pair<PedidoEntity, List<LineaEntity>>> = listOf(
        PedidoEntity(85, alumno.nombre, alumno.matricula, "nido", EstadoPedido.ENTREGADO, momento(8, 10, 5), minutosPrep = 6) to listOf(linea(6), linea(13)),
        PedidoEntity(90, alumno.nombre, alumno.matricula, "balcon", EstadoPedido.ENTREGADO, momento(9, 30, 4), minutosPrep = 9) to listOf(linea(5)),
        PedidoEntity(98, "Luis Gómez", "20212DN044", "nido", EstadoPedido.CANCELADO, momento(11, 5), motivo = "Falta de ingrediente (Chile Poblano)") to listOf(linea(ID_CHILE_POBLANO)),
        PedidoEntity(99, "Sofía Morales", "20233TI101", "nido", EstadoPedido.ENTREGADO, momento(11, 21), minutosPrep = 8) to listOf(linea(1)),
        PedidoEntity(100, "Carlos Ruiz", "20203ID088", "nido", EstadoPedido.ENTREGADO, momento(11, 35), mostrador = "Mostrador 2", minutosPrep = 11) to listOf(linea(6, 2), linea(14)),
        PedidoEntity(101, "María Torres", "20223TE012", "nido", EstadoPedido.ENTREGADO, momento(11, 42), minutosPrep = 7) to listOf(linea(5), linea(13)),
        PedidoEntity(102, "Ana López", "20241TI007", "nido", EstadoPedido.PREPARANDO, momento(11, 50)) to listOf(linea(7), linea(12)),
        PedidoEntity(103, "Diego Hernández", "20232TI019", "nido", EstadoPedido.RECIBIDO, momento(11, 54), nota = "Sin cebolla") to listOf(linea(9), linea(11))
    )
}
