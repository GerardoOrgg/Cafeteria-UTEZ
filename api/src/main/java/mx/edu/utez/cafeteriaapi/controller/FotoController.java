package mx.edu.utez.cafeteriaapi.controller;

import mx.edu.utez.cafeteriaapi.dto.Respuestas.FotoDto;
import mx.edu.utez.cafeteriaapi.service.AuthService;
import mx.edu.utez.cafeteriaapi.service.FotoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * POST /api/fotos (multipart, campo "archivo") -> sube la foto de un platillo y regresa su ruta.
 * Después la foto se ve en http://IP:8080/fotos/nombre.jpg (ver WebConfig).
 */
@RestController
@RequestMapping("/api/fotos")
public class FotoController {

    private final FotoService fotos;
    private final AuthService auth;

    public FotoController(FotoService fotos, AuthService auth) {
        this.fotos = fotos;
        this.auth = auth;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public FotoDto subir(@RequestHeader(Encabezados.USUARIO) Integer usuarioId, @RequestPart("archivo") MultipartFile archivo) {
        auth.exigirEncargado(usuarioId, null);
        return new FotoDto(fotos.guardar(archivo));
    }
}
