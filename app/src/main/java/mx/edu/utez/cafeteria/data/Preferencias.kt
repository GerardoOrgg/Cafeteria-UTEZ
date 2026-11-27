package mx.edu.utez.cafeteria.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "preferencias_cafeteria")

data class Ajustes(
    val sesionId: Int?, // id del usuario con sesión abierta (null = nadie)
    val cafeteriaId: String?, // cafetería que eligió el alumno (null = todavía no elige)
    val tiempos: Map<String, TiempoEspera> // tiempo de espera de cada cafetería
)

// Datos sueltos (clave-valor) que no necesitan una tabla: DataStore
class Preferencias(private val context: Context) {

    private object Claves {
        val SESION = intPreferencesKey("sesion_id")
        val CAFETERIA = stringPreferencesKey("cafeteria_id")
        fun tiempo(cafeteriaId: String): Preferences.Key<String> = stringPreferencesKey("tiempo_$cafeteriaId")
    }

    val ajustes: Flow<Ajustes> = context.dataStore.data.map { p ->
        Ajustes(
            sesionId = p[Claves.SESION],
            cafeteriaId = p[Claves.CAFETERIA],
            tiempos = Cafeterias.lista.associate { c ->
                c.id to (p[Claves.tiempo(c.id)]?.let { nombre -> TiempoEspera.entries.find { it.name == nombre } } ?: TiempoEspera.MEDIO)
            }
        )
    }

    suspend fun iniciarSesion(usuarioId: Int) {
        context.dataStore.edit { it[Claves.SESION] = usuarioId }
    }

    suspend fun cerrarSesion() {
        context.dataStore.edit {
            it.remove(Claves.SESION)
            it.remove(Claves.CAFETERIA)
        }
    }

    suspend fun guardarCafeteria(id: String) {
        context.dataStore.edit { it[Claves.CAFETERIA] = id }
    }

    suspend fun guardarTiempo(cafeteriaId: String, tiempo: TiempoEspera) {
        context.dataStore.edit { it[Claves.tiempo(cafeteriaId)] = tiempo.name }
    }
}
