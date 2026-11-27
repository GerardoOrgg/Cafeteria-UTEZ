package mx.edu.utez.cafeteriaapi.repository;

import mx.edu.utez.cafeteriaapi.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    // Panel del encargado: solo los pedidos de SU cafetería
    List<Pedido> findByCafeteriaIdOrderByNumeroDesc(String cafeteriaId);

    // "Mis pedidos" del alumno
    List<Pedido> findByMatriculaIgnoreCaseOrderByNumeroDesc(String matricula);

    List<Pedido> findAllByOrderByNumeroDesc();
}
