package mx.edu.utez.cafeteria.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.edu.utez.cafeteria.data.Cafeteria
import mx.edu.utez.cafeteria.data.Cafeterias
import mx.edu.utez.cafeteria.data.Categoria
import mx.edu.utez.cafeteria.data.Fotos
import mx.edu.utez.cafeteria.data.Pedido
import mx.edu.utez.cafeteria.data.Producto
import mx.edu.utez.cafeteria.data.Repositorio
import mx.edu.utez.cafeteria.data.TiempoEspera
import mx.edu.utez.cafeteria.data.Usuario
import mx.edu.utez.cafeteria.config.Configuracion

// Lo que solo vive mientras la app está abierta (no se guarda)
private data class EstadoLocal(
    val busqueda: String = "",
    val categoria: Categoria = Categoria.DIARIO,
    val carrito: Map<Int, Int> = emptyMap(), // id del platillo -> cantidad
    val nota: String = "",
    val sugerenciaId: Int? = null // platillo sugerido al agitar el teléfono
)

// Todo lo que necesitan las pantallas, en un solo objeto
data class AppUiState(
    val cargando: Boolean = true,
    val productos: List<Producto> = emptyList(),
    val pedidos: List<Pedido> = emptyList(),
    val cafeteriaId: String? = null,
    val tiempoEspera: TiempoEspera = TiempoEspera.MEDIO,
    val usuario: Usuario = Usuario(Configuracion.NOMBRE_INICIAL, Configuracion.MATRICULA_INICIAL),
    val busqueda: String = "",
    val categoria: Categoria = Categoria.DIARIO,
    val carrito: Map<Int, Int> = emptyMap(),
    val nota: String = "",
    val sugerenciaId: Int? = null
) {
    val cafeteria: Cafeteria get() = Cafeterias.porId(cafeteriaId)

    // Solo platillos que todavía existen y están disponibles
    val lineasCarrito: List<Pair<Producto, Int>>
        get() = carrito.mapNotNull { (id, cantidad) ->
            productos.find { it.id == id && it.disponible }?.let { it to cantidad }
        }

    val totalPiezas: Int get() = lineasCarrito.sumOf { it.second }
    val totalCarrito: Int get() = lineasCarrito.sumOf { (p, c) -> p.precio * c }

    val sugerencia: Producto? get() = productos.find { it.id == sugerenciaId }

    fun productosDe(categoria: Categoria): List<Producto> = productos.filter { it.categoria == categoria }
    fun cantidad(idProducto: Int): Int = carrito[idProducto] ?: 0
    fun agotado(idProducto: Int): Boolean = productos.find { it.id == idProducto }?.disponible == false

    val misPedidos: List<Pedido>
        get() = pedidos.filter { it.cliente.matricula == usuario.matricula }

    // Panel del personal: solo pedidos de la cafetería activa
    val comandasActivas: List<Pedido>
        get() = pedidos.filter { it.cafeteriaId == cafeteria.id && it.estado.activo }.sortedBy { it.numero }

    val despachadosHoy: List<Pedido>
        get() = pedidos.filter { it.cafeteriaId == cafeteria.id && it.deHoy && !it.estado.activo }
}

class CafeteriaViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = Repositorio(app)
    private val local = MutableStateFlow(EstadoLocal())

    // Junta los datos guardados (Room + DataStore) con el estado local
    val uiState: StateFlow<AppUiState> =
        combine(repo.productos, repo.pedidos, repo.ajustes, local) { productos, pedidos, ajustes, l ->
            AppUiState(
                cargando = false,
                productos = productos,
                pedidos = pedidos,
                cafeteriaId = ajustes.cafeteriaId,
                tiempoEspera = ajustes.tiempoEspera,
                usuario = ajustes.usuario,
                busqueda = l.busqueda,
                categoria = l.categoria,
                carrito = l.carrito,
                nota = l.nota,
                sugerenciaId = l.sugerenciaId
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppUiState())

    init {
        viewModelScope.launch { repo.sembrarSiVacio() }
    }

    // ---------------- Alumno ----------------

    fun elegirCafeteria(id: String) {
        viewModelScope.launch { repo.guardarCafeteria(id) }
    }

    fun buscar(texto: String) = local.update { it.copy(busqueda = texto) }

    fun elegirCategoria(categoria: Categoria) = local.update { it.copy(categoria = categoria) }

    fun agregar(idProducto: Int) {
        if (uiState.value.agotado(idProducto)) return
        local.update { it.copy(carrito = it.carrito + (idProducto to (it.carrito[idProducto] ?: 0) + 1)) }
    }

    fun quitar(idProducto: Int) {
        local.update {
            val actual = it.carrito[idProducto] ?: 0
            it.copy(carrito = if (actual <= 1) it.carrito - idProducto else it.carrito + (idProducto to actual - 1))
        }
    }

    fun cambiarNota(nota: String) = local.update { it.copy(nota = nota) }

    fun confirmarPedido() {
        val s = uiState.value
        val lineas = s.lineasCarrito
        if (lineas.isEmpty()) return
        viewModelScope.launch {
            repo.crearPedido(s.usuario, s.cafeteria.id, lineas, s.nota.trim())
            local.update { it.copy(carrito = emptyMap(), nota = "") }
        }
    }

    fun pedirDeNuevo(pedido: Pedido) {
        val s = uiState.value
        local.update { l ->
            var carrito = l.carrito
            pedido.lineas
                .filter { linea -> s.productos.any { it.id == linea.productoId && it.disponible } }
                .forEach { linea -> carrito = carrito + (linea.productoId to (carrito[linea.productoId] ?: 0) + linea.cantidad) }
            l.copy(carrito = carrito)
        }
    }

    fun guardarUsuario(nombre: String, matricula: String) {
        viewModelScope.launch { repo.guardarUsuario(Usuario(nombre.trim(), matricula.trim().uppercase())) }
    }

    // Acelerómetro: al agitar, sugiere un platillo disponible al azar
    fun sugerirPlatillo() {
        val disponibles = uiState.value.productos.filter { it.disponible && it.id != local.value.sugerenciaId }
        if (disponibles.isEmpty()) return
        local.update { it.copy(sugerenciaId = disponibles.random().id) }
    }

    fun cerrarSugerencia() = local.update { it.copy(sugerenciaId = null) }

    // ---------------- Personal ----------------

    fun avanzarComanda(numero: Int) {
        viewModelScope.launch { repo.avanzarPedido(numero) }
    }

    fun cancelarComanda(numero: Int) {
        viewModelScope.launch { repo.cancelarPedido(numero, "Cancelado por la cafetería") }
    }

    fun cambiarDisponible(id: Int, disponible: Boolean) {
        viewModelScope.launch { repo.cambiarDisponible(id, disponible) }
        if (!disponible) local.update { it.copy(carrito = it.carrito - id) }
    }

    // fotoAnterior: la foto que tenía antes de editar, para borrar el archivo si se cambió
    fun guardarPlatillo(producto: Producto, fotoAnterior: String?) {
        viewModelScope.launch {
            repo.guardarProducto(producto)
            if (fotoAnterior != null && fotoAnterior != producto.foto) Fotos.borrar(fotoAnterior)
        }
    }

    fun borrarPlatillo(producto: Producto) {
        viewModelScope.launch { repo.borrarProducto(producto) }
        local.update { it.copy(carrito = it.carrito - producto.id) }
    }

    fun cambiarTiempoEspera(tiempo: TiempoEspera) {
        viewModelScope.launch { repo.guardarTiempo(tiempo) }
    }
}
