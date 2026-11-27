package mx.edu.utez.cafeteriaapi.exception;

import org.springframework.http.HttpStatus;

/**
 * Error que se le regresa a la app con un código HTTP y un mensaje en español.
 * La app muestra el mensaje tal cual (por ejemplo "Contraseña incorrecta.").
 */
public class ApiException extends RuntimeException {

    private final HttpStatus estado;

    public ApiException(HttpStatus estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    public HttpStatus getEstado() {
        return estado;
    }

    // 400: los datos que mandó la app no son válidos
    public static ApiException invalido(String mensaje) {
        return new ApiException(HttpStatus.BAD_REQUEST, mensaje);
    }

    // 401: usuario o contraseña incorrectos
    public static ApiException noAutorizado(String mensaje) {
        return new ApiException(HttpStatus.UNAUTHORIZED, mensaje);
    }

    // 403: tiene sesión, pero no le toca hacer eso
    public static ApiException prohibido(String mensaje) {
        return new ApiException(HttpStatus.FORBIDDEN, mensaje);
    }

    // 404: no existe
    public static ApiException noEncontrado(String mensaje) {
        return new ApiException(HttpStatus.NOT_FOUND, mensaje);
    }

    // 409: choca con algo que ya existe (ej. matrícula repetida)
    public static ApiException conflicto(String mensaje) {
        return new ApiException(HttpStatus.CONFLICT, mensaje);
    }
}
