package mx.edu.utez.cafeteria.data

import android.content.Context
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import mx.edu.utez.cafeteria.data.local.AppDatabase
import mx.edu.utez.cafeteria.data.local.PedidoConLineas
import mx.edu.utez.cafeteria.data.remoto.CancelarRequest
import mx.edu.utez.cafeteria.data.remoto.ClienteApi
import mx.edu.utez.cafeteria.data.remoto.DisponibleRequest
import mx.edu.utez.cafeteria.data.remoto.ItemPedidoRequest
import mx.edu.utez.cafeteria.data.remoto.LoginRequest
import mx.edu.utez.cafeteria.data.remoto.PedidoDto
import mx.edu.utez.cafeteria.data.remoto.PedidoRequest
import mx.edu.utez.cafeteria.data.remoto.RegistroRequest
import mx.edu.utez.cafeteria.data.remoto.RestablecerRequest
import mx.edu.utez.cafeteria.data.remoto.TiempoRequest
import mx.edu.utez.cafeteria.data.remoto.UsuarioDto
import mx.edu.utez.cafeteria.data.remoto.aDto
import mx.edu.utez.cafeteria.data.remoto.aEntidad
import mx.edu.utez.cafeteria.data.remoto.aProducto
import mx.edu.utez.cafeteria.data.remoto.lineasEntidad
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// Resultado de iniciar sesión o registrarse
sealed interface ResultadoAuth {
    data object Exito : ResultadoAuth
    data class Error(val mensaje: String) : ResultadoAuth
}

/**
 * Único punto de entrada a los datos:
 * - API REST (Spring Boot): la fuente de verdad, compartida por todos los teléfonos.
 * - Room (tablas): copia local de lo que mandó la API. Las pantallas leen de aquí.
 * - DataStore (ajustes): sesión, cafetería elegida y tiempos de espera.
 *
 * Cada escritura (pedido nuevo, cambio de estado, platillo...) primero va a la API
 * y lo que responde el servidor se guarda en Room, así la pantalla se actualiza sola.
 * Si algo falla, la función lanza la excepción y el ViewModel muestra el mensaje.
 */
class Repositorio(context: Context) {

    private val db = AppDatabase.obtener(context)
    private val usuarioDao = db.usuarioDao()
    private val productoDao = db.productoDao()
    private val pedidoDao = db.pedidoDao()
    private val preferencias = Preferencias(context)
    private val api = ClienteApi.api

    val productos: Flow<List<Producto>> = productoDao.observarTodos()
    val pedidos: Flow<List<Pedido>> = pedidoDao.observarTodos().map { lista -> lista.map { it.aPedido() } }
    val ajustes: Flow<Ajustes> = preferencias.ajustes

    // Usuario con sesión abierta (o null si nadie ha iniciado sesión)
    @OptIn(ExperimentalCoroutinesApi::class)
    val sesion: Flow<Sesion?> = ajustes.map { it.sesionId }.distinctUntilChanged().flatMapLatest { id ->
        if (id == null) flowOf(null)
        else usuarioDao.observar(id).map { u -> u?.let { Sesion(it.id, it.nombre, it.matricula, it.correo, it.rol, it.cafeteriaId) } }
    }.distinctUntilChanged()

    // ---------- Sincronización con la API ----------

    /**
     * Baja del servidor el menú, los pedidos que le tocan a esta sesión y los tiempos de espera,
     * y los guarda en Room. El ViewModel la llama cada pocos segundos.
     * Regresa false si no se pudo hablar con el servidor (la app sigue mostrando lo último que tenía).
     */
    suspend fun sincronizar(sesion: Sesion?): Boolean = try {
        ClienteApi.usuarioId = sesion?.id
        productoDao.reemplazarTodos(api.productos().map { it.aProducto() })
        if (sesion != null) {
            // Encargado: los pedidos de SU cafetería. Alumno: solo los suyos.
            val lista = if (sesion.rol == Rol.ENCARGADO) {
                api.pedidos(cafeteriaId = sesion.cafeteriaId)
            } else {
                api.pedidos(matricula = sesion.matricula)
            }
            pedidoDao.reemplazarTodos(lista.map { it.aEntidad() to it.lineasEntidad() })
        }
        api.cafeterias().forEach { c ->
            val tiempo = TiempoEspera.entries.find { it.name == c.tiempoEspera }
            if (c.id != null && tiempo != null) preferencias.guardarTiempo(c.id, tiempo)
        }
        true
    } catch (e: CancellationException) {
        throw e // la corrutina se canceló (p. ej. cerró sesión): no es un error de red
    } catch (e: Exception) {
        false
    }

    // ---------- Sesión ----------

    suspend fun iniciarSesion(usuario: String, contrasena: String, rol: Rol): ResultadoAuth = conServidor {
        entrar(api.login(LoginRequest(usuario.trim(), contrasena, rol.name)))
    }

    suspend fun registrarAlumno(nombre: String, matricula: String, contrasena: String): ResultadoAuth = conServidor {
        entrar(api.registro(RegistroRequest(nombre.trim(), matricula.trim().uppercase(), contrasena)))
    }

