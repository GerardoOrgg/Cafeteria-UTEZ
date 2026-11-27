package mx.edu.utez.cafeteria.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.edu.utez.cafeteria.config.Configuracion
import mx.edu.utez.cafeteria.data.Ajustes
import mx.edu.utez.cafeteria.data.Cafeteria
import mx.edu.utez.cafeteria.data.Cafeterias
import mx.edu.utez.cafeteria.data.Categoria
import mx.edu.utez.cafeteria.data.Dias
import mx.edu.utez.cafeteria.data.Fotos
import mx.edu.utez.cafeteria.data.ItemCarrito
import mx.edu.utez.cafeteria.data.Pedido
import mx.edu.utez.cafeteria.data.Producto
import mx.edu.utez.cafeteria.data.Repositorio
import mx.edu.utez.cafeteria.data.ResultadoAuth
import mx.edu.utez.cafeteria.data.Rol
import mx.edu.utez.cafeteria.data.Sesion
import mx.edu.utez.cafeteria.data.TiempoEspera
import mx.edu.utez.cafeteria.data.remoto.ClienteApi

// Lo que solo vive mientras la app está abierta (no se guarda)
private data class EstadoLocal(
    val busqueda: String = "",
    val categoria: Categoria = Categoria.DIARIO,
    val carrito: List<ItemCarrito> = emptyList(),
    val nota: String = "",
    val sugerenciaId: Int? = null, // platillo sugerido al agitar el teléfono
    val detalleId: Int? = null, // platillo abierto en la hoja de detalle
    val conectado: Boolean = true, // false = la última vez no se pudo hablar con el servidor
    val mensaje: String? = null, // aviso para mostrar abajo (ej. error del servidor)
    val enviando: Boolean = false // se está mandando el pedido al servidor
)

// Todo lo que necesitan las pantallas, en un solo objeto
data class AppUiState(
    val cargando: Boolean = true,
    val sesion: Sesion? = null,
    val productos: List<Producto> = emptyList(),
    val pedidos: List<Pedido> = emptyList(),
    val cafeteriaElegida: String? = null,
    val tiempos: Map<String, TiempoEspera> = emptyMap(),
    val busqueda: String = "",
    val categoria: Categoria = Categoria.DIARIO,
    val carrito: List<ItemCarrito> = emptyList(),
    val nota: String = "",
    val sugerenciaId: Int? = null,
    val detalleId: Int? = null,
    val conectado: Boolean = true,
    val mensaje: String? = null,
    val enviando: Boolean = false
) {
    val esEncargado: Boolean get() = sesion?.rol == Rol.ENCARGADO

    // Encargado: su cafetería. Alumno: la que eligió.
    val cafeteria: Cafeteria get() = Cafeterias.porId(if (esEncargado) sesion?.cafeteriaId else cafeteriaElegida)

    val tiempoEspera: TiempoEspera get() = tiempos[cafeteria.id] ?: TiempoEspera.MEDIO

    val hoy: Int get() = Dias.hoy()

    // Menú de la cafetería actual (cada cafetería tiene el suyo)
    val menu: List<Producto> get() = productos.filter { it.cafeteriaId == cafeteria.id }

    fun productosDe(categoria: Categoria): List<Producto> = menu.filter { it.categoria == categoria }

    val especialesDeHoy: List<Producto>
        get() = menu.filter { it.categoria == Categoria.ESPECIAL && it.dia == hoy }

    // null = se puede pedir; si no, el motivo que se muestra
    fun motivoNoPedible(p: Producto): String? = when {
        !p.disponible -> "Agotado temporalmente"
        p.categoria == Categoria.ESPECIAL && p.dia != hoy -> "Solo el ${Dias.nombre(p.dia ?: 0).lowercase()}"
        else -> null
    }

    fun cantidadDe(productoId: Int): Int = carrito.filter { it.productoId == productoId }.sumOf { it.cantidad }

    // Renglones del carrito con su platillo (solo los que siguen existiendo y se pueden pedir)
    val lineasCarrito: List<Pair<Producto, ItemCarrito>>
        get() = carrito.mapNotNull { item ->
            menu.find { it.id == item.productoId && motivoNoPedible(it) == null }?.let { it to item }
        }

    val totalPiezas: Int get() = lineasCarrito.sumOf { it.second.cantidad }
    val totalCarrito: Int get() = lineasCarrito.sumOf { (p, i) -> (p.precio + i.extra) * i.cantidad }

    val sugerencia: Producto? get() = productos.find { it.id == sugerenciaId }
    val detalle: Producto? get() = productos.find { it.id == detalleId }

    val misPedidos: List<Pedido>
        get() = pedidos.filter { it.cliente.matricula == sesion?.matricula }

    // Panel del encargado: solo pedidos de SU cafetería
    val comandasActivas: List<Pedido>
        get() = pedidos.filter { it.cafeteriaId == cafeteria.id && it.estado.activo }.sortedBy { it.numero }

    val despachadosHoy: List<Pedido>
        get() = pedidos.filter { it.cafeteriaId == cafeteria.id && it.deHoy && !it.estado.activo }
}

class CafeteriaViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = Repositorio(app)
    private val local = MutableStateFlow(EstadoLocal())

    private data class Guardado(val ajustes: Ajustes, val sesion: Sesion?)

    // Junta los datos guardados (Room + DataStore) con el estado local
    val uiState: StateFlow<AppUiState> =
        combine(
            repo.productos,
            repo.pedidos,
            combine(repo.ajustes, repo.sesion) { a, s -> Guardado(a, s) },
            local
        ) { productos, pedidos, g, l ->
            AppUiState(
                cargando = false,
                sesion = g.sesion,
                productos = productos,
                pedidos = pedidos,
                cafeteriaElegida = g.ajustes.cafeteriaId,
                tiempos = g.ajustes.tiempos,
                busqueda = l.busqueda,
                categoria = l.categoria,
                carrito = l.carrito,
                nota = l.nota,
                sugerenciaId = l.sugerenciaId,
                detalleId = l.detalleId,
                conectado = l.conectado,
                mensaje = l.mensaje,
                enviando = l.enviando
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppUiState())

    init {
        // Cada pocos segundos le pide a la API lo nuevo (pedidos, menú, tiempos) y lo guarda en Room.
        // collectLatest: si cambia la sesión (otra cuenta), se cancela el ciclo anterior y empieza otro.
        viewModelScope.launch {
            repo.sesion.collectLatest { sesion ->
                while (true) {
                    val ok = repo.sincronizar(sesion)
                    local.update { it.copy(conectado = ok) }
                    delay(Configuracion.SEGUNDOS_ACTUALIZAR * 1000)
                }
            }
        }
    }

    // Corre una acción contra la API; si falla, muestra el mensaje en pantalla en vez de cerrar la app
    private fun ejecutar(alFallar: () -> Unit = {}, accion: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                accion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                alFallar()
                local.update { it.copy(mensaje = ClienteApi.mensajeDeError(e)) }
            }
        }
    }

    fun limpiarMensaje() = local.update { it.copy(mensaje = null) }

    // ---------------- Sesión ----------------

    fun iniciarSesion(usuario: String, contrasena: String, rol: Rol, alTerminar: (ResultadoAuth) -> Unit) {
        viewModelScope.launch { alTerminar(repo.iniciarSesion(usuario, contrasena, rol)) }
    }

    fun registrarAlumno(nombre: String, matricula: String, contrasena: String, alTerminar: (ResultadoAuth) -> Unit) {
        viewModelScope.launch { alTerminar(repo.registrarAlumno(nombre, matricula, contrasena)) }
    }

    fun restablecerContrasena(matricula: String, nombre: String, nueva: String, alTerminar: (ResultadoAuth) -> Unit) {
        viewModelScope.launch { alTerminar(repo.restablecerContrasena(matricula, nombre, nueva)) }
    }

    fun cerrarSesion() {
        local.value = EstadoLocal()
        viewModelScope.launch { repo.cerrarSesion() }
    }

    // ---------------- Alumno ----------------

    fun elegirCafeteria(id: String) {
        // Cada cafetería tiene su menú: al cambiar se vacía el carrito
        if (id != uiState.value.cafeteriaElegida) local.update { it.copy(carrito = emptyList(), busqueda = "") }
        viewModelScope.launch { repo.guardarCafeteria(id) }
    }

    fun buscar(texto: String) = local.update { it.copy(busqueda = texto) }

    fun elegirCategoria(categoria: Categoria) = local.update { it.copy(categoria = categoria) }

    fun abrirDetalle(productoId: Int) = local.update { it.copy(detalleId = productoId, sugerenciaId = null) }

    fun cerrarDetalle() = local.update { it.copy(detalleId = null) }

    // Botón "+" de las tarjetas: si el platillo tiene opciones, abre la hoja para elegir
    fun agregarRapido(productoId: Int) {
        val s = uiState.value
        val p = s.menu.find { it.id == productoId } ?: return
        if (s.motivoNoPedible(p) != null) return
        val ultimo = s.carrito.lastOrNull { it.productoId == productoId }
        when {
            ultimo != null -> sumar(ultimo.clave)
            p.opciones.isNotEmpty() -> abrirDetalle(productoId)
            else -> agregarItem(ItemCarrito(productoId, null, 0, "", 1))
        }
    }

    // Botón "−" de las tarjetas: quita una pieza del último renglón de ese platillo
    fun quitarUno(productoId: Int) {
        val ultimo = uiState.value.carrito.lastOrNull { it.productoId == productoId } ?: return
        restar(ultimo.clave)
    }

    // Desde la hoja de detalle: si ya hay uno igual (misma opción y nota), suma las piezas
    fun agregarItem(item: ItemCarrito) {
        local.update { l ->
            val existente = l.carrito.find { it.clave == item.clave }
            val carrito = if (existente != null) {
                l.carrito.map { if (it.clave == item.clave) it.copy(cantidad = it.cantidad + item.cantidad) else it }
            } else {
                l.carrito + item
            }
            l.copy(carrito = carrito, detalleId = null)
        }
    }

    fun sumar(clave: String) = local.update { l ->
        l.copy(carrito = l.carrito.map { if (it.clave == clave) it.copy(cantidad = it.cantidad + 1) else it })
    }

    fun restar(clave: String) = local.update { l ->
        l.copy(carrito = l.carrito.mapNotNull {
            if (it.clave != clave) it else if (it.cantidad <= 1) null else it.copy(cantidad = it.cantidad - 1)
        })
    }

    fun cambiarNota(nota: String) = local.update { it.copy(nota = nota) }

    // Si el servidor rechaza el pedido (sin conexión, platillo agotado...) el carrito se queda igual
    fun confirmarPedido() {
        val s = uiState.value
        val sesion = s.sesion ?: return
        val lineas = s.lineasCarrito
        if (lineas.isEmpty() || s.enviando) return
        local.update { it.copy(enviando = true) }
        ejecutar(alFallar = { local.update { it.copy(enviando = false) } }) {
            val numero = repo.crearPedido(sesion, s.cafeteria.id, lineas, s.nota.trim())
            local.update { it.copy(carrito = emptyList(), nota = "", enviando = false, mensaje = "Pedido #$numero enviado a ${s.cafeteria.nombreCorto}") }
        }
    }

    // Copia un pedido anterior al carrito (si el pedido fue en otra cafetería, cambia a esa)
    fun pedirDeNuevo(pedido: Pedido) {
        val s = uiState.value
        if (pedido.cafeteriaId != s.cafeteria.id) elegirCafeteria(pedido.cafeteriaId)
        local.update { l ->
            var carrito = if (pedido.cafeteriaId != s.cafeteria.id) emptyList() else l.carrito
            pedido.lineas.forEach { linea ->
                val p = s.productos.find { it.id == linea.productoId && it.cafeteriaId == pedido.cafeteriaId }
                if (p != null && s.motivoNoPedible(p) == null) {
                    val extra = p.opciones.find { it.nombre == linea.opcion }?.extra ?: 0
                    val item = ItemCarrito(p.id, linea.opcion, extra, linea.nota.orEmpty(), linea.cantidad)
                    val existente = carrito.find { it.clave == item.clave }
                    carrito = if (existente != null) carrito.map { if (it.clave == item.clave) it.copy(cantidad = it.cantidad + item.cantidad) else it }
                    else carrito + item
                }
            }
            l.copy(carrito = carrito)
        }
    }

    // Acelerómetro: al agitar, sugiere un platillo que se pueda pedir hoy
    fun sugerirPlatillo() {
        val s = uiState.value
        if (s.detalleId != null) return
        val opciones = s.menu.filter { s.motivoNoPedible(it) == null && it.id != s.sugerenciaId }
        if (opciones.isEmpty()) return
        local.update { it.copy(sugerenciaId = opciones.random().id) }
    }

    fun cerrarSugerencia() = local.update { it.copy(sugerenciaId = null) }

    // ---------------- Encargado ----------------

    fun avanzarComanda(numero: Int) {
        ejecutar { repo.avanzarPedido(numero) }
    }

    fun cancelarComanda(numero: Int) {
        ejecutar { repo.cancelarPedido(numero, "Cancelado por la cafetería") }
    }

    fun cambiarDisponible(id: Int, disponible: Boolean) {
        ejecutar { repo.cambiarDisponible(id, disponible) }
    }

    // El platillo siempre se guarda en la cafetería del encargado
    // fotoAnterior: la foto que tenía antes de editar, para borrar el archivo si se cambió
    fun guardarPlatillo(producto: Producto, fotoAnterior: String?) {
        val cafeteriaId = uiState.value.sesion?.cafeteriaId ?: return
        ejecutar {
            repo.guardarProducto(producto.copy(cafeteriaId = cafeteriaId))
            if (fotoAnterior != null && fotoAnterior != producto.foto) Fotos.borrar(fotoAnterior)
            local.update { it.copy(mensaje = "\"${producto.nombre}\" guardado") }
        }
    }

    fun borrarPlatillo(producto: Producto) {
        ejecutar { repo.borrarProducto(producto) }
    }

    fun cambiarTiempoEspera(tiempo: TiempoEspera) {
        val cafeteriaId = uiState.value.sesion?.cafeteriaId ?: return
        ejecutar { repo.guardarTiempo(cafeteriaId, tiempo) }
    }
}
