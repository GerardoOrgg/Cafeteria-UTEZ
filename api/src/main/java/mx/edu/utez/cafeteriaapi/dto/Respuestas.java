package mx.edu.utez.cafeteriaapi.dto;

import mx.edu.utez.cafeteriaapi.model.Cafeteria;
import mx.edu.utez.cafeteriaapi.model.LineaPedido;
import mx.edu.utez.cafeteriaapi.model.Opcion;
import mx.edu.utez.cafeteriaapi.model.Pedido;
import mx.edu.utez.cafeteriaapi.model.Producto;
import mx.edu.utez.cafeteriaapi.model.Usuario;

import java.util.List;

/**
 * Lo que la API REGRESA a la app (y lo que manda la app al crear/editar un platillo).
 * Nunca se regresa la entidad directo: así la contraseña (hash) jamás sale del servidor.
 */
public final class Respuestas {

    private Respuestas() {
    }

    public record MensajeDto(String mensaje) {
    }

    public record UsuarioDto(Integer id, String nombre, String matricula, String correo, String rol, String cafeteriaId) {
        public static UsuarioDto de(Usuario u) {
            return new UsuarioDto(u.getId(), u.getNombre(), u.getMatricula(), u.getCorreo(), u.getRol().name(), u.getCafeteriaId());
        }
    }

    public record OpcionDto(String nombre, String detalle, Integer extra) {
        public static OpcionDto de(Opcion o) {
            return new OpcionDto(o.getNombre(), o.getDetalle(), o.getExtra());
        }
    }

    // Mismos campos que la clase Producto de la app
    public record ProductoDto(
            Integer id,
            String cafeteriaId,
            String nombre,
            String descripcion,
            Integer precio,
            String categoria,
            Integer dia,
            String detalle,
            String etiqueta,
            String foto,
            Boolean disponible,
            Integer prepMin,
            Integer prepMax,
            String grupoOpciones,
            List<OpcionDto> opciones,
            String recomendacion,
            Long creadoEn
    ) {
        public static ProductoDto de(Producto p) {
            return new ProductoDto(
                    p.getId(), p.getCafeteriaId(), p.getNombre(), p.getDescripcion(), p.getPrecio(),
                    p.getCategoria().name(), p.getDia(), p.getDetalle(), p.getEtiqueta(), p.getFoto(),
                    p.isDisponible(), p.getPrepMin(), p.getPrepMax(), p.getGrupoOpciones(),
                    p.getOpciones().stream().map(OpcionDto::de).toList(),
                    p.getRecomendacion(), p.getCreadoEn()
            );
        }
    }

    public record LineaDto(Integer productoId, String nombre, String opcion, String nota, Integer precio,
                           Integer cantidad, String foto, String categoria) {
        public static LineaDto de(LineaPedido l) {
            return new LineaDto(l.getProductoId(), l.getNombre(), l.getOpcion(), l.getNota(), l.getPrecio(),
                    l.getCantidad(), l.getFoto(), l.getCategoria() == null ? null : l.getCategoria().name());
        }
    }

    public record PedidoDto(
            Integer numero,
            String clienteNombre,
            String matricula,
            String cafeteriaId,
            String estado,
            Long creadoEn,
            String nota,
            String mostrador,
            Integer minutosPrep,
            String motivo,
            Integer total,
            List<LineaDto> lineas
    ) {
        public static PedidoDto de(Pedido p) {
            return new PedidoDto(
                    p.getNumero(), p.getClienteNombre(), p.getMatricula(), p.getCafeteriaId(), p.getEstado().name(),
                    p.getCreadoEn(), p.getNota(), p.getMostrador(), p.getMinutosPrep(), p.getMotivo(), p.total(),
                    p.getLineas().stream().map(LineaDto::de).toList()
            );
        }
    }

    public record CafeteriaDto(String id, String nombre, String tiempoEspera) {
        public static CafeteriaDto de(Cafeteria c) {
            return new CafeteriaDto(c.getId(), c.getNombre(), c.getTiempoEspera().name());
        }
    }

    // Ruta pública de una foto subida, ej. "/fotos/3f2a....jpg"
    public record FotoDto(String ruta) {
    }
}
