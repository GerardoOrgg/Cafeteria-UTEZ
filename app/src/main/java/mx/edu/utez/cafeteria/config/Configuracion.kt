package mx.edu.utez.cafeteria.config

/**
 * Todo lo que se cambia seguido está aquí.
 * Cambia un valor, vuelve a correr la app y listo.
 */
object Configuracion {

    // ---------- GPS ----------
    // Centro de la UTEZ (Av. Universidad Tecnológica No. 1, Palo Escrito, Emiliano Zapata, Mor.)
    const val UTEZ_LATITUD = 18.851107
    const val UTEZ_LONGITUD = -99.200660

    // Radio en metros donde se permite usar la app
    const val RADIO_METROS = 800f

    // Ponlo en false para probar la app fuera de la UTEZ (sin candado de ubicación)
    const val VALIDAR_UBICACION = true

    // ---------- Panel del personal ----------
    // PIN para entrar al panel de la cafetería desde Perfil
    const val PIN_PERSONAL = "1234"

    // ---------- Acelerómetro ----------
    // Fuerza (en G) para considerar que el teléfono se agitó. En reposo vale 1.0
    const val UMBRAL_AGITAR = 2.4f

    // Tiempo mínimo entre dos agitadas, en milisegundos
    const val ESPERA_ENTRE_AGITADAS_MS = 1500L

    // ---------- Usuario por defecto ----------
    const val NOMBRE_INICIAL = "Juan Pérez"
    const val MATRICULA_INICIAL = "20213TI045"

    // ---------- Platillos ----------
    val ETIQUETAS = listOf("Favorito", "Rápido", "Fit", "Nuevo", "Picante")
    const val PRECIO_MAXIMO = 2000
}
