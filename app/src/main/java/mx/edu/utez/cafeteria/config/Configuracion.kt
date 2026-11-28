package mx.edu.utez.cafeteria.config

object Configuracion {
    const val URL_SERVIDOR = "http://localhost:8080/"

    const val SEGUNDOS_ACTUALIZAR = 5L

    const val UTEZ_LATITUD = 18.851107
    const val UTEZ_LONGITUD = -99.200660

    const val RADIO_METROS = 800f

    const val VALIDAR_UBICACION = false

    val ENCARGADOS = listOf(
        CuentaEncargado("nido@utez.edu.mx", "nido123", "nido", "Encargado Nido de Halcón"),
        CuentaEncargado("balcon@utez.edu.mx", "balcon123", "balcon", "Encargado El Balcón")
    )

    const val ALUMNO_DEMO_NOMBRE = "Juan Pérez"
    const val ALUMNO_DEMO_MATRICULA = "20213TI045"
    const val ALUMNO_DEMO_CONTRASENA = "123456"

    const val MOSTRAR_CUENTAS_PRUEBA = true

    const val DOMINIO_CORREO = "utez.edu.mx"
    const val MIN_CONTRASENA = 6

    const val UMBRAL_AGITAR = 2.4f

    const val ESPERA_ENTRE_AGITADAS_MS = 1500L

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
