package mx.edu.utez.cafeteria.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

// Manejo de fotos de platillos: se guardan en la carpeta privada de la app (files/fotos)
object Fotos {

    private const val PREFIJO_RECURSO = "res:"

    private fun carpeta(context: Context): File =
        File(context.filesDir, "fotos").apply { mkdirs() }

    // Archivo vacío donde la cámara escribirá la foto
    fun nuevoArchivo(context: Context): File =
        File(carpeta(context), "platillo_${System.currentTimeMillis()}.jpg")

    // URI content:// que se le pasa a la cámara (file:// ya no se permite)
    fun uriPara(context: Context, archivo: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)

    // Copia una imagen elegida de la galería a nuestra carpeta (el permiso de la galería es temporal)
    fun copiarDeGaleria(context: Context, origen: Uri): String? = try {
        val destino = nuevoArchivo(context)
        context.contentResolver.openInputStream(origen)?.use { entrada ->
            destino.outputStream().use { salida -> entrada.copyTo(salida) }
        }
        destino.absolutePath
    } catch (e: Exception) {
        null
    }

    // Borra el archivo si fue tomado por la app (las fotos incluidas "res:" no se borran)
    fun borrar(foto: String?) {
        if (foto == null || foto.startsWith(PREFIJO_RECURSO)) return
        runCatching { File(foto).delete() }
    }

    // Lo que se le pasa a Coil (AsyncImage) para dibujar la foto
    fun modelo(context: Context, foto: String?): Any? = when {
        foto == null -> null
        foto.startsWith(PREFIJO_RECURSO) -> {
            val nombre = foto.removePrefix(PREFIJO_RECURSO)
            context.resources.getIdentifier(nombre, "drawable", context.packageName).takeIf { it != 0 }
        }
        else -> File(foto)
    }

    fun recurso(nombre: String): String = PREFIJO_RECURSO + nombre
}
