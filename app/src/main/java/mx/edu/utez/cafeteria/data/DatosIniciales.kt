package mx.edu.utez.cafeteria.data

import mx.edu.utez.cafeteria.data.Categoria.BEBIDAS
import mx.edu.utez.cafeteria.data.Categoria.DIARIO
import mx.edu.utez.cafeteria.data.Categoria.ESPECIAL

object DatosIniciales {
    private const val NIDO = "nido"

    private fun o(nombre: String, extra: Int = 0, detalle: String = "") = Opcion(nombre, detalle, extra)


}
