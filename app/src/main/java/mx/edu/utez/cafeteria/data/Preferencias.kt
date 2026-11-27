package mx.edu.utez.cafeteria.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mx.edu.utez.cafeteria.config.Configuracion

private val Context.dataStore by preferencesDataStore(name = "preferencias_cafeteria")

data class Ajustes(
    val cafeteriaId: String?, // null = todavía no elige cafetería
    val tiempoEspera: TiempoEspera,
    val usuario: Usuario
)

// Datos sueltos (clave-valor) que no necesitan una tabla: DataStore
class Preferencias(private val context: Context) {

    private object Claves {
        val CAFETERIA = stringPreferencesKey("cafeteria_id")
        val TIEMPO = stringPreferencesKey("tiempo_espera")
        val NOMBRE = stringPreferencesKey("nombre")
        val MATRICULA = stringPreferencesKey("matricula")
    }

    val ajustes: Flow<Ajustes> = context.dataStore.data.map { p ->
        Ajustes(
            cafeteriaId = p[Claves.CAFETERIA],
            tiempoEspera = p[Claves.TIEMPO]
                ?.let { nombre -> TiempoEspera.entries.find { it.name == nombre } }
                ?: TiempoEspera.MEDIO,
            usuario = Usuario(
                nombre = p[Claves.NOMBRE] ?: Configuracion.NOMBRE_INICIAL,
                matricula = p[Claves.MATRICULA] ?: Configuracion.MATRICULA_INICIAL
            )
        )
    }

    suspend fun guardarCafeteria(id: String) {
        context.dataStore.edit { it[Claves.CAFETERIA] = id }
    }

    suspend fun guardarTiempo(tiempo: TiempoEspera) {
        context.dataStore.edit { it[Claves.TIEMPO] = tiempo.name }
    }

    suspend fun guardarUsuario(usuario: Usuario) {
        context.dataStore.edit {
            it[Claves.NOMBRE] = usuario.nombre
            it[Claves.MATRICULA] = usuario.matricula
        }
    }
}
