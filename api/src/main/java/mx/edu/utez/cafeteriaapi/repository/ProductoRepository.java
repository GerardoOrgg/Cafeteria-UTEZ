package mx.edu.utez.cafeteriaapi.repository;

import mx.edu.utez.cafeteriaapi.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    List<Producto> findByCafeteriaIdOrderByCategoriaAscDiaAscNombreAsc(String cafeteriaId);

    List<Producto> findAllByOrderByCategoriaAscDiaAscNombreAsc();
}
