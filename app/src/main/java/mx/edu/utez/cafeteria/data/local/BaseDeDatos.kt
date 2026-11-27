package mx.edu.utez.cafeteria.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow
import mx.edu.utez.cafeteria.data.Categoria
import mx.edu.utez.cafeteria.data.EstadoPedido
import mx.edu.utez.cafeteria.data.Opcion
import mx.edu.utez.cafeteria.data.Producto
import mx.edu.utez.cafeteria.data.Rol

/*
 * Room guarda en el teléfono una COPIA (caché) de lo que manda la API.
 * Las pantallas leen de aquí (Flow): así se ven al instante y siguen mostrando lo último
 * aunque el servidor no responda un momento. Quien manda es el servidor.
 */

// ---------------- Tabla de usuarios ----------------

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["matricula"], unique = true), Index(value = ["correo"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val matricula: String, // encargados usan su correo aquí también
    val correo: String,
    // La contraseña ya no se guarda en el teléfono: la revisa el servidor (como SHA-256)
    val rol: Rol,
    val cafeteriaId: String? = null
)

// ---------------- Tablas de pedidos ----------------

@Entity(tableName = "pedidos", indices = [Index("cafeteriaId")])
data class PedidoEntity(
    @PrimaryKey(autoGenerate = true) val numero: Int = 0,
    val clienteNombre: String,
    val matricula: String,
    val cafeteriaId: String,
    val estado: EstadoPedido,
    val creadoEn: Long,
    val nota: String = "",
    val mostrador: String = "Mostrador 1",
    val minutosPrep: Int? = null,
    val motivo: String? = null
)

@Entity(
    tableName = "lineas_pedido",
    foreignKeys = [ForeignKey(
        entity = PedidoEntity::class,
        parentColumns = ["numero"],
        childColumns = ["pedidoNumero"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("pedidoNumero")]
)
data class LineaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pedidoNumero: Int,
    val productoId: Int,
    val nombre: String,
    val opcion: String? = null,
    val nota: String? = null,
    val precio: Int,
    val cantidad: Int,
    val foto: String?,
    val categoria: Categoria
)

// Un pedido con sus platillos (Room arma la relación solo)
data class PedidoConLineas(
    @Embedded val pedido: PedidoEntity,
    @Relation(parentColumn = "numero", entityColumn = "pedidoNumero")
    val lineas: List<LineaEntity>
)

// ---------------- Convertidor: lista de opciones <-> texto ----------------

class Convertidores {
    private val separadorOpcion = "\u001E"
    private val separadorCampo = "\u001F"

    @TypeConverter
    fun opcionesATexto(opciones: List<Opcion>): String =
        opciones.joinToString(separadorOpcion) { listOf(it.nombre, it.detalle, it.extra.toString()).joinToString(separadorCampo) }

    @TypeConverter
    fun textoAOpciones(texto: String): List<Opcion> =
        if (texto.isBlank()) emptyList()
        else texto.split(separadorOpcion).map { fila ->
            val campos = fila.split(separadorCampo)
            Opcion(campos.getOrElse(0) { "" }, campos.getOrElse(1) { "" }, campos.getOrNull(2)?.toIntOrNull() ?: 0)
        }
}

// ---------------- DAOs ----------------

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios WHERE id = :id")
    fun observar(id: Int): Flow<UsuarioEntity?>

    // Guarda (o reemplaza) al usuario que regresó el servidor al iniciar sesión
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(usuario: UsuarioEntity)
}

@Dao
abstract class ProductoDao {
    @Query("SELECT * FROM productos ORDER BY categoria, dia, nombre")
    abstract fun observarTodos(): Flow<List<Producto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertarTodos(productos: List<Producto>)

    // Inserta o reemplaza un platillo con el id que le dio el servidor
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun guardar(producto: Producto)

    @Delete
    abstract suspend fun borrar(producto: Producto)

    @Query("DELETE FROM productos")
    abstract suspend fun borrarTodos()

    // Cambia la copia local por lo que mandó el servidor: o se cambia todo o nada
    @Transaction
    open suspend fun reemplazarTodos(productos: List<Producto>) {
        borrarTodos()
        insertarTodos(productos)
    }
}

@Dao
abstract class PedidoDao {
    @Transaction
    @Query("SELECT * FROM pedidos ORDER BY numero DESC")
    abstract fun observarTodos(): Flow<List<PedidoConLineas>>

    // REPLACE: si el pedido ya estaba (mismo número), se actualiza
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertarPedido(pedido: PedidoEntity)

    @Insert
    abstract suspend fun insertarLineas(lineas: List<LineaEntity>)

    @Query("DELETE FROM lineas_pedido WHERE pedidoNumero = :numero")
    abstract suspend fun borrarLineas(numero: Int)

    @Query("DELETE FROM lineas_pedido")
    abstract suspend fun borrarTodasLasLineas()

    @Query("DELETE FROM pedidos")
    abstract suspend fun borrarTodos()

    // Guarda el pedido y sus platillos juntos: o se guarda todo o nada
    @Transaction
    open suspend fun guardar(pedido: PedidoEntity, lineas: List<LineaEntity>) {
        borrarLineas(pedido.numero)
        insertarPedido(pedido)
        insertarLineas(lineas.map { it.copy(pedidoNumero = pedido.numero) })
    }

    // Cambia todos los pedidos locales por los que mandó el servidor
    @Transaction
    open suspend fun reemplazarTodos(pedidos: List<Pair<PedidoEntity, List<LineaEntity>>>) {
        borrarTodasLasLineas()
        borrarTodos()
        pedidos.forEach { (pedido, lineas) ->
            insertarPedido(pedido)
            insertarLineas(lineas.map { it.copy(pedidoNumero = pedido.numero) })
        }
    }
}

// ---------------- Base de datos ----------------

@Database(
    entities = [UsuarioEntity::class, Producto::class, PedidoEntity::class, LineaEntity::class],
    version = 3, // 3: la app ahora es caché de la API (sin contraseñas en el teléfono)
    exportSchema = false
)
@TypeConverters(Convertidores::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun productoDao(): ProductoDao
    abstract fun pedidoDao(): PedidoDao

    companion object {
        @Volatile
        private var INSTANCIA: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cafeteria_utez.db"
                )
                    // Si cambias una tabla y subes la versión, borra y recrea la base (solo para desarrollo)
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCIA = it }
            }
    }
}
