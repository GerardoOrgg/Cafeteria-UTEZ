package mx.edu.utez.cafeteria.config

/**
 * Todo lo que se cambia seguido está aquí.
 * Cambia un valor, vuelve a correr la app y listo.
 */
object Configuracion {

    // ---------- Servidor (API REST en Spring Boot) ----------
    // IP de la PC donde corre la API (en Windows: abre cmd y escribe ipconfig -> "Dirección IPv4").
    // El teléfono y la PC tienen que estar en la misma red Wi-Fi.
    // En el emulador de Android Studio usa "http://10.0.2.2:8080/" (así llama el emulador a tu PC).
    const val URL_SERVIDOR = "http://192.168.1.100:8080/"

    // Cada cuántos segundos se piden al servidor los pedidos y el menú (para ver lo nuevo sin recargar)
    const val SEGUNDOS_ACTUALIZAR = 5L

    // ---------- GPS ----------
    // Centro de la UTEZ (Av. Universidad Tecnológica No. 1, Palo Escrito, Emiliano Zapata, Mor.)
    const val UTEZ_LATITUD = 18.851107
    const val UTEZ_LONGITUD = -99.200660

    // Radio en metros donde se permite usar la app
    const val RADIO_METROS = 800f

    // Ponlo en true para que la app solo funcione dentro de la UTEZ
    const val VALIDAR_UBICACION = false

    // ---------- Cuentas de encargado (una por cafetería) ----------
    // correo, contraseña, id de la cafetería, nombre que se muestra
    // Solo se usan para mostrarlas en el login: las cuentas reales viven en el servidor (DatosIniciales.java)
    val ENCARGADOS = listOf(
        CuentaEncargado("nido@utez.edu.mx", "nido123", "nido", "Encargado Nido de Halcón"),
        CuentaEncargado("balcon@utez.edu.mx", "balcon123", "balcon", "Encargado El Balcón")
    )

    // Alumno de prueba que ya viene registrado (también en el servidor)
    const val ALUMNO_DEMO_NOMBRE = "Juan Pérez"
    const val ALUMNO_DEMO_MATRICULA = "20213TI045"
    const val ALUMNO_DEMO_CONTRASENA = "123456"

    // Muestra las cuentas de prueba en la pantalla de inicio de sesión (ponlo en false al entregar)
    const val MOSTRAR_CUENTAS_PRUEBA = true

    const val DOMINIO_CORREO = "utez.edu.mx"
    const val MIN_CONTRASENA = 6

    // ---------- Acelerómetro ----------
    // Fuerza (en G) para considerar que el teléfono se agitó. En reposo vale 1.0
    const val UMBRAL_AGITAR = 2.4f

    // Tiempo mínimo entre dos agitadas, en milisegundos
    const val ESPERA_ENTRE_AGITADAS_MS = 1500L

    // ---------- Platillos ----------
    val ETIQUETAS = listOf("Favorito", "Rápido", "Fit", "Nuevo", "Picante")
    const val PRECIO_MAXIMO = 2000
    const val MAX_NOTA = 100
}

data class CuentaEncargado(
    val correo: String,
    val contrasena: String,
    val cafeteriaId: String,
    val nombre: String
)
