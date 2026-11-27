package mx.edu.utez.cafeteriaapi.service;

import mx.edu.utez.cafeteriaapi.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

/**
 * Fotos de platillos que el encargado toma con la cámara.
 * Se guardan en la carpeta "fotos" del servidor y se sirven en http://IP:8080/fotos/archivo.jpg
 */
@Service
public class FotoService {

    public static final String PREFIJO = "/fotos/";

    private final Path carpeta;

    public FotoService(@Value("${app.fotos.carpeta:fotos}") String carpeta) throws IOException {
        this.carpeta = Paths.get(carpeta).toAbsolutePath().normalize();
        Files.createDirectories(this.carpeta);
    }

    public Path getCarpeta() {
        return carpeta;
    }

    // Guarda la foto con un nombre único y regresa su ruta pública ("/fotos/uuid.jpg")
    public String guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw ApiException.invalido("No llegó ninguna foto.");
        }
        String tipo = archivo.getContentType();
        if (tipo == null || !tipo.startsWith("image/")) {
            throw ApiException.invalido("El archivo no es una imagen.");
        }
        String nombre = UUID.randomUUID() + extension(archivo.getOriginalFilename());
        try (InputStream entrada = archivo.getInputStream()) {
            Files.copy(entrada, carpeta.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la foto", e);
        }
        return PREFIJO + nombre;
    }

    // Borra la foto del disco si era una subida al servidor (las "res:" vienen dentro de la app)
    public void borrar(String ruta) {
        if (ruta == null || !ruta.startsWith(PREFIJO)) {
            return;
        }
        Path archivo = carpeta.resolve(ruta.substring(PREFIJO.length())).normalize();
        // Seguridad: nunca borrar algo fuera de la carpeta de fotos
        if (!archivo.startsWith(carpeta)) {
            return;
        }
        try {
            Files.deleteIfExists(archivo);
        } catch (IOException ignorado) {
            // Si no se pudo borrar, solo queda el archivo huérfano
        }
    }

    private String extension(String nombreOriginal) {
        if (nombreOriginal != null) {
            String n = nombreOriginal.toLowerCase(Locale.ROOT);
            if (n.endsWith(".png")) return ".png";
            if (n.endsWith(".webp")) return ".webp";
        }
        return ".jpg";
    }
}
