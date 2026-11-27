package mx.edu.utez.cafeteriaapi.exception;

import mx.edu.utez.cafeteriaapi.dto.Respuestas.MensajeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Convierte cualquier error en un JSON {"mensaje": "..."} con su código HTTP.
 * Así la API nunca regresa una página de error y la app siempre puede mostrar algo entendible.
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<MensajeDto> api(ApiException e) {
        return ResponseEntity.status(e.getEstado()).body(new MensajeDto(e.getMessage()));
    }

    // Falló una validación (@NotBlank, @Min...): se regresa el primer mensaje
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MensajeDto> validacion(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Datos inválidos.");
        return ResponseEntity.badRequest().body(new MensajeDto(mensaje));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<MensajeDto> jsonInvalido(Exception e) {
        return ResponseEntity.badRequest().body(new MensajeDto("La petición no tiene el formato esperado."));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<MensajeDto> sinUsuario(MissingRequestHeaderException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MensajeDto("Inicia sesión como encargado para hacer esto."));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<MensajeDto> fotoGrande(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(new MensajeDto("La foto es demasiado grande (máximo 15 MB)."));
    }

    // Cualquier otra cosa: se registra en la consola del servidor y se responde 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MensajeDto> inesperado(Exception e) {
        log.error("Error inesperado", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MensajeDto("Ocurrió un error en el servidor."));
    }
}
