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
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import mx.edu.utez.cafeteria.data.Categoria
import mx.edu.utez.cafeteria.data.EstadoPedido
import mx.edu.utez.cafeteria.data.Producto

// ---------------- Tablas de pedidos ----------------

@Entity(tableName = "pedidos")
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

// ---------------- DAOs ----------------

@Dao
interface ProductoDao {
    @Query("SELECT * FROM productos ORDER BY categoria, nombre")
    fun observarTodos(): Flow<List<Producto>>

    @Query("SELECT COUNT(*) FROM productos")
    suspend fun contar(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(productos: List<Producto>)

    @Insert
    suspend fun insertar(producto: Producto): Long

    @Update
    suspend fun actualizar(producto: Producto)

    @Delete
    suspend fun borrar(producto: Producto)

    @Query("UPDATE productos SET disponible = :disponible WHERE id = :id")
    suspend fun cambiarDisponible(id: Int, disponible: Boolean)
}

@Dao
abstract class PedidoDao {
    @Transaction
    @Query("SELECT * FROM pedidos ORDER BY numero DESC")
    abstract fun observarTodos(): Flow<List<PedidoConLineas>>

    @Insert
    abstract suspend fun insertarPedido(pedido: PedidoEntity): Long

    @Insert
    abstract suspend fun insertarLineas(lineas: List<LineaEntity>)

    @Query("SELECT * FROM pedidos WHERE numero = :numero")
    abstract suspend fun obtener(numero: Int): PedidoEntity?

    @Update
    abstract suspend fun actualizar(pedido: PedidoEntity)

    // Guarda el pedido y sus platillos juntos: o se guarda todo o nada
    @Transaction
    open suspend fun crearPedido(pedido: PedidoEntity, lineas: List<LineaEntity>): Int {
        val numero = insertarPedido(pedido).toInt()
        insertarLineas(lineas.map { it.copy(pedidoNumero = numero) })
        return numero
    }
}

// ---------------- Base de datos ----------------

@Database(
    entities = [Producto::class, PedidoEntity::class, LineaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
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
