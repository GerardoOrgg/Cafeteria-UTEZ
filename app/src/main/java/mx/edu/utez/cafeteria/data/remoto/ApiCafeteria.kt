package mx.edu.utez.cafeteria.data.remoto

import com.google.gson.Gson
import mx.edu.utez.cafeteria.config.Configuracion
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException
import java.util.concurrent.TimeUnit

interface ApiCafeteria {
    @POST("api/auth/login")
    suspend fun login(@Body datos: LoginRequest): UsuarioDto

    @POST("api/auth/registro")
    suspend fun registro(@Body datos: RegistroRequest): UsuarioDto

    @POST("api/auth/restablecer")
    suspend fun restablecer(@Body datos: RestablecerRequest): MensajeDto

    @GET("api/productos")
    suspend fun productos(@Query("cafeteriaId") cafeteriaId: String? = null): List<ProductoDto>

    @POST("api/productos")
    suspend fun crearProducto(@Body producto: ProductoDto): ProductoDto

    @PUT("api/productos/{id}")
    suspend fun actualizarProducto(@Path("id") id: Int, @Body producto: ProductoDto): ProductoDto

    @PATCH("api/productos/{id}/disponible")
    suspend fun cambiarDisponible(@Path("id") id: Int, @Body datos: DisponibleRequest): ProductoDto

    @DELETE("api/productos/{id}")
    suspend fun borrarProducto(@Path("id") id: Int): MensajeDto

    @Multipart
    @POST("api/fotos")
    suspend fun subirFoto(@Part archivo: MultipartBody.Part): FotoDto

    @GET("api/pedidos")
    suspend fun pedidos(
        @Query("cafeteriaId") cafeteriaId: String? = null,
        @Query("matricula") matricula: String? = null
    ): List<PedidoDto>

    @POST("api/pedidos")
    suspend fun crearPedido(@Body pedido: PedidoRequest): PedidoDto

    @PATCH("api/pedidos/{numero}/avanzar")
    suspend fun avanzarPedido(@Path("numero") numero: Int): PedidoDto

    @PATCH("api/pedidos/{numero}/cancelar")
    suspend fun cancelarPedido(@Path("numero") numero: Int, @Body datos: CancelarRequest): PedidoDto

    @GET("api/cafeterias")
    suspend fun cafeterias(): List<CafeteriaDto>

    @PUT("api/cafeterias/{id}/tiempo")
    suspend fun cambiarTiempo(@Path("id") cafeteriaId: String, @Body datos: TiempoRequest): CafeteriaDto
}

object ClienteApi {
    @Volatile
    var usuarioId: Int? = null

    private val cliente = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { cadena ->
            val id = usuarioId
            val peticion = if (id != null) {
                cadena.request().newBuilder().header("X-Usuario-Id", id.toString()).build()
            } else {
                cadena.request()
            }
            cadena.proceed(peticion)
        }
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    val api: ApiCafeteria = Retrofit.Builder()
        .baseUrl(Configuracion.URL_SERVIDOR)
        .client(cliente)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiCafeteria::class.java)

    fun mensajeDeError(e: Throwable): String = when (e) {
        is HttpException -> {
            val cuerpo = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
            val mensaje = runCatching { Gson().fromJson(cuerpo, MensajeDto::class.java)?.mensaje }.getOrNull()
            mensaje ?: "El servidor respondió con error ${e.code()}."
        }
        is IOException -> "No hay conexión con el servidor (${Configuracion.URL_SERVIDOR}). Revisa que esté encendido y que estés en la misma red."
        else -> e.message ?: "Ocurrió un error inesperado."
    }
}
