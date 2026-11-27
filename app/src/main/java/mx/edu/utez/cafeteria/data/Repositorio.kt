package mx.edu.utez.cafeteria.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import mx.edu.utez.cafeteria.data.local.AppDatabase
import mx.edu.utez.cafeteria.data.local.LineaEntity
import mx.edu.utez.cafeteria.data.local.PedidoConLineas
import mx.edu.utez.cafeteria.data.local.PedidoEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// Único punto de entrada a los datos: Room (tablas) + DataStore (ajustes)
class Repositorio(context: Context) {

    private val db = AppDatabase.obtener(context)
    private val productoDao = db.productoDao()
    private val pedidoDao = db.pedidoDao()
    private val preferencias = Preferencias(context)

    val productos: Flow<List<Producto>> = productoDao.observarTodos()
    val pedidos: Flow<List<Pedido>> = pedidoDao.observarTodos().map { lista -> lista.map { it.aPedido() } }
    val ajustes: Flow<Ajustes> = preferencias.ajustes

    // La primera vez que se abre la app, llena la base con el menú y pedidos de ejemplo
    suspend fun sembrarSiVacio() {
        if (productoDao.contar() > 0) return
        productoDao.insertarTodos(DatosIniciales.productos)
        val alumno = ajustes.first().usuario
        DatosIniciales.pedidos(alumno).forEach { (pedido, lineas) ->
            pedidoDao.crearPedido(pedido, lineas)
        }
    }

    // ---------- Platillos ----------

    suspend fun guardarProducto(producto: Producto) {
        if (producto.id == 0) productoDao.insertar(producto) else productoDao.actualizar(producto)
    }

    suspend fun borrarProducto(producto: Producto) {
        productoDao.borrar(producto)
        Fotos.borrar(producto.foto)
    }

    suspend fun cambiarDisponible(id: Int, disponible: Boolean) = productoDao.cambiarDisponible(id, disponible)

    // ---------- Pedidos ----------

    suspend fun crearPedido(usuario: Usuario, cafeteriaId: String, lineas: List<Pair<Producto, Int>>, nota: String): Int {
        val pedido = PedidoEntity(
            clienteNombre = usuario.nombre,
            matricula = usuario.matricula,
            cafeteriaId = cafeteriaId,
            estado = EstadoPedido.RECIBIDO,
            creadoEn = System.currentTimeMillis(),
            nota = nota
        )
        val filas = lineas.map { (p, cantidad) ->
            LineaEntity(pedidoNumero = 0, productoId = p.id, nombre = p.nombre, precio = p.precio, cantidad = cantidad, foto = p.foto, categoria = p.categoria)
        }
        return pedidoDao.crearPedido(pedido, filas)
    }

    // RECIBIDO -> PREPARANDO -> LISTO -> ENTREGADO
    suspend fun avanzarPedido(numero: Int) {
        val p = pedidoDao.obtener(numero) ?: return
        val siguiente = when (p.estado) {
            EstadoPedido.RECIBIDO -> p.copy(estado = EstadoPedido.PREPARANDO)
            EstadoPedido.PREPARANDO -> p.copy(estado = EstadoPedido.LISTO)
            EstadoPedido.LISTO -> {
                val minutos = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - p.creadoEn).toInt()
                p.copy(estado = EstadoPedido.ENTREGADO, minutosPrep = minutos.coerceAtLeast(1))
            }
            else -> return
        }
        pedidoDao.actualizar(siguiente)
    }

    suspend fun cancelarPedido(numero: Int, motivo: String) {
        val p = pedidoDao.obtener(numero) ?: return
        pedidoDao.actualizar(p.copy(estado = EstadoPedido.CANCELADO, motivo = motivo))
    }

    // ---------- Ajustes ----------

    suspend fun guardarCafeteria(id: String) = preferencias.guardarCafeteria(id)
    suspend fun guardarTiempo(tiempo: TiempoEspera) = preferencias.guardarTiempo(tiempo)
    suspend fun guardarUsuario(usuario: Usuario) = preferencias.guardarUsuario(usuario)

    // ---------- Conversión de Room a lo que usan las pantallas ----------

    private fun PedidoConLineas.aPedido(): Pedido {
        val dias = diasDesde(pedido.creadoEn)
        return Pedido(
            numero = pedido.numero,
            cliente = Usuario(pedido.clienteNombre, pedido.matricula),
            cafeteriaId = pedido.cafeteriaId,
            lineas = lineas.map { LineaPedido(it.productoId, it.nombre, it.precio, it.cantidad, it.foto, it.categoria) },
            estado = pedido.estado,
            creadoEn = pedido.creadoEn,
            hora = SimpleDateFormat("h:mm a", Locale.US).format(Date(pedido.creadoEn)),
            fecha = etiquetaFecha(pedido.creadoEn, dias),
            deHoy = dias == 0,
            nota = pedido.nota,
            mostrador = pedido.mostrador,
            minutosPrep = pedido.minutosPrep,
            motivo = pedido.motivo
        )
    }

    private fun diasDesde(momento: Long): Int {
        fun inicioDelDia(ms: Long) = Calendar.getInstance().apply {
            timeInMillis = ms
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return TimeUnit.MILLISECONDS.toDays(inicioDelDia(System.currentTimeMillis()) - inicioDelDia(momento)).toInt()
    }

    private fun etiquetaFecha(momento: Long, dias: Int): String {
        val espanol = Locale.forLanguageTag("es-MX")
        return when {
            dias <= 0 -> "Hoy"
            dias == 1 -> "Ayer"
            dias < 7 -> SimpleDateFormat("EEEE", espanol).format(Date(momento)).replaceFirstChar { it.uppercase() }
            else -> SimpleDateFormat("dd/MM/yyyy", espanol).format(Date(momento))
        }
    }
}
