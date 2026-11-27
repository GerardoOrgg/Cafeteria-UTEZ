package mx.edu.utez.cafeteria.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import mx.edu.utez.cafeteria.config.Configuracion
import java.io.File

object Fotos {
    private const val PREFIJO_RECURSO = "res:"
    private const val PREFIJO_SERVIDOR = "/fotos/"

    fun esArchivoLocal(foto: String): Boolean =
        !foto.startsWith(PREFIJO_RECURSO) && !foto.startsWith(PREFIJO_SERVIDOR)

    private fun carpeta(context: Context): File =
        File(context.filesDir, "fotos").apply { mkdirs() }

    fun nuevoArchivo(context: Context): File =
        File(carpeta(context), "platillo_${System.currentTimeMillis()}.jpg")

    fun uriPara(context: Context, archivo: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)

    fun copiarDeGaleria(context: Context, origen: Uri): String? = try {
        val destino = nuevoArchivo(context)
        context.contentResolver.openInputStream(origen)?.use { entrada ->
            destino.outputStream().use { salida -> entrada.copyTo(salida) }
        }
        destino.absolutePath
    } catch (e: Exception) {
        null
    }

    fun borrar(foto: String?) {
        if (foto == null || !esArchivoLocal(foto)) return
        runCatching { File(foto).delete() }
    }

    fun modelo(context: Context, foto: String?): Any? = when {
        foto == null -> null
        foto.startsWith(PREFIJO_RECURSO) -> {
            val nombre = foto.removePrefix(PREFIJO_RECURSO)
            context.resources.getIdentifier(nombre, "drawable", context.packageName).takeIf { it != 0 }
        }
        foto.startsWith(PREFIJO_SERVIDOR) -> Configuracion.URL_SERVIDOR.trimEnd('/') + foto
        else -> File(foto)
    }

    fun recurso(nombre: String): String = PREFIJO_RECURSO + nombre
}