    // Cambiar contraseña olvidada: el servidor la verifica con la matrícula y el nombre registrado
    suspend fun restablecerContrasena(matricula: String, nombre: String, nueva: String): ResultadoAuth = conServidor {
        api.restablecer(RestablecerRequest(matricula.trim(), nombre.trim(), nueva))
    }

    // Guarda al usuario que respondió el servidor y deja la sesión abierta (aunque se cierre la app)
    private suspend fun entrar(usuario: UsuarioDto) {
        val entidad = usuario.aEntidad()
        usuarioDao.guardar(entidad)
        ClienteApi.usuarioId = entidad.id
        preferencias.iniciarSesion(entidad.id)
    }

    suspend fun cerrarSesion() {
        ClienteApi.usuarioId = null
        preferencias.cerrarSesion()
    }

    // Convierte las excepciones de red en un ResultadoAuth.Error con mensaje para la pantalla
    private suspend fun conServidor(accion: suspend () -> Unit): ResultadoAuth = try {
        accion()
        ResultadoAuth.Exito
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        ResultadoAuth.Error(ClienteApi.mensajeDeError(e))
    }

    // ---------- Platillos ----------

    suspend fun guardarProducto(producto: Producto) {
        // Si la foto es nueva (un archivo del teléfono), primero se sube al servidor
        val fotoLocal = producto.foto?.takeIf { Fotos.esArchivoLocal(it) }
        val foto = if (fotoLocal != null) subirFoto(fotoLocal) else producto.foto
        val dto = producto.copy(foto = foto).aDto()
        val guardado = if (producto.id == 0) api.crearProducto(dto) else api.actualizarProducto(producto.id, dto)
        productoDao.guardar(guardado.aProducto())
        // Ya está en el servidor: se borra la copia que quedó en el teléfono
        if (fotoLocal != null) Fotos.borrar(fotoLocal)
    }

    private suspend fun subirFoto(ruta: String): String = withContext(Dispatchers.IO) {
        val archivo = File(ruta)
        val parte = MultipartBody.Part.createFormData("archivo", archivo.name, archivo.asRequestBody("image/jpeg".toMediaType()))
        api.subirFoto(parte).ruta ?: error("El servidor no regresó la ruta de la foto")
    }

    suspend fun borrarProducto(producto: Producto) {
        api.borrarProducto(producto.id) // el servidor también borra la foto
        productoDao.borrar(producto)
        Fotos.borrar(producto.foto)
    }

    suspend fun cambiarDisponible(id: Int, disponible: Boolean) {
        productoDao.guardar(api.cambiarDisponible(id, DisponibleRequest(disponible)).aProducto())
    }

    // ---------- Pedidos ----------

    // El servidor pone el nombre del alumno, calcula los precios y genera el número de pedido
    suspend fun crearPedido(sesion: Sesion, cafeteriaId: String, items: List<Pair<Producto, ItemCarrito>>, nota: String): Int {
        val peticion = PedidoRequest(
            usuarioId = sesion.id,
            cafeteriaId = cafeteriaId,
            nota = nota,
            items = items.map { (p, item) -> ItemPedidoRequest(p.id, item.opcion, item.nota.ifBlank { null }, item.cantidad) }
        )
        val creado = api.crearPedido(peticion)
        guardarPedido(creado)
        return creado.numero ?: 0
    }

    // RECIBIDO -> PREPARANDO -> LISTO -> ENTREGADO (el servidor decide el siguiente estado)
    suspend fun avanzarPedido(numero: Int) {
        guardarPedido(api.avanzarPedido(numero))
    }

    suspend fun cancelarPedido(numero: Int, motivo: String) {
        guardarPedido(api.cancelarPedido(numero, CancelarRequest(motivo)))
    }

    private suspend fun guardarPedido(dto: PedidoDto) {
        pedidoDao.guardar(dto.aEntidad(), dto.lineasEntidad())
    }

    // ---------- Ajustes ----------

    suspend fun guardarCafeteria(id: String) = preferencias.guardarCafeteria(id)

    // El tiempo de espera se cambia en el servidor para que lo vean todos los alumnos
    suspend fun guardarTiempo(cafeteriaId: String, tiempo: TiempoEspera) {
        api.cambiarTiempo(cafeteriaId, TiempoRequest(tiempo.name))
        preferencias.guardarTiempo(cafeteriaId, tiempo)
    }

    // ---------- Conversión de Room a lo que usan las pantallas ----------

    private fun PedidoConLineas.aPedido(): Pedido {
        val dias = diasDesde(pedido.creadoEn)
        return Pedido(
            numero = pedido.numero,
            cliente = Usuario(pedido.clienteNombre, pedido.matricula),
            cafeteriaId = pedido.cafeteriaId,
            lineas = lineas.map { LineaPedido(it.productoId, it.nombre, it.opcion, it.nota, it.precio, it.cantidad, it.foto, it.categoria) },
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
