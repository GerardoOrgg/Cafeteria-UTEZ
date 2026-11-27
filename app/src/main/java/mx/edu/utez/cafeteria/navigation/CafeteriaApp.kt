package mx.edu.utez.cafeteria.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.edu.utez.cafeteria.data.Cafeterias
import mx.edu.utez.cafeteria.ui.AppUiState
import mx.edu.utez.cafeteria.ui.CafeteriaViewModel
import mx.edu.utez.cafeteria.ui.components.Avatar
import mx.edu.utez.cafeteria.ui.components.BarraNavegacion
import mx.edu.utez.cafeteria.ui.components.Chip
import mx.edu.utez.cafeteria.ui.components.ItemNav
import mx.edu.utez.cafeteria.ui.components.LogoApp
import mx.edu.utez.cafeteria.ui.components.TopBarAlumno
import mx.edu.utez.cafeteria.ui.personal.ComandasScreen
import mx.edu.utez.cafeteria.ui.personal.EditarPlatilloScreen
import mx.edu.utez.cafeteria.ui.personal.EntregadosScreen
import mx.edu.utez.cafeteria.ui.personal.SedeScreen
import mx.edu.utez.cafeteria.ui.personal.StockScreen
import mx.edu.utez.cafeteria.ui.screens.CarritoScreen
import mx.edu.utez.cafeteria.ui.screens.CategoriasScreen
import mx.edu.utez.cafeteria.ui.screens.MenuScreen
import mx.edu.utez.cafeteria.ui.screens.PedidosScreen
import mx.edu.utez.cafeteria.ui.screens.PerfilScreen
import mx.edu.utez.cafeteria.ui.screens.SeleccionCafeteriaScreen
import mx.edu.utez.cafeteria.ui.theme.Cafe
import mx.edu.utez.cafeteria.ui.ubicacion.CandadoUbicacion

enum class Pantalla { SELECCION, ALUMNO, PERSONAL }

@Composable
fun CafeteriaApp(viewModel: CafeteriaViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Primero el candado de GPS: fuera de la UTEZ no se ve nada de la app
    CandadoUbicacion {
        if (state.cargando) {
            Box(Modifier.fillMaxSize().background(Cafe.Fondo), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Cafe.Ambar)
            }
        } else {
            ContenidoApp(state, viewModel)
        }
    }
}

@Composable
private fun ContenidoApp(state: AppUiState, viewModel: CafeteriaViewModel) {
        // Si ya eligió cafetería antes (guardado en DataStore), entra directo al menú
        var pantalla by rememberSaveable { mutableStateOf<Pantalla?>(null) }
        LaunchedEffect(Unit) {
            if (pantalla == null) pantalla = if (state.cafeteriaId == null) Pantalla.SELECCION else Pantalla.ALUMNO
        }

        when (pantalla) {
            null -> Unit

            Pantalla.SELECCION -> {
                val puedeRegresar = state.cafeteriaId != null
                BackHandler(enabled = puedeRegresar) { pantalla = Pantalla.ALUMNO }
                SeleccionCafeteriaScreen(
                    cafeterias = Cafeterias.lista,
                    onElegir = {
                        viewModel.elegirCafeteria(it)
                        pantalla = Pantalla.ALUMNO
                    },
                    onAtras = if (puedeRegresar) ({ pantalla = Pantalla.ALUMNO }) else null
                )
            }

            Pantalla.ALUMNO -> AppAlumno(
                state = state,
                viewModel = viewModel,
                onCambiarCafeteria = { pantalla = Pantalla.SELECCION },
                onAbrirPanel = { pantalla = Pantalla.PERSONAL }
            )

            Pantalla.PERSONAL -> PanelPersonal(
                state = state,
                viewModel = viewModel,
                onSalir = { pantalla = Pantalla.ALUMNO }
            )
        }
}

// ---------------- App del alumno ----------------

private const val TAB_MENU = 0
private const val TAB_CATEGORIAS = 1
private const val TAB_PEDIDOS = 2
private const val TAB_PERFIL = 3

