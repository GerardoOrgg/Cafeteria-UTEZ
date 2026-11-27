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

    public void borrar(String ruta) {
        if (ruta == null || !ruta.startsWith(PREFIJO)) {
            return;
        }
        Path archivo = carpeta.resolve(ruta.substring(PREFIJO.length())).normalize();
        if (!archivo.startsWith(carpeta)) {
            return;
        }
        try {
            Files.deleteIfExists(archivo);
        } catch (IOException ignorado) {
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
