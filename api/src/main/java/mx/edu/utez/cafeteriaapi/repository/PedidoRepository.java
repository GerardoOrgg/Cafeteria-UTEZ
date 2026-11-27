package mx.edu.utez.cafeteriaapi.repository;

import mx.edu.utez.cafeteriaapi.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    List<Pedido> findByCafeteriaIdOrderByNumeroDesc(String cafeteriaId);

    List<Pedido> findByMatriculaIgnoreCaseOrderByNumeroDesc(String matricula);

    List<Pedido> findAllByOrderByNumeroDesc();
}
