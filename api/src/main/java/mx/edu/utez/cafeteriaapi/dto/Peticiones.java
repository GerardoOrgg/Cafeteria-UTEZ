package mx.edu.utez.cafeteriaapi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Lo que la app MANDA a la API (cuerpo JSON de POST, PUT y PATCH).
 * Son "records": clases de solo datos, Java les crea constructor y getters.
 */
public final class Peticiones {

    private Peticiones() {
    }

    public record LoginRequest(
            @NotBlank(message = "Escribe tu matrícula o correo.") String usuario,
            @NotBlank(message = "Escribe tu contraseña.") String contrasena,
            // "ALUMNO" o "ENCARGADO": la pestaña que eligió en la app
            @NotBlank(message = "Falta el tipo de cuenta.") String rol
    ) {
    }

    public record RegistroRequest(
            @NotBlank(message = "Escribe tu nombre.") @Size(max = 100) String nombre,
            @NotBlank(message = "Escribe tu matrícula.") @Size(max = 30) String matricula,
            @NotBlank(message = "Escribe una contraseña.") @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.") String contrasena
    ) {
    }

    public record RestablecerRequest(
            @NotBlank(message = "Escribe tu matrícula.") String matricula,
            @NotBlank(message = "Escribe tu nombre.") String nombre,
            @NotBlank(message = "Escribe la nueva contraseña.") @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.") String nueva
    ) {
    }

    public record DisponibleRequest(@NotNull(message = "Falta indicar si está disponible.") Boolean disponible) {
    }

    // Un renglón del carrito. El precio NO se manda: lo calcula el servidor con los precios de la base
    public record ItemPedidoRequest(
            @NotNull(message = "Falta el platillo.") Integer productoId,
            String opcion,
            @Size(max = 100, message = "La nota de un platillo es de máximo 100 caracteres.") String nota,
            @NotNull @Min(value = 1, message = "La cantidad mínima es 1.") @Max(value = 20, message = "Máximo 20 piezas por platillo.") Integer cantidad
    ) {
    }

    public record PedidoRequest(
            @NotNull(message = "Falta el usuario.") Integer usuarioId,
            @NotBlank(message = "Falta la cafetería.") String cafeteriaId,
            @Size(max = 200, message = "La nota es de máximo 200 caracteres.") String nota,
            @NotEmpty(message = "El pedido está vacío.") List<@Valid ItemPedidoRequest> items
    ) {
    }

    public record CancelarRequest(String motivo) {
    }

    // "BAJO", "MEDIO" o "ALTO"
    public record TiempoRequest(@NotBlank(message = "Falta el tiempo de espera.") String tiempo) {
    }
}