@Composable
private fun AppAlumno(
    state: AppUiState,
    viewModel: CafeteriaViewModel,
    onCambiarCafeteria: () -> Unit,
    onAbrirPanel: () -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(TAB_MENU) }
    var verCarrito by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = verCarrito || tab != TAB_MENU) {
        if (verCarrito) verCarrito = false else tab = TAB_MENU
    }

    if (verCarrito) {
        CarritoScreen(
            state = state,
            onAgregar = viewModel::agregar,
            onQuitar = viewModel::quitar,
            onNota = viewModel::cambiarNota,
            onConfirmar = {
                viewModel.confirmarPedido()
                verCarrito = false
                tab = TAB_PEDIDOS
            },
            onCerrar = { verCarrito = false }
        )
        return
    }

    val activos = state.misPedidos.count { it.estado.activo }
    Scaffold(
        containerColor = Cafe.Fondo,
        topBar = { TopBarAlumno(state.cafeteria.nombreCorto, onCambiarCafeteria) },
        bottomBar = {
            BarraNavegacion(
                items = listOf(
                    ItemNav("Menú", Icons.Default.Restaurant),
                    ItemNav("Categorías", Icons.Default.GridView),
                    ItemNav("Pedidos", Icons.Default.Receipt, activos),
                    ItemNav("Perfil", Icons.Default.Person)
                ),
                seleccionado = tab,
                onSeleccionar = { tab = it }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            val abrirCarrito: () -> Unit = { verCarrito = true }
            when (tab) {
                TAB_MENU -> MenuScreen(
                    state = state,
                    onCambiarCafeteria = onCambiarCafeteria,
                    onBuscar = viewModel::buscar,
                    onAgregar = viewModel::agregar,
                    onQuitar = viewModel::quitar,
                    onVerCarrito = abrirCarrito,
                    onVerCategoria = {
                        viewModel.elegirCategoria(it)
                        tab = TAB_CATEGORIAS
                    },
                    onAgitar = viewModel::sugerirPlatillo,
                    onCerrarSugerencia = viewModel::cerrarSugerencia
                )

                TAB_CATEGORIAS -> CategoriasScreen(
                    state = state,
                    onCategoria = viewModel::elegirCategoria,
                    onAgregar = viewModel::agregar,
                    onQuitar = viewModel::quitar,
                    onVerCarrito = abrirCarrito
                )

                TAB_PEDIDOS -> PedidosScreen(
                    state = state,
                    onPedirDeNuevo = {
                        viewModel.pedirDeNuevo(it)
                        verCarrito = true
                    },
                    onVolverAlMenu = { tab = TAB_MENU }
                )

                else -> PerfilScreen(
                    state = state,
                    onGuardarUsuario = viewModel::guardarUsuario,
                    onCambiarCafeteria = onCambiarCafeteria,
                    onAbrirPanel = onAbrirPanel
                )
            }
        }
    }
}

// ---------------- Panel del personal ----------------

private const val TAB_COMANDAS = 0
private const val TAB_ENTREGADOS = 1
private const val TAB_STOCK = 2
private const val TAB_SEDE = 3

private const val PLATILLO_NUEVO = -1 // valor especial: abrir el formulario vacío

@Composable
private fun PanelPersonal(
    state: AppUiState,
    viewModel: CafeteriaViewModel,
    onSalir: () -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(TAB_COMANDAS) }
    // null = no se está editando; -1 = platillo nuevo; otro número = id del platillo a editar
    var editandoId by rememberSaveable { mutableStateOf<Int?>(null) }

    BackHandler(enabled = editandoId == null) {
        if (tab != TAB_COMANDAS) tab = TAB_COMANDAS else onSalir()
    }

    val idEditando = editandoId
    if (idEditando != null) {
        val original = state.productos.find { it.id == idEditando }
        BackHandler { editandoId = null }
        EditarPlatilloScreen(
            original = if (idEditando == PLATILLO_NUEVO) null else original,
            onGuardar = { producto, fotoAnterior ->
                viewModel.guardarPlatillo(producto, fotoAnterior)
                editandoId = null
            },
            onCancelar = { editandoId = null }
        )
        return
    }

    Scaffold(
        containerColor = Cafe.Fondo,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Cafe.Barra)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LogoApp("Panel de cafetería")
                Spacer(Modifier.weight(1f))
                Chip("En servicio", punto = Cafe.Verde)
                Spacer(Modifier.width(10.dp))
                Avatar()
            }
        },
        bottomBar = {
            BarraNavegacion(
                items = listOf(
                    ItemNav("Comandas", Icons.Default.Receipt, state.comandasActivas.size),
                    ItemNav("Entregados", Icons.Default.DoneAll),
                    ItemNav("Menú & Stock", Icons.Default.Inventory2),
                    ItemNav("Cafetería", Icons.Default.Storefront)
                ),
                seleccionado = tab,
                onSeleccionar = { tab = it }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            when (tab) {
                TAB_COMANDAS -> ComandasScreen(state, viewModel::avanzarComanda, viewModel::cancelarComanda)
                TAB_ENTREGADOS -> EntregadosScreen(state)
                TAB_STOCK -> StockScreen(
                    state = state,
                    onCambiarDisponible = viewModel::cambiarDisponible,
                    onNuevo = { editandoId = PLATILLO_NUEVO },
                    onEditar = { editandoId = it.id },
                    onBorrar = viewModel::borrarPlatillo
                )
                else -> SedeScreen(
                    state = state,
                    onCambiarSede = viewModel::elegirCafeteria,
                    onTiempo = viewModel::cambiarTiempoEspera,
                    onSalir = onSalir
                )
            }
        }
    }
}
