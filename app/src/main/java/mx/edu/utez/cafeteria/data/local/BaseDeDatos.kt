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

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["matricula"], unique = true), Index(value = ["correo"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val matricula: String,
    val correo: String,
    val rol: Rol,
    val cafeteriaId: String? = null
)

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

data class PedidoConLineas(
    @Embedded val pedido: PedidoEntity,
    @Relation(parentColumn = "numero", entityColumn = "pedidoNumero")
    val lineas: List<LineaEntity>
)

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

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios WHERE id = :id")
    fun observar(id: Int): Flow<UsuarioEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(usuario: UsuarioEntity)
}

@Dao
abstract class ProductoDao {
    @Query("SELECT * FROM productos ORDER BY categoria, dia, nombre")
    abstract fun observarTodos(): Flow<List<Producto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertarTodos(productos: List<Producto>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun guardar(producto: Producto)

    @Delete
    abstract suspend fun borrar(producto: Producto)

    @Query("DELETE FROM productos")
    abstract suspend fun borrarTodos()

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

    @Transaction
    open suspend fun guardar(pedido: PedidoEntity, lineas: List<LineaEntity>) {
        borrarLineas(pedido.numero)
        insertarPedido(pedido)
        insertarLineas(lineas.map { it.copy(pedidoNumero = pedido.numero) })
    }

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

@Database(
    entities = [UsuarioEntity::class, Producto::class, PedidoEntity::class, LineaEntity::class],
    version = 3,
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
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCIA = it }
            }
    }
}
