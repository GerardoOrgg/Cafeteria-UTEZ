package mx.edu.utez.cafeteriaapi.service;

import mx.edu.utez.cafeteriaapi.dto.Respuestas.OpcionDto;
import mx.edu.utez.cafeteriaapi.dto.Respuestas.ProductoDto;
import mx.edu.utez.cafeteriaapi.exception.ApiException;
import mx.edu.utez.cafeteriaapi.model.Categoria;
import mx.edu.utez.cafeteriaapi.model.Opcion;
import mx.edu.utez.cafeteriaapi.model.Producto;
import mx.edu.utez.cafeteriaapi.model.Usuario;
import mx.edu.utez.cafeteriaapi.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Menú de cada cafetería: consultar, dar de alta, editar, marcar agotado y borrar platillos.
 */
@Service
public class ProductoService {

    public static final int PRECIO_MAXIMO = 2000;

    private final ProductoRepository productos;
    private final AuthService auth;
    private final FotoService fotos;

    public ProductoService(ProductoRepository productos, AuthService auth, FotoService fotos) {
        this.productos = productos;
        this.auth = auth;
        this.fotos = fotos;
    }

    // Sin cafeteriaId regresa el menú de las dos
    public List<Producto> listar(String cafeteriaId) {
        if (cafeteriaId == null || cafeteriaId.isBlank()) {
            return productos.findAllByOrderByCategoriaAscDiaAscNombreAsc();
        }
        return productos.findByCafeteriaIdOrderByCategoriaAscDiaAscNombreAsc(cafeteriaId);
    }

    public Producto obtener(Integer id) {
        return productos.findById(id).orElseThrow(() -> ApiException.noEncontrado("Ese platillo ya no existe."));
    }

    // El platillo siempre se guarda en la cafetería del encargado que lo crea
    @Transactional
    public Producto crear(ProductoDto datos, Integer usuarioId) {
        Usuario encargado = auth.exigirEncargado(usuarioId, null);
        Producto p = new Producto();
        aplicar(p, datos);
        p.setCafeteriaId(encargado.getCafeteriaId());
        p.setCreadoEn(System.currentTimeMillis());
        return productos.save(p);
    }

    @Transactional
    public Producto actualizar(Integer id, ProductoDto datos, Integer usuarioId) {
        Producto p = obtener(id);
        auth.exigirEncargado(usuarioId, p.getCafeteriaId());
        String fotoAnterior = p.getFoto();
        aplicar(p, datos);
        Producto guardado = productos.save(p);
        // Si cambió la foto, se borra el archivo viejo del servidor
        if (fotoAnterior != null && !fotoAnterior.equals(guardado.getFoto())) {
            fotos.borrar(fotoAnterior);
        }
        return guardado;
    }

    // Switch de "Menú & Stock": hay / no hay en barra
    @Transactional
    public Producto cambiarDisponible(Integer id, boolean disponible, Integer usuarioId) {
        Producto p = obtener(id);
        auth.exigirEncargado(usuarioId, p.getCafeteriaId());
        p.setDisponible(disponible);
        return productos.save(p);
    }

    // Los pedidos que ya lo tienen no cambian: guardan su propia copia del platillo
    @Transactional
    public void borrar(Integer id, Integer usuarioId) {
        Producto p = obtener(id);
        auth.exigirEncargado(usuarioId, p.getCafeteriaId());
        productos.delete(p);
        fotos.borrar(p.getFoto());
    }

    // Copia los datos que mandó la app al platillo, validando cada uno
    private void aplicar(Producto p, ProductoDto d) {
        String nombre = texto(d.nombre());
        if (nombre == null) throw ApiException.invalido("Escribe el nombre del platillo.");
        if (nombre.length() > 100) throw ApiException.invalido("El nombre es muy largo.");

        if (d.precio() == null || d.precio() < 1 || d.precio() > PRECIO_MAXIMO) {
            throw ApiException.invalido("El precio debe estar entre $1 y $" + PRECIO_MAXIMO + ".");
        }

        Categoria categoria;
        try {
            categoria = Categoria.valueOf(d.categoria());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw ApiException.invalido("Elige una categoría.");
        }

        // Los especiales necesitan día (1 = lunes ... 5 = viernes); los demás no llevan
        Integer dia = null;
        if (categoria == Categoria.ESPECIAL) {
            if (d.dia() == null || d.dia() < 1 || d.dia() > 5) {
                throw ApiException.invalido("Elige el día del especial (lunes a viernes).");
            }
            dia = d.dia();
        }

        int prepMin = d.prepMin() == null ? 5 : d.prepMin();
        int prepMax = d.prepMax() == null ? 10 : d.prepMax();
        if (prepMin < 1 || prepMax < prepMin || prepMax > 120) {
            throw ApiException.invalido("Revisa el tiempo de preparación (mínimo 1, y el máximo mayor o igual al mínimo).");
        }

        // La foto debe venir del servidor ("/fotos/...") o de la app ("res:..."): una ruta del teléfono no sirve aquí
        String foto = texto(d.foto());
        if (foto != null && !foto.startsWith(FotoService.PREFIJO) && !foto.startsWith("res:")) {
            throw ApiException.invalido("La foto no se subió al servidor. Vuelve a intentarlo.");
        }

        List<Opcion> opciones = new ArrayList<>();
        if (d.opciones() != null) {
            for (OpcionDto o : d.opciones()) {
                String nombreOpcion = texto(o.nombre());
                if (nombreOpcion == null) continue;
                int extra = o.extra() == null ? 0 : o.extra();
                if (extra < 0) throw ApiException.invalido("El extra de una opción no puede ser negativo.");
                opciones.add(new Opcion(nombreOpcion, extra, o.detalle() == null ? "" : o.detalle().trim()));
            }
        }
        String grupo = texto(d.grupoOpciones());
        if (!opciones.isEmpty() && grupo == null) grupo = "Opciones";

        p.setNombre(nombre);
        p.setDescripcion(d.descripcion() == null ? "" : d.descripcion().trim());
        p.setPrecio(d.precio());
        p.setCategoria(categoria);
        p.setDia(dia);
        p.setDetalle(d.detalle() == null ? "" : d.detalle().trim());
        p.setEtiqueta(texto(d.etiqueta()));
        p.setFoto(foto);
        p.setDisponible(d.disponible() == null || d.disponible());
        p.setPrepMin(prepMin);
        p.setPrepMax(prepMax);
        p.setGrupoOpciones(opciones.isEmpty() ? null : grupo);
        p.getOpciones().clear();
        p.getOpciones().addAll(opciones);
        p.setRecomendacion(texto(d.recomendacion()));
    }

    // null si viene vacío
    private static String texto(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
