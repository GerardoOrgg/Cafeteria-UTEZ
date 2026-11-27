package mx.edu.utez.cafeteriaapi.service;

import mx.edu.utez.cafeteriaapi.dto.Peticiones.ItemPedidoRequest;
import mx.edu.utez.cafeteriaapi.dto.Peticiones.PedidoRequest;
import mx.edu.utez.cafeteriaapi.exception.ApiException;
import mx.edu.utez.cafeteriaapi.model.Cafeteria;
import mx.edu.utez.cafeteriaapi.model.Categoria;
import mx.edu.utez.cafeteriaapi.model.EstadoPedido;
import mx.edu.utez.cafeteriaapi.model.LineaPedido;
import mx.edu.utez.cafeteriaapi.model.Opcion;
import mx.edu.utez.cafeteriaapi.model.Pedido;
import mx.edu.utez.cafeteriaapi.model.Producto;
import mx.edu.utez.cafeteriaapi.model.Usuario;
import mx.edu.utez.cafeteriaapi.repository.CafeteriaRepository;
import mx.edu.utez.cafeteriaapi.repository.PedidoRepository;
import mx.edu.utez.cafeteriaapi.repository.ProductoRepository;
import mx.edu.utez.cafeteriaapi.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Pedidos: el alumno los crea y el encargado de ESA cafetería los avanza o cancela.
 */
@Service
public class PedidoService {

    private static final List<String> DIAS = List.of("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo");

    private final PedidoRepository pedidos;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;
    private final CafeteriaRepository cafeterias;
    private final AuthService auth;
    private final ZoneId zona;

    public PedidoService(PedidoRepository pedidos, ProductoRepository productos, UsuarioRepository usuarios,
                         CafeteriaRepository cafeterias, AuthService auth,
                         @Value("${app.zona-horaria:America/Mexico_City}") String zona) {
        this.pedidos = pedidos;
        this.productos = productos;
        this.usuarios = usuarios;
        this.cafeterias = cafeterias;
        this.auth = auth;
        this.zona = ZoneId.of(zona);
    }

    // 1 = lunes ... 7 = domingo (igual que Dias.hoy() en la app)
    public int diaDeHoy() {
        return LocalDate.now(zona).getDayOfWeek().getValue();
    }

    // Encargado: ?cafeteriaId=nido   ·   Alumno: ?matricula=20213TI045
    public List<Pedido> listar(String cafeteriaId, String matricula) {
        if (cafeteriaId != null && !cafeteriaId.isBlank()) {
            return pedidos.findByCafeteriaIdOrderByNumeroDesc(cafeteriaId);
        }
        if (matricula != null && !matricula.isBlank()) {
            return pedidos.findByMatriculaIgnoreCaseOrderByNumeroDesc(matricula.trim());
        }
        return pedidos.findAllByOrderByNumeroDesc();
    }

    public Pedido obtener(Integer numero) {
        return pedidos.findById(numero).orElseThrow(() -> ApiException.noEncontrado("No existe el pedido #" + numero + "."));
    }

    /**
     * Crea el pedido en estado RECIBIDO. Los precios se toman de la base, no de la app,
     * y se revisa que cada platillo sea de esa cafetería, haya en barra y (si es especial) toque hoy.
     */
    @Transactional
    public Pedido crear(PedidoRequest datos) {
        Usuario alumno = usuarios.findById(datos.usuarioId())
                .orElseThrow(() -> ApiException.noAutorizado("Tu sesión ya no es válida. Vuelve a iniciar sesión."));
        Cafeteria cafeteria = cafeterias.findById(datos.cafeteriaId())
                .orElseThrow(() -> ApiException.noEncontrado("Esa cafetería no existe."));
        int hoy = diaDeHoy();

        Pedido pedido = new Pedido();
        pedido.setUsuarioId(alumno.getId());
        pedido.setClienteNombre(alumno.getNombre());
        pedido.setMatricula(alumno.getMatricula());
        pedido.setCafeteriaId(cafeteria.getId());
        pedido.setEstado(EstadoPedido.RECIBIDO);
        pedido.setCreadoEn(System.currentTimeMillis());
        pedido.setNota(datos.nota() == null ? "" : datos.nota().trim());

        for (ItemPedidoRequest item : datos.items()) {
            Producto p = productos.findById(item.productoId())
                    .orElseThrow(() -> ApiException.invalido("Uno de los platillos ya no existe. Revisa tu carrito."));
            if (!p.getCafeteriaId().equals(cafeteria.getId())) {
                throw ApiException.invalido("\"" + p.getNombre() + "\" no es de " + cafeteria.getNombre() + ".");
            }
            if (!p.isDisponible()) {
                throw ApiException.invalido("\"" + p.getNombre() + "\" se agotó. Quítalo del carrito.");
            }
            if (p.getCategoria() == Categoria.ESPECIAL && !Objects.equals(p.getDia(), hoy)) {
                throw ApiException.invalido("\"" + p.getNombre() + "\" solo se vende el " + DIAS.get(p.getDia() - 1) + ".");
            }

            // Si el platillo tiene opciones, elegir una válida es obligatorio
            String opcion = null;
            int extra = 0;
            if (!p.getOpciones().isEmpty()) {
                Opcion elegida = p.getOpciones().stream()
                        .filter(o -> o.getNombre().equals(item.opcion()))
                        .findFirst()
                        .orElseThrow(() -> ApiException.invalido("Elige una opción válida para \"" + p.getNombre() + "\"."));
                opcion = elegida.getNombre();
                extra = elegida.getExtra();
            }
            String nota = item.nota() == null || item.nota().isBlank() ? null : item.nota().trim();
            pedido.getLineas().add(new LineaPedido(p, opcion, nota, p.getPrecio() + extra, item.cantidad()));
        }
        return pedidos.save(pedido);
    }

    // RECIBIDO -> PREPARANDO -> LISTO -> ENTREGADO
    @Transactional
    public Pedido avanzar(Integer numero, Integer usuarioId) {
        Pedido p = obtener(numero);
        auth.exigirEncargado(usuarioId, p.getCafeteriaId());
        switch (p.getEstado()) {
            case RECIBIDO -> p.setEstado(EstadoPedido.PREPARANDO);
            case PREPARANDO -> p.setEstado(EstadoPedido.LISTO);
            case LISTO -> {
                long minutos = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - p.getCreadoEn());
                p.setEstado(EstadoPedido.ENTREGADO);
                p.setMinutosPrep((int) Math.max(1, minutos));
            }
            default -> throw ApiException.invalido("El pedido #" + numero + " ya está cerrado.");
        }
        return pedidos.save(p);
    }

    @Transactional
    public Pedido cancelar(Integer numero, String motivo, Integer usuarioId) {
        Pedido p = obtener(numero);
        auth.exigirEncargado(usuarioId, p.getCafeteriaId());
        if (!p.getEstado().esActivo()) {
            throw ApiException.invalido("El pedido #" + numero + " ya no se puede cancelar.");
        }
        p.setEstado(EstadoPedido.CANCELADO);
        p.setMotivo(motivo == null || motivo.isBlank() ? "Cancelado por la cafetería" : motivo.trim());
        return pedidos.save(p);
    }
}
