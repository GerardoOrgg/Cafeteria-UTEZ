package mx.edu.utez.cafeteriaapi.repository;

import mx.edu.utez.cafeteriaapi.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // Menú de una sola cafetería
    List<Producto> findByCafeteriaIdOrderByCategoriaAscDiaAscNombreAsc(String cafeteriaId);

    // Menú de las dos cafeterías
    List<Producto> findAllByOrderByCategoriaAscDiaAscNombreAsc();
}
