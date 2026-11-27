package mx.edu.utez.cafeteria.data.remoto

import mx.edu.utez.cafeteria.data.Categoria
import mx.edu.utez.cafeteria.data.EstadoPedido
import mx.edu.utez.cafeteria.data.Opcion
import mx.edu.utez.cafeteria.data.Producto
import mx.edu.utez.cafeteria.data.Rol
import mx.edu.utez.cafeteria.data.local.LineaEntity
import mx.edu.utez.cafeteria.data.local.PedidoEntity
import mx.edu.utez.cafeteria.data.local.UsuarioEntity

/*
 * DTO = "objeto de transferencia": la forma exacta del JSON que viaja entre la app y la API.
 * Los campos son opcionales (?) porque Gson puede dejar null lo que no venga en el JSON;
 * al convertirlos a las clases de la app se ponen valores por defecto y nunca truena.
 */

// ---------------- Lo que se manda ----------------

data class LoginRequest(val usuario: String, val contrasena: String, val rol: String)

data class RegistroRequest(val nombre: String, val matricula: String, val contrasena: String)

data class RestablecerRequest(val matricula: String, val nombre: String, val nueva: String)

data class DisponibleRequest(val disponible: Boolean)

// El precio NO se manda: el servidor lo calcula con los precios de su base
data class ItemPedidoRequest(val productoId: Int, val opcion: String?, val nota: String?, val cantidad: Int)

data class PedidoRequest(val usuarioId: Int, val cafeteriaId: String, val nota: String, val items: List<ItemPedidoRequest>)

data class CancelarRequest(val motivo: String)

data class TiempoRequest(val tiempo: String)

// ---------------- Lo que se recibe ----------------

data class MensajeDto(val mensaje: String? = null)

data class FotoDto(val ruta: String? = null)

data class UsuarioDto(
    val id: Int? = null,
    val nombre: String? = null,
    val matricula: String? = null,
    val correo: String? = null,
    val rol: String? = null,
    val cafeteriaId: String? = null
)

data class OpcionDto(val nombre: String? = null, val detalle: String? = null, val extra: Int? = null)

// Se usa en los dos sentidos: lo que regresa GET /api/productos y lo que se manda al crear o editar
data class ProductoDto(
    val id: Int? = null,
    val cafeteriaId: String? = null,
    val nombre: String? = null,
    val descripcion: String? = null,
    val precio: Int? = null,
    val categoria: String? = null,
    val dia: Int? = null,
    val detalle: String? = null,
    val etiqueta: String? = null,
    val foto: String? = null,
    val disponible: Boolean? = null,
    val prepMin: Int? = null,
    val prepMax: Int? = null,
    val grupoOpciones: String? = null,
    val opciones: List<OpcionDto>? = null,
    val recomendacion: String? = null,
    val creadoEn: Long? = null
)

data class LineaDto(
    val productoId: Int? = null,
    val nombre: String? = null,
    val opcion: String? = null,
    val nota: String? = null,
    val precio: Int? = null,
    val cantidad: Int? = null,
    val foto: String? = null,
    val categoria: String? = null
)

data class PedidoDto(
    val numero: Int? = null,
    val clienteNombre: String? = null,
    val matricula: String? = null,
    val cafeteriaId: String? = null,
    val estado: String? = null,
    val creadoEn: Long? = null,
    val nota: String? = null,
    val mostrador: String? = null,
    val minutosPrep: Int? = null,
    val motivo: String? = null,
    val total: Int? = null,
    val lineas: List<LineaDto>? = null
)

data class CafeteriaDto(val id: String? = null, val nombre: String? = null, val tiempoEspera: String? = null)

// ---------------- Conversiones JSON <-> clases de la app ----------------

private fun categoriaDe(texto: String?): Categoria = Categoria.entries.find { it.name == texto } ?: Categoria.DIARIO

fun UsuarioDto.aEntidad(): UsuarioEntity = UsuarioEntity(
    id = id ?: 0,
    nombre = nombre.orEmpty(),
    matricula = matricula.orEmpty(),
    correo = correo.orEmpty(),
    rol = Rol.entries.find { it.name == rol } ?: Rol.ALUMNO,
    cafeteriaId = cafeteriaId
)

fun ProductoDto.aProducto(): Producto = Producto(
    id = id ?: 0,
    cafeteriaId = cafeteriaId.orEmpty(),
    nombre = nombre.orEmpty(),
    descripcion = descripcion.orEmpty(),
    precio = precio ?: 0,
    categoria = categoriaDe(categoria),
    dia = dia,
    detalle = detalle.orEmpty(),
    etiqueta = etiqueta,
    foto = foto,
    disponible = disponible ?: true,
    prepMin = prepMin ?: 5,
    prepMax = prepMax ?: 10,
    grupoOpciones = grupoOpciones,
    opciones = opciones.orEmpty().map { Opcion(it.nombre.orEmpty(), it.detalle.orEmpty(), it.extra ?: 0) },
    recomendacion = recomendacion,
    creadoEn = creadoEn ?: System.currentTimeMillis()
)

fun Producto.aDto(): ProductoDto = ProductoDto(
    id = id.takeIf { it != 0 },
    cafeteriaId = cafeteriaId,
    nombre = nombre,
    descripcion = descripcion,
    precio = precio,
    categoria = categoria.name,
    dia = dia,
    detalle = detalle,
    etiqueta = etiqueta,
    foto = foto,
    disponible = disponible,
    prepMin = prepMin,
    prepMax = prepMax,
    grupoOpciones = grupoOpciones,
    opciones = opciones.map { OpcionDto(it.nombre, it.detalle, it.extra) },
    recomendacion = recomendacion,
    creadoEn = creadoEn
)

// Un pedido del servidor se guarda en Room como un renglón de "pedidos" y varios de "lineas_pedido"
fun PedidoDto.aEntidad(): PedidoEntity = PedidoEntity(
    numero = numero ?: 0,
    clienteNombre = clienteNombre.orEmpty(),
    matricula = matricula.orEmpty(),
    cafeteriaId = cafeteriaId.orEmpty(),
    estado = EstadoPedido.entries.find { it.name == estado } ?: EstadoPedido.RECIBIDO,
    creadoEn = creadoEn ?: System.currentTimeMillis(),
    nota = nota.orEmpty(),
    mostrador = mostrador ?: "Mostrador 1",
    minutosPrep = minutosPrep,
    motivo = motivo
)

fun PedidoDto.lineasEntidad(): List<LineaEntity> = lineas.orEmpty().map {
    LineaEntity(
        pedidoNumero = numero ?: 0,
        productoId = it.productoId ?: 0,
        nombre = it.nombre.orEmpty(),
        opcion = it.opcion,
        nota = it.nota,
        precio = it.precio ?: 0,
        cantidad = it.cantidad ?: 1,
        foto = it.foto,
        categoria = categoriaDe(it.categoria)
    )
}
