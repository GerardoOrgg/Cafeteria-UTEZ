package mx.edu.utez.cafeteriaapi.repository;

import mx.edu.utez.cafeteriaapi.model.Cafeteria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CafeteriaRepository extends JpaRepository<Cafeteria, String> {
}
