package mx.edu.utez.cafeteria.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
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
import mx.edu.utez.cafeteria.ui.screens.DetallePlatilloSheet
import mx.edu.utez.cafeteria.ui.screens.LoginScreen
import mx.edu.utez.cafeteria.ui.screens.MenuScreen
import mx.edu.utez.cafeteria.ui.screens.PedidosScreen
import mx.edu.utez.cafeteria.ui.screens.PerfilScreen
import mx.edu.utez.cafeteria.ui.screens.SeleccionCafeteriaScreen
import mx.edu.utez.cafeteria.ui.theme.Cafe
import mx.edu.utez.cafeteria.ui.ubicacion.CandadoUbicacion

@Composable
fun CafeteriaApp(viewModel: CafeteriaViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CandadoUbicacion {
        Box(Modifier.fillMaxSize()) {
            ContenidoApp(state, viewModel)
            AvisosServidor(state, viewModel::limpiarMensaje)
        }
    }
}

@Composable
private fun ContenidoApp(state: AppUiState, viewModel: CafeteriaViewModel) {
    val sesion = state.sesion
    when {
        state.cargando -> Box(Modifier.fillMaxSize().background(Cafe.Fondo), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Cafe.Ambar)
        }

        sesion == null -> LoginScreen(
            onIniciarSesion = viewModel::iniciarSesion,
            onRegistrar = viewModel::registrarAlumno,
            onRestablecer = viewModel::restablecerContrasena
        )

        state.esEncargado -> key(sesion.id) {
            PanelEncargado(state, viewModel)
        }

        else -> key(sesion.id) {
            FlujoAlumno(state, viewModel)
        }
    }
}

@Composable
private fun AvisosServidor(state: AppUiState, onCerrarMensaje: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        if (!state.conectado && state.sesion != null) {
            Text(
                "Sin conexión con el servidor · reintentando…",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(Cafe.Rojo)
                    .statusBarsPadding()
                    .padding(vertical = 6.dp)
            )
        }
        state.mensaje?.let { mensaje ->
            LaunchedEffect(mensaje) {
                delay(3500)
                onCerrarMensaje()
            }
            Surface(
                onClick = onCerrarMensaje,
                color = Cafe.TarjetaClara,
                shape = RoundedCornerShape(14.dp),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, bottom = 96.dp)
            ) {
                Text(
                    mensaje,
                    color = Cafe.Texto,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun FlujoAlumno(state: AppUiState, viewModel: CafeteriaViewModel) {
    var cambiandoCafeteria by rememberSaveable { mutableStateOf(false) }

    if (state.cafeteriaElegida == null || cambiandoCafeteria) {
        val puedeRegresar = state.cafeteriaElegida != null
        BackHandler(enabled = puedeRegresar) { cambiandoCafeteria = false }
        SeleccionCafeteriaScreen(
            cafeterias = Cafeterias.lista,
            onElegir = {
                viewModel.elegirCafeteria(it)
                cambiandoCafeteria = false
            },
            onAtras = if (puedeRegresar) ({ cambiandoCafeteria = false }) else null
        )
    } else {
        AppAlumno(
            state = state,
            viewModel = viewModel,
            onCambiarCafeteria = { cambiandoCafeteria = true }
        )
    }
}

private const val TAB_MENU = 0
private const val TAB_CATEGORIAS = 1
private const val TAB_PEDIDOS = 2
private const val TAB_PERFIL = 3

@Composable
private fun AppAlumno(
    state: AppUiState,
    viewModel: CafeteriaViewModel,
    onCambiarCafeteria: () -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(TAB_MENU) }
    var verCarrito by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = verCarrito || tab != TAB_MENU) {
        if (verCarrito) verCarrito = false else tab = TAB_MENU
    }

    state.detalle?.let { producto ->
        DetallePlatilloSheet(
            producto = producto,
            cafeteria = state.cafeteria,
            motivo = state.motivoNoPedible(producto),
            onAgregar = viewModel::agregarItem,
            onCerrar = viewModel::cerrarDetalle
        )
    }

    if (verCarrito) {
        CarritoScreen(
            state = state,
            onSumar = viewModel::sumar,
            onRestar = viewModel::restar,
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
                    onAgregar = viewModel::agregarRapido,
                    onQuitar = viewModel::quitarUno,
                    onVerCarrito = abrirCarrito,
                    onAbrir = viewModel::abrirDetalle,
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
                    onAbrir = viewModel::abrirDetalle,
                    onAgregar = viewModel::agregarRapido,
                    onQuitar = viewModel::quitarUno,
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
                    onCambiarCafeteria = onCambiarCafeteria,
                    onCerrarSesion = viewModel::cerrarSesion
                )
            }
        }
    }
}

private const val TAB_COMANDAS = 0
private const val TAB_ENTREGADOS = 1
private const val TAB_STOCK = 2
private const val TAB_SEDE = 3

private const val PLATILLO_NUEVO = -1

@Composable
private fun PanelEncargado(state: AppUiState, viewModel: CafeteriaViewModel) {
    var tab by rememberSaveable { mutableIntStateOf(TAB_COMANDAS) }
    var editandoId by rememberSaveable { mutableStateOf<Int?>(null) }

    BackHandler(enabled = editandoId == null && tab != TAB_COMANDAS) { tab = TAB_COMANDAS }

    val idEditando = editandoId
    if (idEditando != null) {
        BackHandler { editandoId = null }
        EditarPlatilloScreen(
            original = if (idEditando == PLATILLO_NUEVO) null else state.menu.find { it.id == idEditando },
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
                LogoApp(state.cafeteria.nombreCorto)
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
                    onTiempo = viewModel::cambiarTiempoEspera,
                    onCerrarSesion = viewModel::cerrarSesion
                )
            }
        }
    }
}
